package com.campus.controller;

import java.security.Principal;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.dto.ResultDTO;
import com.campus.dto.request.LoginDTO;
import com.campus.dto.request.RegisterDTO;
import com.campus.dto.request.EmailCodeRequestDTO;
import com.campus.dto.response.LoginResponseDTO;
import com.campus.dto.response.CreditInfoResponseDTO;
import com.campus.entity.CampusPoint;
import com.campus.service.UserService;
import com.campus.service.EmailService;
import com.campus.exception.BusinessException;
import com.campus.util.JwtUtil;

/**
 * 用户模块Controller：注册、登录、位置绑定、信用分查询
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    /**
     * 根路径，用于检查服务状态
     */
    @GetMapping("/")
    public ResultDTO<String> index() {
        return ResultDTO.success("校园闪聊匹配系统后端服务运行正常");
    }

    /**
     * 1. 校园用户注册（需校园IP校验）
     */
    @PostMapping("/register")
    public ResultDTO<String> register(@RequestBody RegisterDTO dto, HttpServletRequest request) {
        // 获取客户端IP
        String clientIp = request.getRemoteAddr();
        // 调用Service注册
        String token = userService.register(
                dto.getStudentId(),
                dto.getNickname(),
                dto.getPassword(),
                dto.getEmail(),
                dto.getVerifyCode(),
                clientIp
        );
        return ResultDTO.success("注册成功", token);
    }

    /**
     * 1.1 发送邮箱验证码
     */
    @PostMapping("/email-code")
    public ResultDTO<Void> sendEmailCode(@RequestBody EmailCodeRequestDTO dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BusinessException("邮箱不能为空");
        }
        emailService.sendVerificationCode(dto.getEmail());
        return ResultDTO.success("验证码发送成功");
    }

    /**
     * 2. 用户登录
     */
    @PostMapping("/login")
    public ResultDTO<LoginResponseDTO> login(@RequestBody LoginDTO dto, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        LoginResponseDTO response = userService.login(dto.getAccount(), dto.getPassword(), clientIp);
        return ResultDTO.success("登录成功", response);
    }

    /**
     * 3. 绑定/切换校园位置
     */
    @PutMapping("/location")
    public ResultDTO<Void> bindLocation(@RequestParam Long pointId, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        // 调用Service绑定位置
        userService.bindCampusPoint(userId, pointId);
        return ResultDTO.success("位置绑定成功");
    }

    /**
     * 4. 查询信用分及变更记录
     */
    @GetMapping("/credit")
    public ResultDTO<CreditInfoResponseDTO> getCreditInfo(Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        CreditInfoResponseDTO creditInfo = userService.getCreditInfo(userId);
        return ResultDTO.success("查询成功", creditInfo);
    }
    
    /**
     * 5. 获取校园点位列表
     */
    @GetMapping("/points")
    public ResultDTO<List<CampusPoint>> getCampusPoints() {
        List<CampusPoint> points = userService.getCampusPoints();
        return ResultDTO.success("查询成功", points);
    }
}