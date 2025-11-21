package com.campus.service;

import java.util.List;
import com.campus.vo.CreditRecordVO;

/**
 * 信用分模块Service接口
 */
public interface CreditService {

    /**
     * 查询用户信用分变更记录
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 变更记录列表
     */
    List<CreditRecordVO> getCreditRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户最近N条信用分变更记录
     * @param userId 用户ID
     * @param limit 条数
     * @return 最近变更记录列表
     */
    List<CreditRecordVO> getRecentCreditRecords(Long userId, Integer limit);
}