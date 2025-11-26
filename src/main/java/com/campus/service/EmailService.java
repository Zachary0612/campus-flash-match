package com.campus.service;

public interface EmailService {
    /**
     * 发送注册邮箱验证码
     * @param email 目标邮箱
     */
    void sendVerificationCode(String email);

    /**
     * 校验验证码是否匹配（不删除验证码）
     * @param email 邮箱
     * @param code  用户输入验证码
     * @return true=校验通过
     */
    boolean validateVerificationCode(String email, String code);

    /**
     * 删除验证码（注册成功后调用）
     * @param email 邮箱
     */
    void deleteVerificationCode(String email);
}
