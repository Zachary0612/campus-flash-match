package com.campus.service.impl;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.campus.constant.RedisConstant;
import com.campus.exception.BusinessException;
import com.campus.service.EmailService;
import com.campus.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisUtil redisUtil;
    private final AsyncMailService asyncMailService;

    @Autowired
    public EmailServiceImpl(RedisUtil redisUtil, AsyncMailService asyncMailService) {
        this.redisUtil = redisUtil;
        this.asyncMailService = asyncMailService;
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

        // 通过单独的服务类异步发送邮件，避免阻塞请求
        asyncMailService.sendVerificationCodeAsync(email, code);
    }

    @Override
    public boolean validateVerificationCode(String email, String code) {
        Object cached = redisUtil.get(RedisConstant.REDIS_KEY_EMAIL_VERIFY_CODE + email);
        if (cached == null) {
            return false;
        }
        // 只验证，不删除。删除操作由注册成功后调用 deleteVerificationCode
        return code != null && code.equalsIgnoreCase(cached.toString());
    }

    @Override
    public void deleteVerificationCode(String email) {
        redisUtil.delete(RedisConstant.REDIS_KEY_EMAIL_VERIFY_CODE + email);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
