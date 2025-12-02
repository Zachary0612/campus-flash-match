package com.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.campus.interceptor.CampusIpInterceptor;

/**
 * Web配置：注册拦截器，配置拦截规则和CORS
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CampusIpInterceptor campusIpInterceptor;

    @Value("${campus.media.storage-path:./uploads/}")
    private String storagePath;

    /**
     * 配置静态资源映射 - 用于访问上传的媒体文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /media/** 请求映射到本地存储目录
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + storagePath);
    }

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