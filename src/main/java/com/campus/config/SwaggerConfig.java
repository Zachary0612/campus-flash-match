package com.campus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Campus Flash Match API Documentation")
                .description("校园闪聊匹配系统API接口文档")
                .contact(new Contact()
                        .name("Campus Team")
                        .email("campus@example.com")
                        .url("https://github.com"))
                .version("1.0");
    }
}