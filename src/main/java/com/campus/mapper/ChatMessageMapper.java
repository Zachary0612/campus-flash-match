package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 获取事件的聊天记录（最近100条）
     */
    @Select("SELECT * FROM chat_message WHERE event_id = #{eventId} ORDER BY create_time DESC LIMIT 100")
    List<ChatMessage> getRecentMessages(@Param("eventId") String eventId);
}
