package com.campus.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.campus.dto.ResultDTO;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 全局异常处理器：统一处理各类异常，返回标准格式响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResultDTO<String> handleBusinessException(BusinessException e) {
        logger.warning("业务异常: " + e.getMessage());
        return ResultDTO.error(e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthException.class)
    public ResultDTO<String> handleAuthException(AuthException e) {
        logger.warning("认证异常: " + e.getMessage());
        return ResultDTO.error(e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ResultDTO<String> handleException(Exception e) {
        // 记录详细的异常信息到日志
        logger.log(Level.SEVERE, "服务器内部错误: " + e.getMessage(), e);
        // 打印异常堆栈用于调试
        e.printStackTrace();
        // 生产环境不应该返回详细的异常信息
        String message = "服务器内部错误，请稍后重试";
        return ResultDTO.error(message);
    }
}