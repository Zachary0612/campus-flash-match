package com.campus.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 事件收藏表实体（对应event_favorite）
 */
@Data
@TableName("event_favorite")
public class EventFavorite {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
}
