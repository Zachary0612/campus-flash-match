package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import jakarta.annotation.Resource;
import com.campus.util.WebSocketUtil;
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

    /**
     * 注册WebSocket端点导出器，支持@ServerEndpoint注解
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

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
                    
                    // 将token存储到属性中，后续可以在WebSocketHandler中获取
                    attributes.put("token", token);
                    System.out.println("WebSocket握手成功，token: " + token.substring(0, Math.min(20, token.length())) + "...");
                    return true;
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
        // 注册WebSocket处理器
        registry.addHandler(webSocketUtil, "/ws")
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