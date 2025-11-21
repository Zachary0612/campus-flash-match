package com.campus.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:campus-flash-match-secret-key-with-at-least-64-characters-for-hs512-algorithm}")
    private String secret;
    
    @Value("${jwt.expire:86400000}")
    private long expiration;

    public String generateToken(Long userId) {
        try {
            // 确保密钥长度符合安全要求
            SecretKey key = getSigningKey();
            Date expirationDate = new Date(System.currentTimeMillis() + expiration);
            String token = Jwts.builder()
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(expirationDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
            System.out.println("Token生成成功");
            return token;
        } catch (Exception e) {
            System.err.println("Token生成失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Token生成失败", e);
        }
    }

    public Long validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return Long.parseLong(claims.getSubject());
        } catch (JwtException e) {
            throw new RuntimeException("JWT验证失败: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT声明为空", e);
        }
    }
    
    /**
     * 获取用于签名的密钥，确保符合安全要求
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        // 如果配置的密钥长度不足，使用它作为种子生成安全的密钥
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 64) {
            // 使用配置的密钥作为种子，生成足够长度的安全密钥（64字节用于HS512）
            byte[] secureBytes = new byte[64];
            System.arraycopy(keyBytes, 0, secureBytes, 0, Math.min(keyBytes.length, secureBytes.length));
            // 如果配置的密钥较短，用其重复填充
            for (int i = keyBytes.length; i < 64; i++) {
                secureBytes[i] = keyBytes[i % keyBytes.length];
            }
            return Keys.hmacShaKeyFor(secureBytes);
        } else if (keyBytes.length > 64) {
            // 超过64字节，截取前64字节
            byte[] truncatedBytes = new byte[64];
            System.arraycopy(keyBytes, 0, truncatedBytes, 0, 64);
            return Keys.hmacShaKeyFor(truncatedBytes);
        } else {
            // 正好64字节，直接使用
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }
}