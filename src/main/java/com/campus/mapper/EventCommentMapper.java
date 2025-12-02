package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.EventComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 事件评论Mapper接口
 */
@Mapper
public interface EventCommentMapper extends BaseMapper<EventComment> {
    
    /**
     * 统计事件评论数
     */
    @Select("SELECT COUNT(*) FROM event_comment WHERE event_id = #{eventId} AND is_deleted = 0")
    Integer countComments(@Param("eventId") String eventId);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE event_comment SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLike(@Param("id") Long id);
    
    /**
     * 软删除评论
     */
    @Update("UPDATE event_comment SET is_deleted = 1 WHERE id = #{id} AND user_id = #{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);
}
