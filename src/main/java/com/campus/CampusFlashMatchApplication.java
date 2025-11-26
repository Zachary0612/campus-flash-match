package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 项目启动类
 */
@SpringBootApplication(scanBasePackages = "com.campus") // 扫描所有组件
@MapperScan("com.campus.mapper") // 扫描MyBatis Mapper接口
@EnableTransactionManagement // 开启事务管理
@EnableScheduling // 开启定时任务
@EnableAsync // 开启异步支持
public class CampusFlashMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusFlashMatchApplication.class, args);
    }
}

@Configuration
class WebMvcConfig implements WebMvcConfigurer {

    @Value("${campus.media.storage-path:./uploads/}")
    private String storagePath;

    @Value("${campus.media.url-prefix:/media/}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + storagePath;
        registry.addResourceHandler(urlPrefix + "**").addResourceLocations(location);
    }
}