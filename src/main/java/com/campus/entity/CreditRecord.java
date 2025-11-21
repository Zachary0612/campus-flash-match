package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 信用分变更记录表实体（对应credit_record）
 */
@Data
@TableName("credit_record")
public class CreditRecord {
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
     * 变更原因
     */
    private String reason;
    
    /**
     * 分数变更值（正数为加，负数为减）
     */
    private Integer scoreChange;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createTime;
}