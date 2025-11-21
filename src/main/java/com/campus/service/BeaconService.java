package com.campus.service;

import com.campus.dto.request.BeaconCreateDTO;

/**
 * 信标模块Service接口
 */
public interface BeaconService {

    /**
     * 发布占位信标
     * @param dto 信标发布请求参数
     * @param userId 发布者ID
     * @return 信标ID（即事件ID）
     */
    String publishBeacon(BeaconCreateDTO dto, Long userId);

    /**
     * 举报虚假信标
     * @param eventId 信标事件ID
     * @param reporterId 举报者ID
     */
    void reportFakeBeacon(String eventId, Long reporterId);
}