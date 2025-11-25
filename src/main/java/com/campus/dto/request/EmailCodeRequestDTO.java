package com.campus.dto.request;

import lombok.Data;

/**
 * 请求验证码时的邮箱DTO
 */
@Data
public class EmailCodeRequestDTO {
    private String email;
}
