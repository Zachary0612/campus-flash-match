package com.campus.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.dto.ResultDTO;
import com.campus.service.CreditService;
import com.campus.exception.BusinessException;
import com.campus.vo.CreditRecordVO;

/**
 * 信用分模块Controller：查询信用分变更记录
 */
@RestController
@RequestMapping("/api/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    /**
     * 查询信用分变更记录
     */
    @GetMapping("/records")
    public ResultDTO<List<CreditRecordVO>> getCreditRecords(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("用户身份无效");
        }
        List<CreditRecordVO> records = creditService.getCreditRecords(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", records);
    }
}