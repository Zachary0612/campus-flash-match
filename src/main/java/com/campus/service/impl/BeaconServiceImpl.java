package com.campus.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.campus.dto.request.BeaconCreateDTO;
import com.campus.dto.request.EventCreateDTO;
import com.campus.service.BeaconService;
import com.campus.service.EventService;
import com.campus.service.UserService;
import com.campus.util.RedisUtil;
import com.campus.exception.BusinessException;
import com.campus.constant.EventConstant;

/**
 * 信标模块Service实现类
 */
@Service
public class BeaconServiceImpl implements BeaconService {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String publishBeacon(BeaconCreateDTO dto, Long userId) {
        // 1. 校验信标位置描述
        if (dto.getLocationDesc() == null || dto.getLocationDesc().trim().isEmpty()) {
            throw new BusinessException("信标位置描述不能为空");
        }

        // 2. 封装为事件请求（信标类型，目标人数固定为1）
        EventCreateDTO eventDTO = new EventCreateDTO();
        eventDTO.setEventType(EventConstant.EVENT_TYPE_BEACON);
        eventDTO.setTitle("占位信标：" + dto.getLocationDesc());
        eventDTO.setTargetNum(1); // 信标仅1人可认领
        eventDTO.setExpireMinutes(dto.getExpireMinutes());
        eventDTO.setPointId(dto.getPointId());
        
        // 设置图片和描述
        if (dto.getMediaUrls() != null && !dto.getMediaUrls().isEmpty()) {
            eventDTO.setMediaUrls(dto.getMediaUrls());
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            eventDTO.setDescription(dto.getDescription());
        }

        // 3. 扩展信息：存储信标具体位置描述
        java.util.Map<String, Object> extMeta = new java.util.HashMap<>();
        extMeta.put("locationDesc", dto.getLocationDesc());
        eventDTO.setExtMeta(extMeta);

        // 4. 调用事件Service发布信标（本质是创建事件）
        return eventService.createEvent(eventDTO, userId);
    }

    @Override
    public void reportFakeBeacon(String eventId, Long reporterId) {
        // 1. 组装Redis Key
        String eventInfoKey = EventConstant.REDIS_KEY_EVENT_INFO + eventId;

        // 2. 校验事件是否存在
        if (!redisUtil.hasKey(eventInfoKey)) {
            throw new BusinessException("信标事件不存在或已过期");
        }

        // 3. 校验事件类型是否为信标
        String eventType = (String) redisUtil.hGet(eventInfoKey, "event_type");
        if (!EventConstant.EVENT_TYPE_BEACON.equals(eventType)) {
            throw new BusinessException("该事件不是信标类型，无法举报");
        }

        // 4. 获取信标发布者ID
        Long ownerId = Long.valueOf((String) redisUtil.hGet(eventInfoKey, "owner"));

        // 5. 校验举报者是否为参与者（仅参与者可举报）
        String eventParticipantsKey = EventConstant.REDIS_KEY_EVENT_PARTICIPANTS + eventId;
        if (!redisUtil.sIsMember(eventParticipantsKey, reporterId.toString())) {
            throw new BusinessException("仅参与该信标的用户可举报");
        }

        // 6. 扣减发布者信用分（虚假信标）
        userService.updateCreditScore(ownerId, -2, eventId, "发布虚假信标被举报");
    }
}