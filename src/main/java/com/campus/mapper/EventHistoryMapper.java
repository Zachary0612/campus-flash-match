package com.campus.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.EventHistory;

/**
 * 事件历史表Mapper（MyBatis-Plus）
 */
public interface EventHistoryMapper extends BaseMapper<EventHistory> {
    
    /**
     * 自定义插入方法，处理JSONB类型
     * @param eventHistory 事件历史记录
     */
    void insertEventHistory(EventHistory eventHistory);
    
    /**
     * 批量插入事件历史
     * @param historyList 事件历史列表
     */
    void batchInsert(@Param("list") List<EventHistory> historyList);
}