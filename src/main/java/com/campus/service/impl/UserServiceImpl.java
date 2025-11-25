package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.mapper.SysUserMapper;
import com.campus.mapper.CampusPointMapper;
import com.campus.mapper.CreditRecordMapper;
import com.campus.entity.SysUser;
import com.campus.entity.CampusPoint;
import com.campus.vo.CreditRecordVO;
import com.campus.entity.CreditRecord;
import com.campus.dto.response.LoginResponseDTO;
import com.campus.dto.response.CreditInfoResponseDTO;
import com.campus.service.UserService;
import com.campus.service.EmailService;
import com.campus.service.CreditService;
import com.campus.util.JwtUtil;
import com.campus.util.RedisUtil;
import com.campus.exception.BusinessException;
import com.campus.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户模块Service实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final CampusPointMapper campusPointMapper;
    private final CreditRecordMapper creditRecordMapper;
    private final CreditService creditService;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final EmailService emailService;

    // 校园IP前缀（配置文件读取）
    @Value("${campus.ip-prefix}")
    private String campusIpPrefix;

    // 密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserServiceImpl(SysUserMapper sysUserMapper,
                          CampusPointMapper campusPointMapper,
                          com.campus.mapper.CreditRecordMapper creditRecordMapper,
                          CreditService creditService,
                          JwtUtil jwtUtil,
                          RedisUtil redisUtil,
                          EmailService emailService) {
        this.sysUserMapper = sysUserMapper;
        this.campusPointMapper = campusPointMapper;
        this.creditRecordMapper = creditRecordMapper;
        this.creditService = creditService;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
        this.emailService = emailService;
    }

    @Override
    public String register(String studentId, String nickname, String password, String email, String verifyCode, String clientIp) {
        System.out.println("注册请求 - 学号: " + studentId + ", 昵称: " + nickname + ", 客户端IP: " + clientIp);

        if (email == null || email.isBlank()) {
            throw new BusinessException("邮箱不能为空");
        }
        if (verifyCode == null || verifyCode.isBlank()) {
            throw new BusinessException("验证码不能为空");
        }
        if (!emailService.validateVerificationCode(email, verifyCode)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 1. 校园IP校验（开发环境允许本地IP）
        boolean isLocalIp = clientIp.equals("127.0.0.1") || 
                           clientIp.equals("0:0:0:0:0:0:0:1") || 
                           clientIp.equals("localhost");
        
        if (!isLocalIp && !clientIp.startsWith(campusIpPrefix)) {
            throw new BusinessException("仅支持校园IP注册，当前IP: " + clientIp + "，要求前缀: " + campusIpPrefix);
        }
        
        if (isLocalIp) {
            System.out.println("本地开发环境，允许注册");
        }

        // 2. 校验学号是否已注册
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        SysUser existUser = sysUserMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException("学号已注册");
        }

        // 2.1 校验邮箱注册数量
        QueryWrapper<SysUser> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("email", email);
        Long emailCount = sysUserMapper.selectCount(emailWrapper);
        if (emailCount != null && emailCount >= 3) {
            throw new BusinessException("该邮箱最多可注册3个账号");
        }

        // 3. 密码加密
        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("密码加密完成");

        // 4. 保存用户信息
        SysUser user = new SysUser();
        user.setStudentId(studentId);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setCreditScore(80); // 初始信用分80
        user.setLastLoginIp(clientIp);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        System.out.println("用户信息保存完成，用户ID: " + user.getId());

        // 5. 生成Token
        try {
            String token = jwtUtil.generateToken(user.getId());
            System.out.println("Token生成完成: " + token);
            return token;
        } catch (Exception e) {
            System.err.println("Token生成异常: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("Token生成失败: " + e.getMessage());
        }
    }

    @Override
    public LoginResponseDTO login(String account, String password, String clientIp) {
        // 1. 查询用户（学号或邮箱）
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        if (account != null && account.contains("@")) {
            queryWrapper.eq("email", account);
        } else {
            queryWrapper.eq("student_id", account);
        }
        SysUser user = sysUserMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 2. 密码校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 3. 更新最后登录IP和时间
        user.setLastLoginIp(clientIp);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 4. 生成Token
        String token = jwtUtil.generateToken(user.getId());

        // 5. 缓存信用分到Redis（有效期2小时）
        redisUtil.setEx(
                RedisConstant.REDIS_KEY_USER_CREDIT + user.getId(),
                user.getCreditScore(),
                7200,
                TimeUnit.SECONDS
        );

        // 6. 封装响应
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getId());
        response.setToken(token);
        response.setNickname(user.getNickname());
        response.setCreditScore(user.getCreditScore());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void bindCampusPoint(Long userId, Long pointId) {
        // 1. 校验校园点位是否有效
        CampusPoint point = campusPointMapper.selectById(pointId);
        if (point == null || !point.getIsValid()) {
            throw new BusinessException("无效的校园点位");
        }

        // 2. 更新用户信息表中的位置信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 这里可以考虑在用户表中添加lastPointId字段来记录最近的位置
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 3. 更新用户位置到Redis GEO
        redisUtil.geoAdd(
                RedisConstant.REDIS_KEY_USER_LOCATION,
                point.getLongitude(),
                point.getLatitude(),
                userId.toString()
        );

        // 4. 缓存用户临时位置信息
        redisUtil.setEx(
                RedisConstant.REDIS_KEY_USER_LOCATION_TEMP + userId,
                pointId,
                RedisConstant.EXPIRE_TIME_TEMP_LOCATION,
                TimeUnit.SECONDS
        );

        // 5. 标记用户在线（有效期30分钟）
        redisUtil.setEx(
                RedisConstant.REDIS_KEY_USER_ONLINE + userId,
                1,
                RedisConstant.GEO_RADIUS_MAX_DISTANCE,
                TimeUnit.SECONDS
        );
    }

    @Override
    public CreditInfoResponseDTO getCreditInfo(Long userId) {
        // 1. 获取当前信用分（优先从Redis取）
        Integer creditScore = getCurrentCreditFromCacheOrDb(userId);

        // 2. 获取最近3条信用分变更记录
        List<CreditRecordVO> recentRecords = creditService.getRecentCreditRecords(userId, 3);

        // 3. 封装响应
        CreditInfoResponseDTO response = new CreditInfoResponseDTO();
        response.setUserId(userId);
        response.setCurrentCredit(creditScore);
        response.setRecentRecords(recentRecords);
        return response;
    }

    @Override
    public boolean checkCreditForCreate(Long userId) {
        Integer creditScore = getCurrentCreditFromCacheOrDb(userId);
        return creditScore >= 60; // 信用分≥60可发起事件
    }

    @Override
    public boolean checkCreditForJoin(Long userId) {
        Integer creditScore = getCurrentCreditFromCacheOrDb(userId);
        return creditScore >= 50; // 信用分≥50可参与事件
    }
    
    @Override
    public void updateCredit(Long userId, int changeValue) {
        // 获取当前信用分
        Integer currentCredit = getCurrentCreditFromCacheOrDb(userId);
        
        // 计算新信用分（确保不低于0）
        int newCredit = Math.max(0, currentCredit + changeValue);
        
        // 更新数据库
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setCreditScore(newCredit);
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.updateById(user);
        }
        
        // 更新Redis缓存
        redisUtil.setEx(
                RedisConstant.REDIS_KEY_USER_CREDIT + userId,
                newCredit,
                7200,
                TimeUnit.SECONDS
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCreditScore(Long userId, Integer changeScore, String eventId, String reason) {
        // 1. 更新数据库信用分（原子操作）
        int rows = sysUserMapper.updateCreditScore(userId, changeScore);
        if (rows == 0) {
            throw new BusinessException("用户不存在或信用分更新失败");
        }

        // 2. 查询更新后的信用分
        SysUser user = sysUserMapper.selectById(userId);
        Integer newCredit = user.getCreditScore();

        // 3. 更新Redis缓存
        redisUtil.setEx(
                RedisConstant.REDIS_KEY_USER_CREDIT + userId,
                newCredit,
                7200,
                TimeUnit.SECONDS
        );

        // 4. 记录信用分变更记录
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setEventId(eventId);
        record.setScoreChange(changeScore); // 使用正确的字段名
        record.setReason(reason);
        record.setCreateTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        creditRecordMapper.insert(record);
    }

    @Override
    public List<CampusPoint> getCampusPoints() {
        QueryWrapper<CampusPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_valid", true);
        return campusPointMapper.selectList(queryWrapper);
    }

    /**
     * 私有方法：获取用户当前信用分（优先从缓存获取，缓存未命中则查询数据库）
     */
    private Integer getCurrentCreditFromCacheOrDb(Long userId) {
        Integer creditScore = (Integer) redisUtil.get(RedisConstant.REDIS_KEY_USER_CREDIT + userId);
        if (creditScore == null) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            creditScore = user.getCreditScore();
            // 缓存到Redis
            redisUtil.setEx(
                    RedisConstant.REDIS_KEY_USER_CREDIT + userId,
                    creditScore,
                    7200,
                    TimeUnit.SECONDS
            );
        }
        return creditScore;
    }

    /**
     * 私有方法：获取用户当前信用分
     * @deprecated 请使用 {@link #getCurrentCreditFromCacheOrDb(Long)} 方法替代
     */
    @Deprecated
    private Integer getCurrentCredit(Long userId) {
        return getCurrentCreditFromCacheOrDb(userId);
    }
}