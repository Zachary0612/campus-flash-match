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
import com.campus.service.NotificationService;
import com.campus.service.UserService;
import com.campus.util.RabbitMqProducer;
import com.campus.repository.EventCacheRepository;
import com.campus.service.event.EventNotificationService;
import com.campus.service.event.EventSettlementService;
import com.campus.exception.BusinessException;
import com.campus.constant.EventConstant;
import com.campus.constant.RedisConstant;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.dto.request.EventSearchDTO;
import com.campus.entity.SysUser;
import com.campus.mapper.SysUserMapper;
import com.campus.vo.CompletedEventVO;
import com.campus.vo.EventDetailVO;
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
    private final SysUserMapper sysUserMapper;
    private final UserService userService;
    private final RabbitMqProducer rabbitMqProducer;
    private final EventCacheRepository eventCacheRepository;
    private final EventNotificationService eventNotificationService;
    private final EventSettlementService settlementService;
    private final NotificationService notificationService;

    // 事件默认搜索半径（米）
    @Value("${campus.event.default-radius}")
    private double defaultRadius;

    // 事件退出有效时间（秒，10分钟）
    @Value("${campus.event.quit-expire}")
    private long quitExpire;

    @Autowired
    public EventServiceImpl(CampusPointMapper campusPointMapper,
                          EventHistoryMapper eventHistoryMapper,
                          SysUserMapper sysUserMapper,
                          UserService userService,
                          RabbitMqProducer rabbitMqProducer,
                          EventCacheRepository eventCacheRepository,
                          EventNotificationService eventNotificationService,
                          EventSettlementService settlementService,
                          NotificationService notificationService) {
        this.campusPointMapper = campusPointMapper;
        this.eventHistoryMapper = eventHistoryMapper;
        this.sysUserMapper = sysUserMapper;
        this.userService = userService;
        this.rabbitMqProducer = rabbitMqProducer;
        this.eventCacheRepository = eventCacheRepository;
        this.eventNotificationService = eventNotificationService;
        this.settlementService = settlementService;
        this.notificationService = notificationService;
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
            eventNotificationService.pushNearbyUserNotify(eventId, dto.getTitle(), point.getLongitude(), point.getLatitude());

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

        System.out.println("开始处理 " + geoResults.getContent().size() + " 个GEO结果");
        
        // 过滤并封装结果
        List<NearbyEventVO> resultList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult : geoResults.getContent()) {
            String eventId = (String) geoResult.getContent().getName();
            double distance = geoResult.getDistance().getValue(); // 距离（米）

            System.out.println("处理事件: " + eventId + ", 距离: " + distance + "米");

            // 校验事件是否存在
            if (!eventCacheRepository.eventExists(eventId)) {
                System.out.println("  -> 跳过: 事件不存在");
                continue;
            }

            try {
                // 校验事件是否过期
                if (eventCacheRepository.isEventExpired(eventId)) {
                    System.out.println("  -> 跳过: 事件已过期，清理数据");
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
                
                System.out.println("  -> currentNum: " + currentNumObj + ", targetNum: " + targetNumObj);
                
                if (currentNumObj == null || targetNumObj == null) {
                    System.out.println("  -> 跳过: currentNum或targetNum为null");
                    continue;
                }
                
                Integer currentNum = Integer.valueOf(String.valueOf(currentNumObj));
                Integer targetNum = Integer.valueOf(String.valueOf(targetNumObj));
                
                // 满员跳过
                if (currentNum >= targetNum) {
                    System.out.println("  -> 跳过: 事件已满员 (" + currentNum + "/" + targetNum + ")");
                    continue;
                }

                System.out.println("  -> 通过所有检查，添加到结果列表");
                
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
                e.printStackTrace();
                continue;
            }
        }
        
        System.out.println("最终返回 " + resultList.size() + " 个事件");
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
            System.err.println("用户参与事件失败: eventId=" + eventId + ", userId=" + userId + ", reason=" + reason);
            
            if ("EVENT_NOT_FOUND".equals(reason)) {
                throw new BusinessException("事件已过期或已结算");
            } else if ("ALREADY_JOINED".equals(reason)) {
                throw new BusinessException("您已经参与了该事件");
            } else if ("EVENT_FULL".equals(reason)) {
                throw new BusinessException("事件已满员，无法参与");
            } else if ("BEACON_FULL".equals(reason)) {
                throw new BusinessException("信标事件仅支持1人参与，已满员");
            } else if ("TRANSACTION_FAILED".equals(reason)) {
                throw new BusinessException("参与失败，事件可能刚刚满员，请刷新后重试");
            } else if ("INTERNAL_ERROR".equals(reason) || "REDIS_ERROR".equals(reason)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            } else {
                throw new BusinessException("参与失败，请重试");
            }
        }

        // 5. 获取更新后的人数
        Integer currentNum = result.getCurrentNum();
        Integer targetNum = result.getTargetNum();
        boolean isFull = currentNum.equals(targetNum);
        
        System.out.println("JoinEvent Debug: eventId=" + eventId + ", currentNum=" + currentNum + ", targetNum=" + targetNum + ", isFull=" + isFull);

        // 5.5 通知事件发起者有新成员加入（仅当未满员时）
        if (!isFull) {
            try {
                Object ownerObj = eventCacheRepository.getEventField(eventId, "owner");
                Object titleObj = eventCacheRepository.getEventField(eventId, "title");
                if (ownerObj != null && titleObj != null) {
                    Long ownerId = Long.valueOf(String.valueOf(ownerObj));
                    if (!ownerId.equals(userId)) {
                        String title = String.valueOf(titleObj);
                        String notifyMsg = String.format("有新成员加入您的事件【%s】（%d/%d）", title, currentNum, targetNum);
                        notificationService.sendNotification(ownerId, "event_joined", "事件参与", notifyMsg, eventId, userId);
                    }
                }
            } catch (Exception e) {
                // 发送通知失败不影响主流程
                System.err.println("发送参与通知失败: " + e.getMessage());
            }
        }

        // 6. 满员则直接同步结算（不依赖异步MQ，确保Redis数据未被清理前完成结算）
        if (isFull) {
            System.out.println("JoinEvent Debug: Event full, triggering synchronous settlement for " + eventId);
            
            // 在结算前先获取Redis数据，避免被定时任务清理
            Map<Object, Object> eventInfo = eventCacheRepository.getEventInfo(eventId);
            Set<Object> participantsSet = eventCacheRepository.getParticipants(eventId);
            
            if (eventInfo != null && participantsSet != null) {
                System.out.println("JoinEvent Debug: Successfully retrieved Redis data before settlement");
                // 直接同步调用结算服务，传入已获取的数据
                settlementService.settleEventWithData(eventId, EventConstant.SETTLE_STATUS_SUCCESS, eventInfo, participantsSet);
            } else {
                System.out.println("JoinEvent Debug: Redis data already missing, falling back to basic settlement");
                // 如果Redis数据已丢失，仍尝试基本结算
                settlementService.settleEvent(eventId, EventConstant.SETTLE_STATUS_SUCCESS);
            }

            // 推送满员通知给所有参与者（注意：结算后Redis数据已清理，需要从结算前获取）
            // 通知已在settlementService.settleEvent中推送，这里不再重复
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
        // 调用新方法，查询所有事件（发起的+参与的）
        return getMyEvents(userId, "all", pageNum, pageSize);
    }

    @Override
    public List<CompletedEventVO> getCompletedEvents(Long userId) {
        // 查询用户参与的所有已完成事件（包括发起的和加入的）
        List<EventHistory> allEvents = eventHistoryMapper.findAllUserEvents(userId);
        
        // 过滤出已完成的事件
        List<EventHistory> records = new ArrayList<>();
        for (EventHistory event : allEvents) {
            if (EventConstant.EVENT_STATUS_COMPLETED.equals(event.getStatus()) 
                || EventConstant.EVENT_STATUS_EXPIRED.equals(event.getStatus())
                || "settled".equals(event.getStatus())) {
                records.add(event);
            }
        }
        
        // 按结算时间排序
        records.sort((a, b) -> {
            if (a.getSettleTime() == null && b.getSettleTime() == null) return 0;
            if (a.getSettleTime() == null) return 1;
            if (b.getSettleTime() == null) return -1;
            return b.getSettleTime().compareTo(a.getSettleTime());
        });

        // 收集用户ID用于批量查询
        Set<Long> userIds = new java.util.HashSet<>();
        for (EventHistory record : records) {
            userIds.add(record.getUserId());
            if (record.getParticipants() != null) {
                try {
                    List<Map> pList = JSON.parseArray(record.getParticipants(), Map.class);
                    for (Map p : pList) {
                        if (p.get("userId") != null) {
                            userIds.add(Long.valueOf(String.valueOf(p.get("userId"))));
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        
        // 批量查询用户信息
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
            userQuery.in("id", userIds);
            List<SysUser> users = sysUserMapper.selectList(userQuery);
            for (SysUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

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
            
            // 设置是否为发起者
            vo.setIsOwner(record.getUserId().equals(userId));
            
            // 设置发起者信息
            SysUser owner = userMap.get(record.getUserId());
            if (owner != null) {
                vo.setOwnerNickname(owner.getNickname());
                vo.setOwnerAvatar(owner.getAvatar());
            }

            // 解析参与者列表并填充用户信息
            List<EventParticipantVO> participantVOList = new ArrayList<>();
            if (StringUtils.hasText(record.getParticipants())) {
                try {
                    List<Map> pList = JSON.parseArray(record.getParticipants(), Map.class);
                    for (Map p : pList) {
                        EventParticipantVO pvo = new EventParticipantVO();
                        if (p.get("userId") != null) {
                            Long pUserId = Long.valueOf(String.valueOf(p.get("userId")));
                            pvo.setUserId(pUserId);
                            SysUser pUser = userMap.get(pUserId);
                            if (pUser != null) {
                                pvo.setNickname(pUser.getNickname());
                                pvo.setAvatar(pUser.getAvatar());
                            } else if (p.get("nickname") != null) {
                                pvo.setNickname(String.valueOf(p.get("nickname")));
                            }
                            pvo.setOwner(Boolean.TRUE.equals(p.get("owner")));
                            pvo.setStatus(p.get("status") != null ? String.valueOf(p.get("status")) : null);
                            participantVOList.add(pvo);
                        }
                    }
                } catch (Exception ignored) {}
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
    
    @Override
    public EventDetailVO getEventDetail(String eventId, Long userId) {
        // 先尝试从Redis获取活跃事件
        Map<Object, Object> eventInfo = eventCacheRepository.getEventInfo(eventId);
        
        if (eventInfo != null && !eventInfo.isEmpty()) {
            return buildEventDetailFromRedis(eventId, eventInfo, userId);
        }
        
        // 从数据库获取历史事件
        QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("event_id", eventId);
        EventHistory event = eventHistoryMapper.selectOne(queryWrapper);
        
        if (event == null) {
            throw new BusinessException("事件不存在");
        }
        
        return buildEventDetailFromDB(event, userId);
    }
    
    private EventDetailVO buildEventDetailFromRedis(String eventId, Map<Object, Object> eventInfo, Long userId) {
        EventDetailVO vo = new EventDetailVO();
        vo.setEventId(eventId);
        vo.setTitle(String.valueOf(eventInfo.get("title")));
        vo.setEventType(String.valueOf(eventInfo.get("event_type")));
        vo.setDescription(eventInfo.get("description") != null ? String.valueOf(eventInfo.get("description")) : null);
        vo.setTargetNum(Integer.valueOf(String.valueOf(eventInfo.get("target_num"))));
        vo.setCurrentNum(Integer.valueOf(String.valueOf(eventInfo.get("current_num"))));
        vo.setExpireMinutes(Integer.valueOf(String.valueOf(eventInfo.get("expire_minutes"))));
        vo.setStatus(EventConstant.EVENT_STATUS_ACTIVE);
        
        Long ownerId = Long.valueOf(String.valueOf(eventInfo.get("owner")));
        vo.setOwnerId(ownerId);
        
        // 获取发起者信息
        SysUser owner = userService.getUserById(ownerId);
        if (owner != null) {
            vo.setOwnerNickname(owner.getNickname());
            vo.setOwnerAvatar(owner.getAvatar());
            vo.setOwnerCreditScore(owner.getCreditScore());
        }
        
        // 解析扩展元数据
        String extMetaStr = String.valueOf(eventInfo.get("ext_meta"));
        vo.setExtMeta(parseExtMeta(extMetaStr));
        
        // 解析媒体URL
        if (eventInfo.get("media_urls") != null) {
            vo.setMediaUrls(parseMediaUrls(String.valueOf(eventInfo.get("media_urls"))));
        }
        
        // 获取参与者
        Set<Object> participantsSet = eventCacheRepository.getParticipants(eventId);
        List<EventParticipantVO> participants = new ArrayList<>();
        for (Object obj : participantsSet) {
            Long participantId = Long.valueOf(String.valueOf(obj));
            SysUser user = userService.getUserById(participantId);
            if (user != null) {
                EventParticipantVO pvo = new EventParticipantVO();
                pvo.setUserId(participantId);
                pvo.setNickname(user.getNickname());
                pvo.setOwner(participantId.equals(ownerId));
                participants.add(pvo);
            }
        }
        vo.setParticipants(participants);
        
        // 时间信息
        vo.setCreateTime(LocalDateTime.parse(String.valueOf(eventInfo.get("create_time"))));
        vo.setExpireTime(LocalDateTime.parse(String.valueOf(eventInfo.get("expire_time"))));
        
        // 用户相关状态
        if (userId != null) {
            vo.setIsJoined(participantsSet.contains(userId.toString()) || participantsSet.contains(userId));
            // 收藏状态需要查询数据库
            vo.setIsFavorite(false); // 活跃事件暂不支持收藏状态查询
        }
        
        vo.setCommentCount(0);
        vo.setFavoriteCount(0);
        
        return vo;
    }
    
    private EventDetailVO buildEventDetailFromDB(EventHistory event, Long userId) {
        EventDetailVO vo = new EventDetailVO();
        vo.setEventId(event.getEventId());
        vo.setTitle(event.getTitle());
        vo.setEventType(event.getEventType());
        vo.setTargetNum(event.getTargetNum());
        vo.setCurrentNum(event.getCurrentNum());
        vo.setExpireMinutes(event.getExpireMinutes());
        vo.setStatus(event.getStatus());
        vo.setOwnerId(event.getUserId());
        
        // 获取发起者信息
        SysUser owner = userService.getUserById(event.getUserId());
        if (owner != null) {
            vo.setOwnerNickname(owner.getNickname());
            vo.setOwnerAvatar(owner.getAvatar());
            vo.setOwnerCreditScore(owner.getCreditScore());
        }
        
        // 解析扩展元数据
        vo.setExtMeta(parseExtMeta(event.getExtMeta()));
        if (vo.getExtMeta() != null && vo.getExtMeta().containsKey("description")) {
            vo.setDescription(String.valueOf(vo.getExtMeta().get("description")));
        }
        vo.setMediaUrls(extractMediaUrls(vo.getExtMeta()));
        
        // 解析参与者
        if (event.getParticipants() != null) {
            try {
                List<EventParticipantVO> participants = JSON.parseArray(event.getParticipants(), EventParticipantVO.class);
                vo.setParticipants(participants);
            } catch (Exception e) {
                vo.setParticipants(new ArrayList<>());
            }
        }
        
        vo.setCreateTime(event.getCreateTime());
        vo.setExpireTime(event.getExpireTime());
        vo.setSettleTime(event.getSettleTime());
        
        // 用户相关状态
        if (userId != null) {
            // 检查是否参与
            boolean isJoined = event.getUserId().equals(userId);
            if (!isJoined && event.getParticipants() != null) {
                isJoined = event.getParticipants().contains(userId.toString());
            }
            vo.setIsJoined(isJoined);
            vo.setIsFavorite(false); // 需要注入FavoriteService来查询
        }
        
        vo.setCommentCount(0);
        vo.setFavoriteCount(0);
        
        return vo;
    }
    
    @Override
    public List<EventHistoryVO> searchEvents(EventSearchDTO searchDTO) {
        QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isBlank()) {
            queryWrapper.like("title", searchDTO.getKeyword());
        }
        
        // 事件类型
        if (searchDTO.getEventType() != null && !searchDTO.getEventType().isBlank()) {
            queryWrapper.eq("event_type", searchDTO.getEventType());
        }
        
        // 事件状态
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isBlank()) {
            queryWrapper.eq("status", searchDTO.getStatus());
        }
        
        // 发起者
        if (searchDTO.getOwnerId() != null) {
            queryWrapper.eq("user_id", searchDTO.getOwnerId());
        }
        
        // 目标人数范围
        if (searchDTO.getMinTargetNum() != null) {
            queryWrapper.ge("target_num", searchDTO.getMinTargetNum());
        }
        if (searchDTO.getMaxTargetNum() != null) {
            queryWrapper.le("target_num", searchDTO.getMaxTargetNum());
        }
        
        // 时间范围
        if (searchDTO.getStartTime() != null && !searchDTO.getStartTime().isBlank()) {
            queryWrapper.ge("create_time", searchDTO.getStartTime());
        }
        if (searchDTO.getEndTime() != null && !searchDTO.getEndTime().isBlank()) {
            queryWrapper.le("create_time", searchDTO.getEndTime());
        }
        
        // 排序
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "create_time";
        boolean isAsc = "asc".equalsIgnoreCase(searchDTO.getSortOrder());
        queryWrapper.orderBy(true, isAsc, sortBy);
        
        // 分页
        Page<EventHistory> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        Page<EventHistory> result = eventHistoryMapper.selectPage(page, queryWrapper);
        
        List<EventHistoryVO> voList = new ArrayList<>();
        for (EventHistory event : result.getRecords()) {
            EventHistoryVO vo = new EventHistoryVO();
            vo.setEventId(event.getEventId());
            vo.setTitle(event.getTitle());
            vo.setEventType(event.getEventType());
            vo.setTargetNum(event.getTargetNum());
            vo.setCurrentNum(event.getCurrentNum());
            vo.setStatus(event.getStatus());
            vo.setCreateTime(event.getCreateTime());
            voList.add(vo);
        }
        
        return voList;
    }
    
    @Override
    public void cancelEvent(String eventId, Long userId) {
        // 检查事件是否存在且为活跃状态
        Map<Object, Object> eventInfo = eventCacheRepository.getEventInfo(eventId);
        
        if (eventInfo == null || eventInfo.isEmpty()) {
            throw new BusinessException("事件不存在或已结束");
        }
        
        Long ownerId = Long.valueOf(String.valueOf(eventInfo.get("owner")));
        if (!ownerId.equals(userId)) {
            throw new BusinessException("只有发起者可以取消事件");
        }
        
        String eventType = String.valueOf(eventInfo.get("event_type"));
        
        // 更新数据库状态
        UpdateWrapper<EventHistory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("event_id", eventId)
                .set("status", EventConstant.EVENT_STATUS_CANCELLED)
                .set("settle_time", LocalDateTime.now());
        eventHistoryMapper.update(null, updateWrapper);
        
        // 通知参与者
        Set<Object> participantsSet = eventCacheRepository.getParticipants(eventId);
        List<Long> participantIds = new ArrayList<>();
        for (Object obj : participantsSet) {
            Long participantId = Long.valueOf(String.valueOf(obj));
            if (!participantId.equals(userId)) {
                participantIds.add(participantId);
            }
        }
        
        String title = String.valueOf(eventInfo.get("title"));
        eventNotificationService.pushSettleNotify(participantIds, eventId, title, EventConstant.EVENT_STATUS_CANCELLED);
        
        // 清理Redis数据
        eventCacheRepository.cleanupEventData(eventId, eventType);
    }
    
    @Override
    public List<EventHistoryVO> getMyEvents(Long userId, String type, Integer pageNum, Integer pageSize) {
        List<EventHistory> events;
        
        // 根据类型查询不同的事件
        if ("created".equals(type)) {
            // 只查询发起的事件
            Page<EventHistory> page = new Page<>(pageNum, pageSize);
            QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).orderByDesc("create_time");
            events = eventHistoryMapper.selectPage(page, queryWrapper).getRecords();
        } else if ("joined".equals(type)) {
            // 只查询参与的事件（不包括自己发起的）
            events = eventHistoryMapper.findJoinedEvents(userId);
            // 手动分页
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, events.size());
            if (start < events.size()) {
                events = events.subList(start, end);
            } else {
                events = new ArrayList<>();
            }
        } else {
            // 查询所有事件（发起的 + 参与的）
            events = eventHistoryMapper.findAllUserEvents(userId);
            // 手动分页
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, events.size());
            if (start < events.size()) {
                events = events.subList(start, end);
            } else {
                events = new ArrayList<>();
            }
        }
        
        // 收集所有需要查询的用户ID
        Set<Long> userIds = new java.util.HashSet<>();
        for (EventHistory event : events) {
            userIds.add(event.getUserId());
            // 解析参与者
            if (event.getParticipants() != null && !event.getParticipants().isEmpty()) {
                try {
                    List<Map> participantList = JSON.parseArray(event.getParticipants(), Map.class);
                    for (Map p : participantList) {
                        Object uid = p.get("userId");
                        if (uid != null) {
                            userIds.add(Long.valueOf(String.valueOf(uid)));
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
        }
        
        // 批量查询用户信息
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
            userQuery.in("id", userIds);
            List<SysUser> users = sysUserMapper.selectList(userQuery);
            for (SysUser user : users) {
                userMap.put(user.getId(), user);
            }
        }
        
        // 封装VO
        List<EventHistoryVO> voList = new ArrayList<>();
        for (EventHistory event : events) {
            EventHistoryVO vo = new EventHistoryVO();
            vo.setEventId(event.getEventId());
            vo.setEventType(event.getEventType());
            vo.setTitle(event.getTitle());
            vo.setTargetNum(event.getTargetNum());
            vo.setCurrentNum(event.getCurrentNum());
            vo.setExpireMinutes(event.getExpireMinutes());
            vo.setStatus(event.getStatus());
            vo.setExtMeta(event.getExtMeta());
            vo.setCreateTime(event.getCreateTime());
            vo.setExpireTime(event.getExpireTime());
            vo.setSettleTime(event.getSettleTime());
            
            // 解析扩展信息
            Map<String, Object> metaMap = parseExtMeta(event.getExtMeta());
            vo.setDescription(metaMap.get("description") != null ? String.valueOf(metaMap.get("description")) : null);
            vo.setMediaUrls(extractMediaUrls(metaMap));
            
            // 提取位置信息
            if (metaMap.get("longitude") != null) {
                vo.setLongitude(Double.valueOf(String.valueOf(metaMap.get("longitude"))));
            }
            if (metaMap.get("latitude") != null) {
                vo.setLatitude(Double.valueOf(String.valueOf(metaMap.get("latitude"))));
            }
            if (metaMap.get("locationName") != null) {
                vo.setLocationName(String.valueOf(metaMap.get("locationName")));
            }
            
            // 设置发起者信息
            vo.setOwnerId(event.getUserId());
            vo.setIsOwner(event.getUserId().equals(userId));
            SysUser owner = userMap.get(event.getUserId());
            if (owner != null) {
                vo.setOwnerNickname(owner.getNickname());
                vo.setOwnerAvatar(owner.getAvatar());
            }
            
            // 解析参与者列表
            List<EventParticipantVO> participantVOList = new ArrayList<>();
            if (event.getParticipants() != null && !event.getParticipants().isEmpty()) {
                try {
                    List<Map> participantList = JSON.parseArray(event.getParticipants(), Map.class);
                    for (Map p : participantList) {
                        EventParticipantVO pvo = new EventParticipantVO();
                        Object uid = p.get("userId");
                        if (uid != null) {
                            Long participantId = Long.valueOf(String.valueOf(uid));
                            pvo.setUserId(participantId);
                            SysUser pUser = userMap.get(participantId);
                            if (pUser != null) {
                                pvo.setNickname(pUser.getNickname());
                                pvo.setAvatar(pUser.getAvatar());
                            } else if (p.get("nickname") != null) {
                                pvo.setNickname(String.valueOf(p.get("nickname")));
                            }
                            pvo.setOwner(Boolean.TRUE.equals(p.get("owner")));
                            pvo.setStatus(p.get("status") != null ? String.valueOf(p.get("status")) : null);
                            participantVOList.add(pvo);
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
            vo.setParticipantList(participantVOList);
            
            voList.add(vo);
        }
        
        return voList;
    }
}