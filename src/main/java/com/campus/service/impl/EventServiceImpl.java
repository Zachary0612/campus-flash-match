package com.campus.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.SysUser;
import com.campus.entity.CampusPoint;
import com.campus.entity.EventHistory;
import com.campus.entity.CreditRecord;
import com.campus.mapper.SysUserMapper;
import com.campus.mapper.CampusPointMapper;
import com.campus.mapper.EventHistoryMapper;
import com.campus.mapper.CreditRecordMapper;
import com.campus.dto.request.EventCreateDTO;
import com.campus.dto.response.EventJoinResponseDTO;
import com.campus.dto.SettlementMsgDTO;
import com.campus.service.EventService;
import com.campus.service.UserService;
import com.campus.util.RedisUtil;
import com.campus.util.RabbitMqProducer;
import com.campus.util.WebSocketUtil;
import com.campus.exception.BusinessException;
import com.campus.constant.EventConstant;
import com.campus.constant.RedisConstant;
import com.alibaba.fastjson.JSON;
import com.campus.vo.EventHistoryVO;
import com.campus.vo.NearbyEventVO;

/**
 * 事件模块Service实现类
 */
@Service
public class EventServiceImpl implements EventService {

    private final SysUserMapper sysUserMapper;
    private final CampusPointMapper campusPointMapper;
    private final EventHistoryMapper eventHistoryMapper;
    private final CreditRecordMapper creditRecordMapper;
    private final UserService userService;
    private final RedisUtil redisUtil;
    private final RabbitMqProducer rabbitMqProducer;
    private final WebSocketUtil webSocketUtil;

    // 事件默认搜索半径（米）
    @Value("${campus.event.default-radius}")
    private double defaultRadius;

    // 事件退出有效时间（秒，10分钟）
    @Value("${campus.event.quit-expire}")
    private long quitExpire;

    @Autowired
    public EventServiceImpl(SysUserMapper sysUserMapper,
                          CampusPointMapper campusPointMapper,
                          EventHistoryMapper eventHistoryMapper,
                          com.campus.mapper.CreditRecordMapper creditRecordMapper,
                          UserService userService,
                          RedisUtil redisUtil,
                          RabbitMqProducer rabbitMqProducer,
                          WebSocketUtil webSocketUtil) {
        this.sysUserMapper = sysUserMapper;
        this.campusPointMapper = campusPointMapper;
        this.eventHistoryMapper = eventHistoryMapper;
        this.creditRecordMapper = creditRecordMapper;
        this.userService = userService;
        this.redisUtil = redisUtil;
        this.rabbitMqProducer = rabbitMqProducer;
        this.webSocketUtil = webSocketUtil;
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

        // 6. 组装Redis Key
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + dto.getEventType();
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;

        // 7. 存储事件到Redis
        try {
            // 7.1 存储事件位置（GEO）
            redisUtil.geoAdd(
                    eventLocationKey,
                    point.getLongitude(),
                    point.getLatitude(),
                    eventId
            );

            // 7.2 存储事件详情（Hash）- 所有数据转换为字符串格式存储
            redisUtil.hSet(eventInfoKey, "owner", userId.toString());
            redisUtil.hSet(eventInfoKey, "event_type", dto.getEventType());
            redisUtil.hSet(eventInfoKey, "title", dto.getTitle());
            redisUtil.hSet(eventInfoKey, "target_num", dto.getTargetNum().toString());
            redisUtil.hSet(eventInfoKey, "current_num", "1"); // 发起者默认参与
            redisUtil.hSet(eventInfoKey, "expire_minutes", dto.getExpireMinutes().toString());
            redisUtil.hSet(eventInfoKey, "ext_meta", JSON.toJSONString(dto.getExtMeta()));
            redisUtil.hSet(eventInfoKey, "create_time", createTime.toString());
            redisUtil.hSet(eventInfoKey, "expire_time", expireTime.toString());
            // 设置过期时间（不覆盖Hash结构）
            redisUtil.expire(eventInfoKey, expireSeconds, TimeUnit.SECONDS);

            // 7.3 存储参与者集合（Set）
            redisUtil.sAdd(eventParticipantsKey, userId.toString());
            redisUtil.expire(eventParticipantsKey, expireSeconds, TimeUnit.SECONDS);

                // 7.4 立即插入事件历史记录（解决"我的事件"显示问题）
            EventHistory eventHistory = new EventHistory();
            eventHistory.setEventId(eventId);
            eventHistory.setUserId(userId);
            eventHistory.setEventType(dto.getEventType());
            eventHistory.setTitle(dto.getTitle());
            eventHistory.setTargetNum(dto.getTargetNum());
            eventHistory.setCurrentNum(1); // 发起者默认参与
            eventHistory.setExpireMinutes(dto.getExpireMinutes());
            eventHistory.setExtMeta(JSON.toJSONString(dto.getExtMeta()));
            // participants字段使用数据库默认值NULL
            eventHistory.setStatus("active"); // 活跃状态
            eventHistory.setCreateTime(createTime);
            eventHistory.setExpireTime(expireTime);
            eventHistoryMapper.insertEventHistory(eventHistory);

            // 7.5 推送实时通知（1公里内在线用户）
            pushNearbyUserNotify(eventId, dto.getTitle(), point.getLongitude(), point.getLatitude());

        } catch (Exception e) {
            // 异常回滚：删除已存储的Redis数据
            redisUtil.delete(eventInfoKey);
            redisUtil.delete(eventParticipantsKey);
            redisUtil.geoRemove(eventLocationKey, eventId);
            throw new BusinessException("事件发起失败：" + e.getMessage());
        }

        return eventId;
    }

    @Override
    public List<NearbyEventVO> getNearbyEvents(String eventType, Long userId, double radius) {
        // 1. 获取用户当前位置（Redis GEO）
        List<Point> userPosList = redisUtil.geoPosition(
                RedisConstant.REDIS_KEY_USER_LOCATION,
                userId.toString()
        );
        if (userPosList == null || userPosList.isEmpty() || userPosList.get(0) == null) {
            throw new BusinessException("请先绑定校园位置");
        }
        Point userPoint = userPosList.get(0);
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
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        
        // 检查键是否存在，避免查询不存在的键
        if (!redisUtil.hasKey(eventLocationKey)) {
            return new ArrayList<>();
        }
        
        // 检查键的类型是否正确（应为ZSET，用于GEO操作）
        DataType keyType = redisUtil.type(eventLocationKey);
        if (keyType != DataType.ZSET) {
            System.err.println("Redis键类型不匹配，期望ZSET类型，实际类型: " + keyType + "，键名: " + eventLocationKey);
            return new ArrayList<>();
        }
        
        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults;
        try {
            geoResults = redisUtil.geoRadius(
                    eventLocationKey,
                    userLon,
                    userLat,
                    radius,
                    RedisGeoCommands.DistanceUnit.METERS
            );
        } catch (Exception e) {
            System.err.println("执行geoRadius操作时发生异常，键名: " + eventLocationKey + "，错误信息: " + e.getMessage());
            return new ArrayList<>();
        }

        // 过滤并封装结果
        List<NearbyEventVO> resultList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult : geoResults.getContent()) {
            String eventId = (String) geoResult.getContent().getName();
            double distance = geoResult.getDistance().getValue(); // 距离（米）

            // 校验事件是否存在（未过期）
            String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
            if (!redisUtil.hasKey(eventInfoKey)) {
                continue;
            }
            
            // 检查事件信息键的类型是否正确（应为HASH）
            DataType eventInfoKeyType = redisUtil.type(eventInfoKey);
            if (eventInfoKeyType != DataType.HASH) {
                System.err.println("Redis键类型不匹配，期望HASH类型，实际类型: " + eventInfoKeyType + "，键名: " + eventInfoKey);
                continue;
            }

            try {
                // 校验事件是否未满员
                Object currentNumObj = redisUtil.hGet(eventInfoKey, "current_num");
                Object targetNumObj = redisUtil.hGet(eventInfoKey, "target_num");
                
                // 检查获取的值是否为null
                if (currentNumObj == null || targetNumObj == null) {
                    continue;
                }
                
                String currentNumStr = String.valueOf(currentNumObj);
                String targetNumStr = String.valueOf(targetNumObj);
                Integer currentNum = Integer.valueOf(currentNumStr);
                Integer targetNum = Integer.valueOf(targetNumStr);
                if (currentNum >= targetNum) {
                    continue;
                }

                // 封装VO
                NearbyEventVO vo = new NearbyEventVO();
                vo.setEventId(eventId);
                vo.setEventType(eventType);
                Object titleObj = redisUtil.hGet(eventInfoKey, "title");
                vo.setTitle(titleObj != null ? String.valueOf(titleObj) : "");
                vo.setDistance((int) distance);
                vo.setCurrentNum(currentNum);
                vo.setTargetNum(targetNum);
                Object createTimeObj = redisUtil.hGet(eventInfoKey, "create_time");
                if (createTimeObj != null) {
                    vo.setCreateTime(LocalDateTime.parse(String.valueOf(createTimeObj)));
                }
                resultList.add(vo);
            } catch (Exception e) {
                // 如果在处理某个事件时发生异常，跳过该事件并记录日志
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

        // 2. 组装Redis Key
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;

        // 3. Redis事务处理并发（避免超员）
        final Boolean[] success = {false};
        final Map<Object, Object>[] eventInfoMap = new Map[1];
        redisUtil.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                // 3.1 监控事件详情Key
                operations.watch(eventInfoKey);

                // 3.2 校验事件状态
                if (!operations.hasKey(eventInfoKey)) {
                    // 不在SessionCallback内部抛出异常
                    success[0] = false;
                    return null;
                }

                // 3.3 获取事件信息
                eventInfoMap[0] = operations.opsForHash().entries(eventInfoKey);
                String eventType = (String) eventInfoMap[0].get("event_type");
                String currentNumStr = (String) eventInfoMap[0].get("current_num");
                String targetNumStr = (String) eventInfoMap[0].get("target_num");
                Integer currentNum = Integer.valueOf(currentNumStr);
                Integer targetNum = Integer.valueOf(targetNumStr);

                // 3.4 校验是否已参与
                if (operations.opsForSet().isMember(eventParticipantsKey, userId.toString())) {
                    // 不在SessionCallback内部抛出异常，设置success为false
                    success[0] = false;
                    return null;
                }

                // 3.5 校验是否超员
                if (currentNum >= targetNum) {
                    // 不在SessionCallback内部抛出异常，设置success为false
                    success[0] = false;
                    return null;
                }

                // 3.6 信标特殊校验（仅1人可参与）
                if (EventConstant.EVENT_TYPE_BEACON.equals(eventType) && currentNum >= 1) {
                    // 不在SessionCallback内部抛出异常，设置success为false
                    success[0] = false;
                    return null;
                }

                // 3.7 开启事务
                operations.multi();
                // 人数+1
                operations.opsForHash().increment(eventInfoKey, "current_num", 1);
                // 添加到参与者集合
                operations.opsForSet().add(eventParticipantsKey, userId.toString());

                // 3.8 执行事务
                List<Object> result = operations.exec();
                success[0] = (result != null && !result.isEmpty());
                return null;
            }
        });
        boolean isSuccess = success[0];

        if (!isSuccess) {
            // 可能是事件已过期或已结算，或者并发导致的失败
            if (!redisUtil.hasKey(eventInfoKey)) {
                throw new BusinessException("事件已过期或已结算");
            }
            throw new BusinessException("参与失败，请重试");
        }

        // 4. 获取更新后的人数
        Integer currentNum = (Integer) redisUtil.hGet(eventInfoKey, "current_num");
        Integer targetNum = (Integer) redisUtil.hGet(eventInfoKey, "target_num");
        boolean isFull = currentNum.equals(targetNum);

        // 5. 为参与者创建事件历史记录
        try {
            Map<Object, Object> eventInfo = redisUtil.hGetAll(eventInfoKey);
            EventHistory participantHistory = new EventHistory();
            participantHistory.setEventId(eventId);
            participantHistory.setUserId(userId);
            participantHistory.setEventType((String) eventInfo.get("event_type"));
            participantHistory.setTitle((String) eventInfo.get("title"));
            participantHistory.setTargetNum(Integer.valueOf((String) eventInfo.get("target_num")));
            participantHistory.setCurrentNum(currentNum);
            participantHistory.setExpireMinutes(Integer.valueOf((String) eventInfo.get("expire_minutes")));
            participantHistory.setExtMeta((String) eventInfo.get("ext_meta"));
            participantHistory.setStatus("active"); // 活跃状态
            participantHistory.setCreateTime(LocalDateTime.parse((String) eventInfo.get("create_time")));
            participantHistory.setExpireTime(LocalDateTime.parse((String) eventInfo.get("expire_time")));
            eventHistoryMapper.insertEventHistory(participantHistory);
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("创建参与者事件历史记录失败: " + e.getMessage());
        }

        // 6. 满员则发送结算消息到MQ
        if (isFull) {
            SettlementMsgDTO msgDTO = new SettlementMsgDTO();
            msgDTO.setEventId(eventId);
            msgDTO.setStatus(EventConstant.SETTLE_STATUS_SUCCESS);
            msgDTO.setSendTime(new Date());
            rabbitMqProducer.sendSettlementMsg(msgDTO);

            // 推送满员通知给所有参与者
            pushEventFullNotify(eventId, currentNum, targetNum);
        }

        // 6. 封装响应
        EventJoinResponseDTO response = new EventJoinResponseDTO();
        response.setCurrentParticipants(currentNum);
        response.setMaxParticipants(targetNum);
        return response;
    }

    @Override
    public void quitEvent(String eventId, Long userId) {
        // 1. 组装Redis Key
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;

        // 2. 校验事件是否存在
        if (!redisUtil.hasKey(eventInfoKey)) {
            throw new BusinessException("事件已过期或已结算");
        }

        // 3. 校验是否已参与
        if (!redisUtil.sIsMember(eventParticipantsKey, userId.toString())) {
            throw new BusinessException("你未参与该事件");
        }

        // 4. 校验是否为发起者（发起者不可退出）
        Long ownerId = Long.valueOf((String) redisUtil.hGet(eventInfoKey, "owner"));
        if (ownerId.equals(userId)) {
            throw new BusinessException("发起者不可退出，可选择取消事件");
        }

        // 5. 校验退出时间窗口（创建10分钟内可退出）
        String createTimeStr = (String) redisUtil.hGet(eventInfoKey, "create_time");
        LocalDateTime createTime = LocalDateTime.parse(createTimeStr);
        if (LocalDateTime.now().isAfter(createTime.plusSeconds(quitExpire))) {
            throw new BusinessException("超过退出时间窗口，不可退出");
        }

        // 6. Redis事务处理 - 使用更简单的实现避免类型问题
        final Boolean[] success = {false};
        redisUtil.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.watch(eventInfoKey);
                operations.watch(eventParticipantsKey);
                operations.multi();
                operations.opsForHash().increment(eventInfoKey, "current_num", -1);
                operations.opsForSet().remove(eventParticipantsKey, userId.toString());
                List<Object> result = operations.exec();
                success[0] = (result != null && !result.isEmpty());
                return null;
            }
        });
        boolean isSuccess = success[0];

        if (!isSuccess) {
            throw new BusinessException("退出失败，请重试");
        }

        // 7. 删除参与者的事件历史记录
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
            // 角色信息不包含在VO中，已移除
            vo.setStatus(history.getStatus());
            vo.setCreateTime(history.getCreateTime());
            voList.add(vo);
        }

        return voList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void settleEvent(SettlementMsgDTO msgDTO) {
        String eventId = msgDTO.getEventId();
        String settleStatus = msgDTO.getStatus();

        // 1. 组装Redis Key
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;

        // 2. 校验事件是否存在
        if (!redisUtil.hasKey(eventInfoKey)) {
            throw new BusinessException("事件数据不存在，无需结算");
        }

        // 3. 读取Redis中的事件数据
        Map<Object, Object> eventInfo = redisUtil.hGetAll(eventInfoKey);
        Set<Object> participantsSet = redisUtil.sMembers(eventParticipantsKey);

        // 4. 解析事件数据
        Long ownerId = Long.valueOf((String) eventInfo.get("owner"));
        String eventType = (String) eventInfo.get("event_type");
        String title = (String) eventInfo.get("title");
        Integer targetNum = Integer.valueOf((String) eventInfo.get("target_num"));
        Integer currentNum = Integer.valueOf((String) eventInfo.get("current_num"));
        Integer expireMinutes = Integer.valueOf((String) eventInfo.get("expire_minutes"));
        String extMetaStr = (String) eventInfo.get("ext_meta");
        // 修复时间类型转换，从Redis获取的数据通常是字符串
        LocalDateTime createTime = LocalDateTime.parse((String) eventInfo.get("create_time"));
        LocalDateTime expireTime = LocalDateTime.parse((String) eventInfo.get("expire_time"));

        // 5. 转换参与者数组
        List<Long> participantIds = new ArrayList<>();
        for (Object obj : participantsSet) {
            participantIds.add(Long.valueOf((String) obj));
        }

        // 6. 存储事件历史
        String finalStatus = settleStatus;
        if (EventConstant.SETTLE_STATUS_SUCCESS.equals(settleStatus) && currentNum < targetNum) {
            finalStatus = EventConstant.SETTLE_STATUS_FAILED_TIMEOUT; // 未达标则超时失败
        }

        // 6.1 更新现有事件历史记录的状态和结算时间
        for (Long userId : participantIds) {
            // 查询现有记录
            QueryWrapper<EventHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("event_id", eventId).eq("user_id", userId);
            EventHistory existingHistory = eventHistoryMapper.selectOne(queryWrapper);
            
            if (existingHistory != null) {
                // 更新现有记录
                existingHistory.setStatus(finalStatus);
                existingHistory.setCurrentNum(currentNum);
                existingHistory.setSettleTime(LocalDateTime.now());
                eventHistoryMapper.updateById(existingHistory);
            } else {
                // 如果不存在记录（异常情况），创建新记录
                EventHistory history = new EventHistory();
                history.setEventId(eventId);
                history.setUserId(userId);
                history.setEventType(eventType);
                history.setTitle(title);
                history.setTargetNum(targetNum);
                history.setCurrentNum(currentNum);
                history.setExpireMinutes(expireMinutes);
                history.setExtMeta(extMetaStr);
                history.setStatus(finalStatus);
                history.setCreateTime(createTime);
                history.setExpireTime(expireTime);
                history.setSettleTime(LocalDateTime.now());
                eventHistoryMapper.insertEventHistory(history);
            }
        }

        // 7. 信用分处理
        // 7.1 成功结算：发起者+2，参与者+1
        if (EventConstant.SETTLE_STATUS_SUCCESS.equals(finalStatus)) {
            // 发起者信用分+2
            CreditRecord ownerRecord = new CreditRecord();
            ownerRecord.setUserId(ownerId);
            ownerRecord.setEventId(eventId);
            ownerRecord.setReason("create_success");
            ownerRecord.setScoreChange(2);
            ownerRecord.setCreateTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            creditRecordMapper.insert(ownerRecord);
            userService.updateCredit(ownerId, 2);

            // 参与者信用分+1（排除发起者）
            for (Long userId : participantIds) {
                if (!ownerId.equals(userId)) {
                    CreditRecord participantRecord = new CreditRecord();
                    participantRecord.setUserId(userId);
                    participantRecord.setEventId(eventId);
                    participantRecord.setReason("join_success");
                    participantRecord.setScoreChange(1);
                    participantRecord.setCreateTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    creditRecordMapper.insert(participantRecord);
                    userService.updateCredit(userId, 1);
                }
            }
        }

        // 8. 推送结算通知
        pushSettleNotify(participantIds, eventId, title, finalStatus);

        // 9. 删除Redis数据
        String eventLocationKey = EventConstant.REDIS_KEY_EVENT_LOCATION + eventType;
        redisUtil.delete(eventInfoKey);
        redisUtil.delete(eventParticipantsKey);
        redisUtil.geoRemove(eventLocationKey, eventId);
    }

    /**
     * 私有方法：推送附近用户通知
     */
    private void pushNearbyUserNotify(String eventId, String title, double lon, double lat) {
        // 搜索1公里内的在线用户
        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = redisUtil.geoRadius(
                RedisConstant.REDIS_KEY_USER_LOCATION,
                lon,
                lat,
                1000, // 1公里
                RedisGeoCommands.DistanceUnit.METERS
        );

        if (geoResults != null && !geoResults.getContent().isEmpty()) {
            String notifyMsg = String.format(
                    "附近有新事件：%s（%s）",
                    title,
                    eventId
            );

            for (GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult : geoResults.getContent()) {
                String userIdStr = (String) geoResult.getContent().getName();
                try {
                    WebSocketUtil.sendMessage(Long.valueOf(userIdStr), notifyMsg);
                } catch (Exception e) {
                    // 忽略发送失败
                }
            }
        }
    }

    /**
     * 私有方法：推送事件满员通知
     */
    private void pushEventFullNotify(String eventId, Integer currentNum, Integer targetNum) {
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        Set<Object> participantsSet = redisUtil.sMembers(eventParticipantsKey);

        String notifyMsg = String.format(
                "事件【%s】已满员（%d/%d），等待结算！",
                eventId, currentNum, targetNum
        );

        for (Object obj : participantsSet) {
            Long userId = Long.valueOf((String) obj);
            try {
                WebSocketUtil.sendMessage(userId, notifyMsg);
            } catch (Exception e) {
                // 忽略发送失败
            }
        }
    }

    /**
     * 私有方法：推送结算结果通知
     */
    private void pushSettleNotify(List<Long> userIds, String eventId, String title, String status) {
        String notifyMsg = String.format(
                "事件【%s】已结算，状态：%s（%s）",
                title,
                EventConstant.SETTLE_STATUS_SUCCESS.equals(status) ? "成功" : "过期失败",
                eventId
        );

        for (Long userId : userIds) {
            try {
                WebSocketUtil.sendMessage(userId, notifyMsg);
            } catch (Exception e) {
                // 忽略发送失败
            }
        }
    }
}