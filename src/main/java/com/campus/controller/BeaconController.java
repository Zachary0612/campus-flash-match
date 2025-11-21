package com.campus.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.dto.ResultDTO;
import com.campus.dto.request.BeaconCreateDTO;
import com.campus.service.BeaconService;
import com.campus.exception.BusinessException;

/**
 * 信标模块Controller：发布信标、举报虚假信标
 */
@RestController
@RequestMapping("/api/beacon")
public class BeaconController {

    @Autowired
    private BeaconService beaconService;

    /**
     * 1. 发布占位信标
     */
    @PostMapping("/publish")
    public ResultDTO<String> publishBeacon(@RequestBody BeaconCreateDTO dto, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        String beaconId = beaconService.publishBeacon(dto, userId);
        return ResultDTO.success("信标发布成功", beaconId);
    }

    /**
     * 2. 举报虚假信标
     */
    @PostMapping("/report")
    public ResultDTO<Void> reportFakeBeacon(
            @RequestParam String eventId,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long reporterId = Long.valueOf(principal.getName());
        beaconService.reportFakeBeacon(eventId, reporterId);
        return ResultDTO.success("举报成功，已扣除发布者信用分");
    }
}