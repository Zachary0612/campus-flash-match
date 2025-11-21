package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 项目启动类
 */
@SpringBootApplication(scanBasePackages = "com.campus") // 扫描所有组件
@MapperScan("com.campus.mapper") // 扫描MyBatis Mapper接口
@EnableTransactionManagement // 开启事务管理
@EnableScheduling // 开启定时任务
public class CampusFlashMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusFlashMatchApplication.class, args);
    }
}