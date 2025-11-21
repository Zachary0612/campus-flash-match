package com.campus.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * MyBatis-Plus配置：分页插件
 */
@Configuration
@MapperScan("com.campus.mapper") // 再次指定Mapper扫描路径（确保生效）
public class MyBatisPlusConfig {

    /**
     * 配置分页插件（支持PageHelper风格分页）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor()); // 分页拦截器
        return interceptor;
    }
    
    /**
     * 配置MyBatis Plus的自动填充功能
     */
    /*@Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时不自动填充时间字段
            }
        };
    }*/
}