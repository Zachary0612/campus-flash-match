package com.campus.dto.request;

import lombok.Data;

@Data
public class RegisterDTO {
    private String studentId;
    private String nickname;
    private String password;
}