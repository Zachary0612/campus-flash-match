package com.campus.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 事件历史表实体（对应event_history）
 */
@Data
@TableName("event_history")
public class EventHistory {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件ID（与Redis中事件ID一致）
     */
    private String eventId;

    /**
     * 发起者ID（关联sys_user.id）
     */
    private Long userId;

    /**
     * 事件类型（group_buy/beacon/meetup）
     */
    private String eventType;

    /**
     * 事件标题
     */
    private String title;

    /**
     * 目标人数
     */
    private Integer targetNum;
    
    /**
     * 当前人数
     */
    private Integer currentNum;
    
    /**
     * 过期分钟数
     */
    private Integer expireMinutes;
    
    /**
     * 扩展元数据（JSON格式）
     */
    private String extMeta;
    
    /**
     * 参与者ID数组
     */
    @JSONField(serialize = false)
    private Long[] participants;
    
    /**
     * 事件状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 结算时间
     */
    private LocalDateTime settleTime;
}