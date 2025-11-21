package com.campus.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.CreditRecord;
import com.campus.mapper.CreditRecordMapper;
import com.campus.vo.CreditRecordVO;
import com.campus.service.CreditService;

/**
 * 信用分模块Service实现类
 */
@Service
public class CreditServiceImpl implements CreditService {

    private final CreditRecordMapper creditRecordMapper;

    public CreditServiceImpl(CreditRecordMapper creditRecordMapper) {
        this.creditRecordMapper = creditRecordMapper;
    }

    @Override
    public List<CreditRecordVO> getCreditRecords(Long userId, Integer pageNum, Integer pageSize) {
        // 分页查询信用分变更记录
        Page<CreditRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CreditRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");
        Page<CreditRecord> recordPage = creditRecordMapper.selectPage(page, queryWrapper);

        // 封装VO
        List<CreditRecordVO> voList = new ArrayList<>();
        for (CreditRecord record : recordPage.getRecords()) {
            CreditRecordVO vo = convertToVO(record);
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public List<CreditRecordVO> getRecentCreditRecords(Long userId, Integer limit) {
        // 查询最近N条记录
        QueryWrapper<CreditRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time")
                .last("LIMIT " + limit);
        List<CreditRecord> recordList = creditRecordMapper.selectList(queryWrapper);

        // 封装VO
        List<CreditRecordVO> voList = new ArrayList<>();
        for (CreditRecord record : recordList) {
            CreditRecordVO vo = convertToVO(record);
            voList.add(vo);
        }

        return voList;
    }

    /**
     * 将CreditRecord实体转换为CreditRecordVO
     */
    private CreditRecordVO convertToVO(CreditRecord record) {
        CreditRecordVO vo = new CreditRecordVO();
        vo.setRecordId(record.getId());
        vo.setEventId(record.getEventId());
        vo.setChangeScore(record.getScoreChange());
        vo.setReason(record.getReason());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}