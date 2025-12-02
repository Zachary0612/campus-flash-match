package com.campus.service;

import java.util.List;
import com.campus.dto.request.EventCreateDTO;
import com.campus.dto.request.EventSearchDTO;
import com.campus.dto.response.EventJoinResponseDTO;
import com.campus.dto.SettlementMsgDTO;
import com.campus.vo.CompletedEventVO;
import com.campus.vo.EventDetailVO;
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
     * 查询已完成事件，包含参与者信息
     * @param userId 用户ID（通常为发起者）
     * @return 已完成事件及其参与者列表
     */
    List<CompletedEventVO> getCompletedEvents(Long userId);

    /**
     * 事件结算（MQ消费者调用）
     * @param msgDTO 结算消息（事件ID、结算状态）
     */
    void settleEvent(SettlementMsgDTO msgDTO);
    
    /**
     * 获取事件详情
     * @param eventId 事件ID
     * @param userId 当前用户ID（用于判断是否已收藏/参与）
     * @return 事件详情
     */
    EventDetailVO getEventDetail(String eventId, Long userId);
    
    /**
     * 搜索事件
     * @param searchDTO 搜索条件
     * @return 事件列表
     */
    List<EventHistoryVO> searchEvents(EventSearchDTO searchDTO);
    
    /**
     * 取消事件（仅发起者可操作）
     * @param eventId 事件ID
     * @param userId 用户ID
     */
    void cancelEvent(String eventId, Long userId);
    
    /**
     * 查询用户的所有事件（发起的+参与的）
     * @param userId 用户ID
     * @param type 类型：all-全部, created-发起的, joined-参与的
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 事件历史列表（包含详细信息）
     */
    List<EventHistoryVO> getMyEvents(Long userId, String type, Integer pageNum, Integer pageSize);
}