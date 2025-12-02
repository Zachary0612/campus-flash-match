package com.campus.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步邮件发送服务
 * 单独抽出来是因为 @Async 在同一个类内部调用时不会生效
 */
@Slf4j
@Service
public class AsyncMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public AsyncMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationCodeAsync(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("校园闪聊匹配-注册验证码");
            message.setText(String.format("您的验证码是 %s ，有效期5分钟，请勿泄露。", code));
            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", email);
        } catch (Exception e) {
            log.error("验证码邮件发送失败: {}, 错误: {}", email, e.getMessage());
        }
    }
}
