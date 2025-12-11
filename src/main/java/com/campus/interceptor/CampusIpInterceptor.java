package com.campus.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class CampusIpInterceptor implements HandlerInterceptor {

    @Value("${campus.ip-prefixes}")
    private String campusIpPrefixes;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        System.out.println("客户端IP: " + clientIp + ", 允许的前缀: " + campusIpPrefixes);
        
        // 开发环境：允许本地访问
        if (clientIp.equals("127.0.0.1") || clientIp.equals("0:0:0:0:0:0:0:1") || clientIp.equals("localhost")) {
            System.out.println("本地开发环境，允许访问");
            return true;
        }
        
        // 校验IP前缀（支持多个前缀，用逗号分隔）
        String[] prefixes = campusIpPrefixes.split(",");
        boolean allowed = false;
        for (String prefix : prefixes) {
            if (clientIp.startsWith(prefix.trim())) {
                allowed = true;
                break;
            }
        }
        
        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            
            // 构造错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 403);
            errorResponse.put("message", "仅支持校园IP访问，当前IP: " + clientIp + "，允许的前缀: " + campusIpPrefixes);
            
            // 使用Jackson ObjectMapper序列化响应
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            
            response.getWriter().write(jsonResponse);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取真实客户端IP（考虑代理情况）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}