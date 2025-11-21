package com.campus.service;

import java.util.List;
import com.campus.dto.request.EventCreateDTO;
import com.campus.dto.response.EventJoinResponseDTO;
import com.campus.dto.SettlementMsgDTO;
import com.campus.vo.EventHistoryVO;
import com.campus.vo.NearbyEventVO;

/**
 * 事件模块Service接口
 */
public interface EventService {

    /**
     * 发起事件（拼单/约伴）
     * @param dto 事件发起请求参数
     * @param userId 发起者ID
     * @return 事件ID
     */
    String createEvent(EventCreateDTO dto, Long userId);

    /**
     * 查询附近事件
     * @param eventType 事件类型（group_buy/meetup）
     * @param userId 用户ID（用于获取用户当前位置）
     * @param radius 搜索半径（米）
     * @return 附近事件列表
     */
    List<NearbyEventVO> getNearbyEvents(String eventType, Long userId, double radius);

    /**
     * 参与事件
     * @param eventId 事件ID
     * @param userId 参与者ID
     * @return 参与响应（当前人数、是否满员）
     */
    EventJoinResponseDTO joinEvent(String eventId, Long userId);

    /**
     * 退出事件（10分钟内）
     * @param eventId 事件ID
     * @param userId 参与者ID
     */
    void quitEvent(String eventId, Long userId);

    /**
     * 查询事件历史
     * @param userId 用户ID（发起者/参与者）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 事件历史列表
     */
    List<EventHistoryVO> getEventHistory(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 事件结算（MQ消费者调用）
     * @param msgDTO 结算消息（事件ID、结算状态）
     */
    void settleEvent(SettlementMsgDTO msgDTO);
}