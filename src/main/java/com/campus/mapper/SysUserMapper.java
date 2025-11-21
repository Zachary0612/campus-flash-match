package com.campus.mapper;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.SysUser;

/**
 * 用户表Mapper（MyBatis-Plus）
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据学号查询用户
     * @param studentId 学号
     * @return 用户信息
     */
    SysUser selectByStudentId(@Param("studentId") String studentId);

    /**
     * 更新用户信用分（原子操作）
     * @param userId 用户ID
     * @param changeScore 变更分数（正数+，负数-）
     * @return 影响行数
     */
    int updateCreditScore(
            @Param("userId") Long userId,
            @Param("changeScore") Integer changeScore
    );
}