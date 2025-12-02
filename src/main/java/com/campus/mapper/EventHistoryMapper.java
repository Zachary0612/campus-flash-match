package com.campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.EventHistory;

/**
 * 事件历史表Mapper（MyBatis-Plus）
 */
@Mapper
public interface EventHistoryMapper extends BaseMapper<EventHistory> {
    
    /**
     * 自定义插入方法，处理JSONB类型
     * @param eventHistory 事件历史记录
     */
    void insertEventHistory(EventHistory eventHistory);
    
    /**
     * 批量插入事件历史
     * @param historyList 事件历史列表
     */
    void batchInsert(@Param("list") List<EventHistory> historyList);
    
    /**
     * 统计用户发起的事件数
     */
    @Select("SELECT COUNT(*) FROM event_history WHERE user_id = #{userId}")
    Integer countCreatedEvents(@Param("userId") Long userId);
    
    /**
     * 统计用户参与的事件数（通过participants字段包含用户ID）
     */
    @Select("SELECT COUNT(*) FROM event_history WHERE participants LIKE CONCAT('%', #{userId}, '%') OR user_id = #{userId}")
    Integer countJoinedEvents(@Param("userId") Long userId);
    
    /**
     * 统计用户成功完成的事件数
     */
    @Select("SELECT COUNT(*) FROM event_history WHERE (participants LIKE CONCAT('%', #{userId}, '%') OR user_id = #{userId}) AND status = 'settled'")
    Integer countCompletedEvents(@Param("userId") Long userId);
    
    /**
     * 统计用户本月发起的事件数
     */
    @Select("SELECT COUNT(*) FROM event_history WHERE user_id = #{userId} AND create_time >= DATE_TRUNC('month', CURRENT_DATE)")
    Integer countMonthlyCreatedEvents(@Param("userId") Long userId);
    
    /**
     * 统计用户本月参与的事件数
     */
    @Select("SELECT COUNT(*) FROM event_history WHERE (participants LIKE CONCAT('%', #{userId}, '%') OR user_id = #{userId}) AND create_time >= DATE_TRUNC('month', CURRENT_DATE)")
    Integer countMonthlyJoinedEvents(@Param("userId") Long userId);
    
    /**
     * 查询用户参与的所有事件（包括发起的和加入的）
     * 通过 participants 字段中包含用户ID 或 user_id 等于用户ID
     */
    @Select("SELECT DISTINCT * FROM event_history WHERE " +
            "participants::text LIKE CONCAT('%\"userId\":', #{userId}, '%') OR " +
            "participants::text LIKE CONCAT('%\"userId\": ', #{userId}, '%') OR " +
            "participants::text LIKE CONCAT('%\"userId\":', #{userId}, ',%') OR " +
            "participants::text LIKE CONCAT('%\"userId\":', #{userId}, '}%') OR " +
            "user_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<EventHistory> findAllUserEvents(@Param("userId") Long userId);
    
    /**
     * 查询用户参与但非发起的事件
     */
    @Select("SELECT DISTINCT * FROM event_history WHERE (" +
            "participants::text LIKE CONCAT('%\"userId\":', #{userId}, '%') OR " +
            "participants::text LIKE CONCAT('%\"userId\": ', #{userId}, '%') OR " +
            "participants::text LIKE CONCAT('%\"userId\":', #{userId}, ',%') OR " +
            "participants::text LIKE CONCAT('%\"userId\":', #{userId}, '}%')" +
            ") AND user_id != #{userId} ORDER BY create_time DESC")
    List<EventHistory> findJoinedEvents(@Param("userId") Long userId);
    
    /**
     * 更新事件状态
     */
    @Update("UPDATE event_history SET status = #{status} WHERE event_id = #{eventId}")
    int updateEventStatus(@Param("eventId") String eventId, @Param("status") String status);
}