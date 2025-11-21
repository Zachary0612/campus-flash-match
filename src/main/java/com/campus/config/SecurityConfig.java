package com.campus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 禁用默认的表单登录，使用 JWT 进行身份验证
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 禁用 CSRF（因为使用 JWT）
            .csrf(csrf -> csrf.disable())
            
            // 禁用默认的表单登录
            .formLogin(form -> form.disable())
            
            // 禁用 HTTP Basic 认证
            .httpBasic(basic -> basic.disable())
            
            // 禁用默认的登出功能
            .logout(logout -> logout.disable())
            
            // 设置 Session 管理为无状态（使用 JWT）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许所有人访问的接口
                .requestMatchers(
                    "/",
                    "/api",
                    "/api/",
                    "/health",
                    "/api/user/register",
                    "/api/user/login",
                    "/api/user/points",  // 校园点位列表
                    "/error",
                    "/ws/**",  // WebSocket 端点
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}