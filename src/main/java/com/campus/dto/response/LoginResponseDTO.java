package com.campus.dto.response;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private Long userId;
    private String token;
    private String nickname;
    private Integer creditScore;
}