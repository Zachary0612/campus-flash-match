package com.campus.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 校园点位表实体（对应campus_point）
 */
@Data
@TableName("campus_point")
public class CampusPoint {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 点位名称（如"A1宿舍楼"）
     */
    private String pointName;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 点位类型（dorm/teaching/library/stadium）
     */
    private String pointType;

    /**
     * 是否有效（true=有效，false=无效）
     */
    private Boolean isValid;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}