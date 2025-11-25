package com.campus.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.CampusPoint;
import com.campus.entity.EventHistory;
import com.campus.mapper.CampusPointMapper;
import com.campus.mapper.EventHistoryMapper;
import com.campus.dto.request.EventCreateDTO;
import com.campus.dto.response.EventJoinResponseDTO;
import com.campus.dto.SettlementMsgDTO;
import com.campus.service.EventService;
import com.campus.service.UserService;
import com.campus.util.RabbitMqProducer;
import com.campus.repository.EventCacheRepository;
import com.campus.service.event.EventNotificationService;
import com.campus.service.event.EventSettlementService;
import com.campus.exception.BusinessException;
import com.campus.constant.EventConstant;
import com.campus.constant.RedisConstant;
import com.alibaba.fastjson.JSON;
import com.campus.vo.CompletedEventVO;
import com.campus.vo.EventHistoryVO;
import com.campus.vo.EventParticipantVO;
import com.campus.vo.NearbyEventVO;

/**
 * 事件模块Service实现类
 */
@Service
public class EventServiceImpl implements EventService {

    private final CampusPointMapper campusPointMapper;
    private final EventHistoryMapper eventHistoryMapper;
    private final UserService userService;
    private final RabbitMqProducer rabbitMqProducer;
    private final EventCacheRepository eventCacheRepository;
    private final EventNotificationService notificationService;
    private final EventSettlementService settlementService;

    // 事件默认搜索半径（米）
    @Value("${campus.event.default-radius}")
    private double defaultRadius;

    // 事件退出有效时间（秒，10分钟）
    @Value("${campus.event.quit-expire}")
    private long quitExpire;

    @Autowired
    public EventServiceImpl(CampusPointMapper campusPointMapper,
                          EventHistoryMapper eventHistoryMapper,
                          UserService userService,
                          RabbitMqProducer rabbitMqProducer,
                          EventCacheRepository eventCacheRepository,
                          EventNotificationService notificationService,
                          EventSettlementService settlementService) {
        this.campusPointMapper = campusPointMapper;
        this.eventHistoryMapper = eventHistoryMapper;
        this.userService = userService;
        this.rabbitMqProducer = rabbitMqProducer;
        this.eventCacheRepository = eventCacheRepository;
        this.notificationService = notificationService;
        this.settlementService = settlementService;
    }

    /**
     * 生成唯一事件ID（格式：event_UUID前8位）
     */
    private String generateEventId() {
        return "event_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String createEvent(EventCreateDTO dto, Long userId) {
        // 1. 校验用户信用分（是否允许发起）
        if (!userService.checkCreditForCreate(userId)) {
            throw new BusinessException("信用分低于60分，无法发起事件");
        }

        // 2. 校验校园点位
        CampusPoint point = campusPointMapper.selectById(dto.getPointId());
        if (point == null || !point.getIsValid()) {
            throw new BusinessException("无效的校园点位");
        }

        // 3. 校验事件类型（支持拼单/约伴/信标）
        if (!EventConstant.EVENT_TYPE_GROUP_BUY.equals(dto.getEventType())
                && !EventConstant.EVENT_TYPE_MEETUP.equals(dto.getEventType())
                && !EventConstant.EVENT_TYPE_BEACON.equals(dto.getEventType())) {
            throw new BusinessException("仅支持拼单（group_buy）、约伴（meetup）和信标（beacon）类型");
        }

        // 4. 生成事件ID
        String eventId = generateEventId();

        // 5. 计算事件过期时间
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusMinutes(dto.getExpireMinutes());
        long expireSeconds = dto.getExpireMinutes() * 60; // 过期时间（秒）

        Map<String, Object> extMetaMap = buildExtMeta(dto);

        // 6. 存储事件到Redis
        try {
            // 6.1 存储事件位置（GEO）
            eventCacheRepository.saveEventLocation(
                    dto.getEventType(),
                    eventId,
                    point.getLongitude(),
                    point.getLatitude()
            );

            // 6.2 存储事件详情（Hash）
            Map<String, Object> eventInfo = new HashMap<>();
            eventInfo.put("owner", userId.toString());
            eventInfo.put("event_type", dto.getEventType());
            eventInfo.put("title", dto.getTitle());
            eventInfo.put("target_num", dto.getTargetNum());
            eventInfo.put("current_num", 1);
            eventInfo.put("expire_minutes", dto.getExpireMinutes());
            eventInfo.put("ext_meta", JSON.toJSONString(extMetaMap));
            eventInfo.put("description", dto.getDescription());
            if (dto.getMediaUrls() != null && !dto.getMediaUrls().isEmpty()) {
                eventInfo.put("media_urls", JSON.toJSONString(dto.getMediaUrls()));
            }
            eventInfo.put("create_time", createTime.toString());
            eventInfo.put("expire_time", expireTime.toString());
            eventCacheRepository.saveEventInfo(eventId, eventInfo, expireSeconds);

            // 6.3 存储参与者集合（Set）
            eventCacheRepository.addParticipant(eventId, userId, expireSeconds);

            // 6.4 立即插入事件历史记录（解决"我的事件"显示问题）
            EventHistory eventHistory = new EventHistory();
            eventHistory.setEventId(eventId);
            eventHistory.setUserId(userId);
            eventHistory.setEventType(dto.getEventType());
            eventHistory.setTitle(dto.getTitle());
            eventHistory.setTargetNum(dto.getTargetNum());
            eventHistory.setCurrentNum(1); // 发起者默认参与
            eventHistory.setExpireMinutes(dto.getExpireMinutes());
            eventHistory.setExtMeta(JSON.toJSONString(extMetaMap));
            // participants字段使用数据库默认值NULL
            eventHistory.setStatus(EventConstant.EVENT_STATUS_ACTIVE); // 活跃状态
            eventHistory.setCreateTime(createTime);
            eventHistory.setExpireTime(expireTime);
            eventHistoryMapper.insertEventHistory(eventHistory);

            // 6.5 推送实时通知（1公里内在线用户）
            notificationService.pushNearbyUserNotify(eventId, dto.getTitle(), point.getLongitude(), point.getLatitude());

        } catch (Exception e) {
            // 异常回滚：删除已存储的Redis数据
            eventCacheRepository.cleanupEventData(eventId, dto.getEventType());
            throw new BusinessException("事件发起失败：" + e.getMessage());
        }

        return eventId;
    }

    @Override
    public List<NearbyEventVO> getNearbyEvents(String eventType, Long userId, double radius) {
        // 1. 获取用户当前位置
        Point userPoint = eventCacheRepository.getUserLocation(userId);
        if (userPoint == null) {
            throw new BusinessException("请先绑定校园位置");
        }
        double userLon = userPoint.getX();
        double userLat = userPoint.getY();

        // 2. 校验事件类型（如果eventType不为空）
        if (eventType != null && !eventType.isEmpty()) {
            if (!EventConstant.EVENT_TYPE_GROUP_BUY.equals(eventType)
                    && !EventConstant.EVENT_TYPE_MEETUP.equals(eventType)
                    && !EventConstant.EVENT_TYPE_BEACON.equals(eventType)) {
                throw new BusinessException("无效的事件类型");
            }
            
            // 查询特定类型的事件
            return getNearbyEventsByType(eventType, userId, userLon, userLat, radius);
        } else {
            // eventType为空时，查询所有类型的事件
            List<NearbyEventVO> allEvents = new ArrayList<>();
            allEvents.addAll(getNearbyEventsByType(EventConstant.EVENT_TYPE_GROUP_BUY, userId, userLon, userLat, radius));
            allEvents.addAll(getNearbyEventsByType(EventConstant.EVENT_TYPE_MEETUP, userId, userLon, userLat, radius));
            allEvents.addAll(getNearbyEventsByType(EventConstant.EVENT_TYPE_BEACON, userId, userLon, userLat, radius));
            
            // 按距离排序
            allEvents.sort((a, b) -> Integer.compare(a.getDistance(), b.getDistance()));
            return allEvents;
        }
    }

    /**
     * 根据事件类型查询附近事件
     */
    private List<NearbyEventVO> getNearbyEventsByType(String eventType, Long userId, double userLon, double userLat, double radius) {
        // 查询附近事件（GEO半径搜索）
        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = 
                eventCacheRepository.findNearbyEvents(eventType, userLon, userLat, radius);
        
        if (geoResults == null) {
            return new ArrayList<>();
        }

        // 过滤并封装结果
        List<NearbyEventVO> resultList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult : geoResults.getContent()) {
            String eventId = (String) geoResult.getContent().getName();
            double distance = geoResult.getDistance().getValue(); // 距离（米）

            // 校验事件是否存在
            if (!eventCacheRepository.eventExists(eventId)) {
                continue;
            }

            try {
                // 校验事件是否过期
                if (eventCacheRepository.isEventExpired(eventId)) {
                    eventCacheRepository.cleanupEventData(eventId, eventType);
                    continue;
                }

                // 获取事件信息
                Object currentNumObj = eventCacheRepository.getEventField(eventId, "current_num");
                Object targetNumObj = eventCacheRepository.getEventField(eventId, "target_num");
                Object titleObj = eventCacheRepository.getEventField(eventId, "title");
                Object createTimeObj = eventCacheRepository.getEventField(eventId, "create_time");
                Object descriptionObj = eventCacheRepository.getEventField(eventId, "description");
                Object mediaObj = eventCacheRepository.getEventField(eventId, "media_urls");
                
                if (currentNumObj == null || targetNumObj == null) {
                    continue;
                }
                
                Integer currentNum = Integer.valueOf(String.valueOf(currentNumObj));
                Integer targetNum = Integer.valueOf(String.valueOf(targetNumObj));
                
                // 满员跳过
                if (currentNum >= targetNum) {
                    continue;
                }

                // 封装VO
                NearbyEventVO vo = new NearbyEventVO();
                vo.setEventId(eventId);
                vo.setEventType(eventType);
                vo.setTitle(titleObj != null ? String.valueOf(titleObj) : "");
                vo.setDistance((int) distance);
                vo.setCurrentNum(currentNum);
                vo.setTargetNum(targetNum);
                if (createTimeObj != null) {
                    vo.setCreateTime(LocalDateTime.parse(String.valueOf(createTimeObj)));
                }
                if (descriptionObj != null) {
                    vo.setDescription(String.valueOf(descriptionObj));
                }
                if (mediaObj != null) {
                    vo.setMediaUrls(parseMediaUrls(String.valueOf(mediaObj)));
                }
                resultList.add(vo);
            } catch (Exception e) {
                System.err.println("处理事件时发生错误，事件ID: " + eventId + "，错误信息: " + e.getMessage());
                continue;
            }
        }
        
        return resultList;
    }

    @Override
    public EventJoinResponseDTO joinEvent(String eventId, Long userId) {
        // 1. 校验用户信用分（是否允许参与）
        if (!userService.checkCreditForJoin(userId)) {
            throw new BusinessException("信用分低于50分，无法参与事件");
        }

        // 2. 检查事件是否存在
        if (!eventCacheRepository.eventExists(eventId)) {
            throw new BusinessException("事件已过期或已结算");
        }

        // 3. 检查事件是否已过期
        if (eventCacheRepository.isEventExpired(eventId)) {
            throw new BusinessException("事件已过期");
        }

        // 4. Redis事务处理并发（避免超员）
        EventCacheRepository.JoinEventResult result = eventCacheRepository.joinEventTransaction(eventId, userId);

        if (!result.isSuccess()) {
            // 根据失败原因返回具体错误信息
            String reason = result.getFailureReason();
            if ("EVENT_NOT_FOUND".equals(reason)) {
                throw new BusinessException("事件已过期或已结算");
            } else if ("ALREADY_JOINED".equals(reason)) {
                throw new BusinessException("您已经参与了该事件");
            } else if ("EVENT_FULL".equals(reason)) {
                throw new BusinessException("事件已满员，无法参与");
            } else if ("BEACON_FULL".equals(reason)) {
                throw new BusinessException("信标事件仅支持1人参与，已满员");
            } else {
                throw new BusinessException("参与失败，请重试");
            }
        }

        // 5. 获取更新后的人数
        Integer currentNum = result.getCurrentNum();
        Integer targetNum = result.getTargetNum();
        boolean isFull = currentNum.equals(targetNum);

        // 6. 满员则发送结算消息到MQ
        if (isFull) {
            SettlementMsgDTO msgDTO = new SettlementMsgDTO();
            msgDTO.setEventId(eventId);
            msgDTO.setStatus(EventConstant.SETTLE_STATUS_SUCCESS);
            msgDTO.setSendTime(new Date());
            rabbitMqProducer.sendSettlementMsg(msgDTO);

            // 推送满员通知给所有参与者
            Set<Object> participants = eventCacheRepository.getParticipants(eventId);
            notificationService.pushEventFullNotify(eventId, participants, currentNum, targetNum);
        }

        // 7. 封装响应
        EventJoinResponseDTO response = new EventJoinResponseDTO();
        response.setCurrentParticipants(currentNum);
        response.setMaxParticipants(targetNum);
        return response;
    }

    @Override
    public void quitEvent(String eventId, Long userId) {
        // 1. 校验事件是否存在
        if (!eventCacheRepository.eventExists(eventId)) {
            throw new BusinessException("事件已过期或已结算");
        }

        // 2. 校验是否已参与
        if (!eventCacheRepository.isParticipant(eventId, userId)) {
            throw new BusinessException("你未参与该事件");
        }

        // 3. 校验是否为发起者（发起者不可退出）
        Object ownerObj = eventCacheRepository.getEventField(eventId, "owner");
        Long ownerId = Long.valueOf(String.valueOf(ownerObj));
        if (ownerId.equals(userId)) {
            throw new BusinessException("发起者不可退出，可选择取消事件");
        }

        // 4. 校验退出时间窗口（创建10分钟内可退出）
        Object createTimeObj = eventCacheRepository.getEventField(eventId, "create_time");
        LocalDateTime createTime = LocalDateTime.parse(String.valueOf(createTimeObj));
        if (LocalDateTime.now().isAfter(createTime.plusSeconds(quitExpire))) {
            throw new BusinessException("超过退出时间窗口，不可退出");
        }

        // 5. Redis事务处理
        boolean isSuccess = eventCacheRepository.quitEventTransaction(eventId, userId);

        if (!isSuccess) {
            throw new BusinessException("退出失败，请重试");
        }

        // 6. 删除参与者的事件历史记录
        try {
            QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("event_id", eventId).eq("user_id", userId);
            eventHistoryMapper.delete(queryWrapper);
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("删除参与者事件历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<EventHistoryVO> getEventHistory(Long userId, Integer pageNum, Integer pageSize) {
        // 查询用户参与的事件历史
        Page<EventHistory> page = new Page<>(pageNum, pageSize);
        QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");
        Page<EventHistory> resultPage = eventHistoryMapper.selectPage(page, queryWrapper);

        // 封装VO
        List<EventHistoryVO> voList = new ArrayList<>();
        for (EventHistory history : resultPage.getRecords()) {
            EventHistoryVO vo = new EventHistoryVO();
            vo.setEventId(history.getEventId());
            vo.setEventType(history.getEventType());
            vo.setTitle(history.getTitle());
            vo.setTargetNum(history.getTargetNum());
            vo.setCurrentNum(history.getCurrentNum());
            vo.setExpireMinutes(history.getExpireMinutes());
            vo.setStatus(history.getStatus());
            vo.setExtMeta(history.getExtMeta());
            vo.setCreateTime(history.getCreateTime());
            vo.setExpireTime(history.getExpireTime());
            vo.setSettleTime(history.getSettleTime());
            Map<String, Object> metaMap = parseExtMeta(history.getExtMeta());
            vo.setDescription(metaMap.get("description") != null ? String.valueOf(metaMap.get("description")) : null);
            vo.setMediaUrls(extractMediaUrls(metaMap));
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<CompletedEventVO> getCompletedEvents(Long userId) {
        QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .in("status", EventConstant.EVENT_STATUS_COMPLETED, EventConstant.EVENT_STATUS_EXPIRED)
                .orderByDesc("settle_time");
        List<EventHistory> records = eventHistoryMapper.selectList(queryWrapper);

        List<CompletedEventVO> result = new ArrayList<>();
        for (EventHistory record : records) {
            CompletedEventVO vo = new CompletedEventVO();
            vo.setEventId(record.getEventId());
            vo.setEventType(record.getEventType());
            vo.setTitle(record.getTitle());
            vo.setTargetNum(record.getTargetNum());
            vo.setCurrentNum(record.getCurrentNum());
            vo.setExpireMinutes(record.getExpireMinutes());
            vo.setStatus(record.getStatus());
            vo.setExtMeta(record.getExtMeta());
            vo.setCreateTime(record.getCreateTime());
            vo.setExpireTime(record.getExpireTime());
            vo.setSettleTime(record.getSettleTime());
            Map<String, Object> metaMap = parseExtMeta(record.getExtMeta());
            vo.setDescription(metaMap.get("description") != null ? String.valueOf(metaMap.get("description")) : null);
            vo.setMediaUrls(extractMediaUrls(metaMap));

            List<EventParticipantVO> participantVOList = new ArrayList<>();
            if (StringUtils.hasText(record.getParticipants())) {
                participantVOList = JSON.parseArray(record.getParticipants(), EventParticipantVO.class);
            }
            vo.setParticipants(participantVOList);
            result.add(vo);
        }

        return result;
    }

    @Override
    public void settleEvent(SettlementMsgDTO msgDTO) {
        settlementService.settleEvent(msgDTO.getEventId(), msgDTO.getStatus());
    }

    private Map<String, Object> buildExtMeta(EventCreateDTO dto) {
        Map<String, Object> result = new HashMap<>();
        if (dto.getExtMeta() != null) {
            result.putAll(dto.getExtMeta());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            result.put("description", dto.getDescription());
        }
        if (dto.getMediaUrls() != null && !dto.getMediaUrls().isEmpty()) {
            result.put("mediaUrls", dto.getMediaUrls());
        }
        return result;
    }

    private Map<String, Object> parseExtMeta(String extMetaJson) {
        if (!StringUtils.hasText(extMetaJson)) {
            return new HashMap<>();
        }
        try {
            return JSON.parseObject(extMetaJson);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<String> parseMediaUrls(String mediaJson) {
        if (!StringUtils.hasText(mediaJson)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(mediaJson, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractMediaUrls(Map<String, Object> extMeta) {
        if (extMeta == null || !extMeta.containsKey("mediaUrls")) {
            return Collections.emptyList();
        }
        Object value = extMeta.get("mediaUrls");
        if (value instanceof List<?>) {
            return (List<String>) value;
        }
        if (value instanceof String) {
            return parseMediaUrls((String) value);
        }
        return Collections.emptyList();
    }
}