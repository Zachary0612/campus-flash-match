package com.campus.controller;

import com.campus.dto.ResultDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResultDTO<String> home() {
        return ResultDTO.success("欢迎使用校园闪聊匹配系统API服务");
    }

    @GetMapping("/api")
    public ResultDTO<String> apiHome() {
        return ResultDTO.success("欢迎使用校园闪聊匹配系统API服务");
    }

    @GetMapping("/health")
    public ResultDTO<String> health() {
        return ResultDTO.success("服务运行正常");
    }
}