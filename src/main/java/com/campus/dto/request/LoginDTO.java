package com.campus.dto.request;

import lombok.Data;

@Data
public class LoginDTO {
    private String account; // 学号或邮箱
    private String password;
}