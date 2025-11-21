package com.campus.service;

import java.util.List;
import com.campus.entity.CampusPoint;
import com.campus.dto.response.LoginResponseDTO;
import com.campus.dto.response.CreditInfoResponseDTO;

/**
 * 用户模块Service接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param studentId 学号
     * @param nickname 昵称
     * @param password 密码
     * @param clientIp 客户端IP（校园IP校验）
     * @return 登录Token
     */
    String register(String studentId, String nickname, String password, String clientIp);

    /**
     * 用户登录
     * @param studentId 学号
     * @param password 密码
     * @param clientIp 客户端IP
     * @return 登录响应（用户ID、Token、信用分）
     */
    LoginResponseDTO login(String studentId, String password, String clientIp);

    /**
     * 绑定校园点位
     * @param userId 用户ID
     * @param pointId 校园点位ID
     */
    void bindCampusPoint(Long userId, Long pointId);

    /**
     * 查询用户信用分信息
     * @param userId 用户ID
     * @return 信用分信息（当前分数+最近变更记录）
     */
    CreditInfoResponseDTO getCreditInfo(Long userId);

    /**
     * 校验用户信用分（是否允许发起事件）
     * @param userId 用户ID
     * @return true=允许，false=不允许
     */
    boolean checkCreditForCreate(Long userId);

    /**
     * 校验用户信用分（是否允许参与事件）
     * @param userId 用户ID
     * @return true=允许，false=不允许
     */
    boolean checkCreditForJoin(Long userId);

    /**
     * 更新用户信用分
     * @param userId 用户ID
     * @param changeValue 变更值（正数为加分，负数为减分）
     */
    void updateCredit(Long userId, int changeValue);
    
    /**
     * 更新用户信用分（带事件ID和原因）
     * @param userId 用户ID
     * @param changeScore 变更分数
     * @param eventId 事件ID
     * @param reason 变更原因
     */
    void updateCreditScore(Long userId, Integer changeScore, String eventId, String reason);
    
    /**
     * 获取校园点位列表
     * @return 校园点位列表
     */
    List<CampusPoint> getCampusPoints();
}