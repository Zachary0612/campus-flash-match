package com.campus.service.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.constant.EventConstant;
import com.campus.entity.CreditRecord;
import com.campus.entity.EventHistory;
import com.campus.entity.SysUser;
import com.campus.mapper.CreditRecordMapper;
import com.campus.mapper.EventHistoryMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.repository.EventCacheRepository;
import com.campus.service.UserService;
import com.campus.vo.EventParticipantVO;
import com.alibaba.fastjson.JSON;

/**
 * 事件结算服务
 * 负责事件结算逻辑、信用分处理、历史记录更新
 */
@Service
public class EventSettlementService {

    @Autowired
    private EventCacheRepository eventCacheRepository;

    @Autowired
    private EventHistoryMapper eventHistoryMapper;

    @Autowired
    private CreditRecordMapper creditRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private EventNotificationService notificationService;

    /**
     * 结算事件（使用预先获取的Redis数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleEventWithData(String eventId, String settleStatus, Map<Object, Object> eventInfo, Set<Object> participantsSet) {
        System.out.println("SettleEventWithData Debug: Using pre-fetched Redis data for " + eventId);
        System.out.println("SettleEventWithData Debug: Participants from pre-fetched data: " + participantsSet);

        // 2. 解析事件数据
        Long ownerId = Long.valueOf(String.valueOf(eventInfo.get("owner")));
        String eventType = String.valueOf(eventInfo.get("event_type"));
        String title = String.valueOf(eventInfo.get("title"));
        Integer targetNum = Integer.valueOf(String.valueOf(eventInfo.get("target_num")));
        Integer currentNum = Integer.valueOf(String.valueOf(eventInfo.get("current_num")));
        Integer expireMinutes = Integer.valueOf(String.valueOf(eventInfo.get("expire_minutes")));
        String extMetaStr = String.valueOf(eventInfo.get("ext_meta"));
        LocalDateTime createTime = LocalDateTime.parse(String.valueOf(eventInfo.get("create_time")));
        LocalDateTime expireTime = LocalDateTime.parse(String.valueOf(eventInfo.get("expire_time")));

        // 3. 转换参与者列表
        List<Long> participantIds = new ArrayList<>();
        for (Object obj : participantsSet) {
            participantIds.add(Long.valueOf(String.valueOf(obj)));
        }

        // 4. 确定最终状态
        String finalStatus = determineFinalStatus(settleStatus, currentNum, targetNum);

        // 5. 更新事件历史记录
        updateEventHistory(eventId, ownerId, participantIds, finalStatus, currentNum);

        // 6. 处理信用分
        if (EventConstant.EVENT_STATUS_COMPLETED.equals(finalStatus)) {
            processCreditScores(eventId, ownerId, participantIds);
        }

        // 7. 推送结算通知
        notificationService.pushSettleNotify(participantIds, eventId, title, finalStatus);

        // 8. 清理Redis数据
        eventCacheRepository.cleanupEventData(eventId, eventType);
    }

    /**
     * 结算事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleEvent(String eventId, String settleStatus) {
        // 1. 获取事件数据
        Map<Object, Object> eventInfo = eventCacheRepository.getEventInfo(eventId);
        
        if (eventInfo == null) {
            System.out.println("SettleEvent Debug: eventInfo is null for " + eventId + ", handling missing data");
            handleMissingEventData(eventId, settleStatus);
            return;
        }

        Set<Object> participantsSet = eventCacheRepository.getParticipants(eventId);
        System.out.println("SettleEvent Debug: Participants from Redis for " + eventId + ": " + participantsSet);

        // 2. 解析事件数据
        Long ownerId = Long.valueOf(String.valueOf(eventInfo.get("owner")));
        String eventType = String.valueOf(eventInfo.get("event_type"));
        String title = String.valueOf(eventInfo.get("title"));
        Integer targetNum = Integer.valueOf(String.valueOf(eventInfo.get("target_num")));
        Integer currentNum = Integer.valueOf(String.valueOf(eventInfo.get("current_num")));
        Integer expireMinutes = Integer.valueOf(String.valueOf(eventInfo.get("expire_minutes")));
        String extMetaStr = String.valueOf(eventInfo.get("ext_meta"));
        LocalDateTime createTime = LocalDateTime.parse(String.valueOf(eventInfo.get("create_time")));
        LocalDateTime expireTime = LocalDateTime.parse(String.valueOf(eventInfo.get("expire_time")));

        // 3. 转换参与者列表
        List<Long> participantIds = new ArrayList<>();
        for (Object obj : participantsSet) {
            participantIds.add(Long.valueOf(String.valueOf(obj)));
        }

        // 4. 确定最终状态
        String finalStatus = determineFinalStatus(settleStatus, currentNum, targetNum);

        // 5. 更新事件历史记录
        updateEventHistory(eventId, ownerId, participantIds, finalStatus, currentNum);

        // 6. 处理信用分
        if (EventConstant.EVENT_STATUS_COMPLETED.equals(finalStatus)) {
            processCreditScores(eventId, ownerId, participantIds);
        }

        // 7. 推送结算通知
        notificationService.pushSettleNotify(participantIds, eventId, title, finalStatus);

        // 8. 清理Redis数据
        eventCacheRepository.cleanupEventData(eventId, eventType);
    }

    /**
     * 确定最终结算状态
     */
    private String determineFinalStatus(String settleStatus, Integer currentNum, Integer targetNum) {
        if (EventConstant.SETTLE_STATUS_SUCCESS.equals(settleStatus)) {
            return currentNum >= targetNum
                    ? EventConstant.EVENT_STATUS_COMPLETED
                    : EventConstant.EVENT_STATUS_EXPIRED;
        } else {
            return EventConstant.EVENT_STATUS_EXPIRED;
        }
    }

    /**
     * 更新事件历史记录
     */
    private void updateEventHistory(String eventId, Long ownerId, List<Long> participantIds, 
                                    String finalStatus, Integer currentNum) {
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!participantIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(participantIds);
            if (users != null) {
                userMap = users.stream()
                        .collect(Collectors.toMap(SysUser::getId, user -> user));
            }
        }

        LocalDateTime settleTimeNow = LocalDateTime.now();
        List<EventParticipantVO> participantDetails = new ArrayList<>();
        
        for (Long uid : participantIds) {
            EventParticipantVO detail = new EventParticipantVO();
            detail.setUserId(uid);
            SysUser sysUser = userMap.get(uid);
            detail.setNickname(sysUser != null ? sysUser.getNickname() : "用户" + uid);
            detail.setStatus(finalStatus);
            detail.setOwner(uid.equals(ownerId));
            detail.setJoinTime(null);
            detail.setSettleTime(settleTimeNow);
            participantDetails.add(detail);
        }

        String participantsJson = JSON.toJSONString(participantDetails);
        System.out.println("SettleEvent Debug: Generated participants JSON for " + eventId + ": " + participantsJson);
        
        // 使用实际参与者数量，而不是可能过时的 currentNum
        int actualParticipantCount = participantIds.size();
        
        UpdateWrapper<EventHistory> ownerUpdate = new UpdateWrapper<>();
        ownerUpdate.eq("event_id", eventId)
                .set("status", finalStatus)
                .set("current_num", actualParticipantCount)
                .set("settle_time", settleTimeNow)
                .set("participants", participantsJson);
        int rows = eventHistoryMapper.update(null, ownerUpdate);
        System.out.println("SettleEvent Debug: Updated event_history for " + eventId + ", rows affected: " + rows);
    }

    /**
     * 处理信用分（成功结算时）
     */
    private void processCreditScores(String eventId, Long ownerId, List<Long> participantIds) {
        // 发起者信用分+2
        CreditRecord ownerRecord = new CreditRecord();
        ownerRecord.setUserId(ownerId);
        ownerRecord.setEventId(eventId);
        ownerRecord.setReason("create_success");
        ownerRecord.setChangeScore(2);
        ownerRecord.setCreateTime(LocalDateTime.now());
        creditRecordMapper.insert(ownerRecord);
        userService.updateCredit(ownerId, 2);

        // 参与者信用分+1（排除发起者）
        for (Long userId : participantIds) {
            if (!ownerId.equals(userId)) {
                CreditRecord participantRecord = new CreditRecord();
                participantRecord.setUserId(userId);
                participantRecord.setEventId(eventId);
                participantRecord.setReason("join_success");
                participantRecord.setChangeScore(1);
                participantRecord.setCreateTime(LocalDateTime.now());
                creditRecordMapper.insert(participantRecord);
                userService.updateCredit(userId, 1);
            }
        }
    }

    /**
     * 处理Redis数据缺失的情况
     */
    private void handleMissingEventData(String eventId, String settleStatus) {
        String finalStatus = EventConstant.SETTLE_STATUS_SUCCESS.equals(settleStatus)
                ? EventConstant.EVENT_STATUS_COMPLETED
                : EventConstant.EVENT_STATUS_EXPIRED;

        UpdateWrapper<EventHistory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("event_id", eventId)
                .set("status", finalStatus)
                .set("settle_time", LocalDateTime.now());
        eventHistoryMapper.update(null, updateWrapper);
    }
}
