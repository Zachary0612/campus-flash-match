package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import jakarta.annotation.Resource;
import com.campus.util.WebSocketUtil;
import com.campus.util.JwtUtil;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private WebSocketUtil webSocketUtil;
    
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 握手拦截器，用于验证Token和获取用户信息
     */
    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                          org.springframework.web.socket.WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                try {
                    // 从请求参数中获取token
                    String query = request.getURI().getQuery();
                    String token = null;
                    if (query != null && query.contains("token=")) {
                        token = query.substring(query.indexOf("token=") + 6);
                        if (query.contains("&")) {
                            token = token.substring(0, token.indexOf("&"));
                        }
                    }
                    
                    // 如果没有token，拒绝连接
                    if (token == null || token.trim().isEmpty()) {
                        System.out.println("WebSocket握手失败：缺少token");
                        return false;
                    }
                    
                    // 验证token有效性
                    try {
                        // 1. 尝试从Token中解析用户ID
                        Long userId = null;
                        try {
                            userId = jwtUtil.validateToken(token);
                        } catch (Exception e) {
                            System.out.println("WebSocket握手：Token验证失败 - " + e.getMessage());
                        }

                        // 2. 如果Token验证失败或未获取到ID，尝试从路径获取（兼容旧逻辑）
                        if (userId == null) {
                            String path = request.getURI().getPath();
                            String[] pathParts = path.split("/");
                            if (pathParts.length > 0) {
                                try {
                                    // 尝试解析路径最后一段
                                    userId = Long.parseLong(pathParts[pathParts.length - 1]);
                                } catch (NumberFormatException e) {
                                    System.out.println("WebSocket握手：路径中未找到有效的用户ID");
                                }
                            }
                        }

                        // 3. 最终校验
                        if (userId == null) {
                            System.out.println("WebSocket握手失败：无法获取用户ID");
                            return false;
                        }
                        
                        // 将用户ID和token存储到属性中
                        attributes.put("userId", userId);
                        attributes.put("token", token);
                        
                        System.out.println("WebSocket握手成功，用户ID: " + userId);
                        return true;
                    } catch (Exception e) {
                        System.out.println("WebSocket握手校验过程异常: " + e.getMessage());
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("WebSocket握手异常: " + e.getMessage());
                    return false;
                }
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                     org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
                // 握手完成后的处理
                if (exception != null) {
                    System.out.println("WebSocket握手后异常: " + exception.getMessage());
                }
            }
        };
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，支持路径参数 /ws/{userId}
        registry.addHandler(webSocketUtil, "/ws/**")
                .setAllowedOrigins("*") // 允许所有跨域请求
                .addInterceptors(handshakeInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler());
                
        // 注册SockJS端点，作为WebSocket的备选方案
        registry.addHandler(webSocketUtil, "/sockjs/ws")
                .setAllowedOrigins("*")
                .addInterceptors(handshakeInterceptor())
                .withSockJS();
    }
}