package com.campus.config;

import com.campus.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取请求路径
        String requestURI = request.getRequestURI();
        
        // 允许访问的公开接口
        if (requestURI.equals("/") || 
            requestURI.equals("/api") || 
            requestURI.equals("/api/") ||
            requestURI.equals("/health") ||
            requestURI.startsWith("/api/user/register") ||
            requestURI.startsWith("/api/user/login") ||
            requestURI.startsWith("/api/user/points") ||
            requestURI.startsWith("/ws/") ||
            requestURI.startsWith("/swagger-ui/") ||
            requestURI.startsWith("/v3/api-docs/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            try {
                Long userId = jwtUtil.validateToken(token);
                
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 将userId放入请求属性，便于后续Controller直接获取
                    request.setAttribute("userId", userId);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("JWT验证失败: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}