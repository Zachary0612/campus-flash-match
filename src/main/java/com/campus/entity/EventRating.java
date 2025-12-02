package com.campus.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 事件评价表实体（对应event_rating）
 */
@Data
@TableName("event_rating")
public class EventRating {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 评价者ID
     */
    private Long raterId;

    /**
     * 被评价者ID
     */
    private Long ratedUserId;

    /**
     * 评分（1-5星）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价标签（准时/友好/靠谱等，逗号分隔）
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
