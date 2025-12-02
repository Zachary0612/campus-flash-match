package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.EventFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 事件收藏Mapper接口
 */
@Mapper
public interface EventFavoriteMapper extends BaseMapper<EventFavorite> {
    
    /**
     * 检查是否已收藏
     */
    @Select("SELECT COUNT(*) FROM event_favorite WHERE user_id = #{userId} AND event_id = #{eventId}")
    Integer checkFavorite(@Param("userId") Long userId, @Param("eventId") String eventId);
    
    /**
     * 统计用户收藏数
     */
    @Select("SELECT COUNT(*) FROM event_favorite WHERE user_id = #{userId}")
    Integer countFavorites(@Param("userId") Long userId);
}
