package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.EventRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 事件评价Mapper接口
 */
@Mapper
public interface EventRatingMapper extends BaseMapper<EventRating> {
    
    /**
     * 计算用户平均评分
     */
    @Select("SELECT COALESCE(AVG(score), 0) FROM event_rating WHERE rated_user_id = #{userId}")
    Double getAverageRating(@Param("userId") Long userId);
    
    /**
     * 统计用户收到的评价数
     */
    @Select("SELECT COUNT(*) FROM event_rating WHERE rated_user_id = #{userId}")
    Integer countRatings(@Param("userId") Long userId);
    
    /**
     * 检查是否已评价
     */
    @Select("SELECT COUNT(*) FROM event_rating WHERE event_id = #{eventId} AND rater_id = #{raterId} AND rated_user_id = #{ratedUserId}")
    Integer checkRated(@Param("eventId") String eventId, @Param("raterId") Long raterId, @Param("ratedUserId") Long ratedUserId);
}
