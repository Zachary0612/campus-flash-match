package com.campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.campus.interceptor.CampusIpInterceptor;

/**
 * Web配置：注册拦截器，配置拦截规则和CORS
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CampusIpInterceptor campusIpInterceptor;

    /**
     * 配置CORS跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册拦截器，配置拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 校园IP拦截器：仅拦截注册接口
        registry.addInterceptor(campusIpInterceptor)
                .addPathPatterns("/api/user/register");
    }
}