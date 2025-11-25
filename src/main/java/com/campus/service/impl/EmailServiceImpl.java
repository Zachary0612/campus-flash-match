package com.campus.service.impl;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.campus.constant.RedisConstant;
import com.campus.exception.BusinessException;
import com.campus.service.EmailService;
import com.campus.util.RedisUtil;

@Service
public class EmailServiceImpl implements EmailService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, RedisUtil redisUtil) {
        this.mailSender = mailSender;
        this.redisUtil = redisUtil;
    }

    @Override
    public void sendVerificationCode(String email) {
        String limitKey = RedisConstant.REDIS_KEY_EMAIL_VERIFY_CODE_LIMIT + email;
        if (Boolean.TRUE.equals(redisUtil.hasKey(limitKey))) {
            throw new BusinessException("验证码发送过于频繁，请稍后重试");
        }

        String code = generateCode();
        redisUtil.setEx(
                RedisConstant.REDIS_KEY_EMAIL_VERIFY_CODE + email,
                code,
                RedisConstant.EXPIRE_TIME_EMAIL_CODE,
                TimeUnit.SECONDS
        );
        redisUtil.setEx(limitKey, 1, RedisConstant.EMAIL_CODE_RESEND_INTERVAL, TimeUnit.SECONDS);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("校园闪聊匹配-注册验证码");
        message.setText(String.format("您的验证码是 %s ，有效期5分钟，请勿泄露。", code));
        mailSender.send(message);
    }

    @Override
    public boolean validateVerificationCode(String email, String code) {
        Object cached = redisUtil.get(RedisConstant.REDIS_KEY_EMAIL_VERIFY_CODE + email);
        if (cached == null) {
            return false;
        }
        boolean match = code != null && code.equalsIgnoreCase(cached.toString());
        if (match) {
            redisUtil.delete(RedisConstant.REDIS_KEY_EMAIL_VERIFY_CODE + email);
        }
        return match;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
