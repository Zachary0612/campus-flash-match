package com.campus.controller;

import java.security.Principal;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.dto.ResultDTO;
import com.campus.dto.request.EventCreateDTO;
import com.campus.dto.request.EventSearchDTO;
import com.campus.dto.response.EventJoinResponseDTO;
import com.campus.service.EventService;
import com.campus.exception.BusinessException;
import com.campus.vo.CompletedEventVO;
import com.campus.vo.EventDetailVO;
import com.campus.vo.EventHistoryVO;
import com.campus.vo.NearbyEventVO;

/**
 * 事件模块Controller：发起、参与、退出、查询附近事件、查询历史
 */
@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * 1. 发起事件（拼单/约伴）
     */
    @PostMapping("/create")
    public ResultDTO<String> createEvent(@RequestBody EventCreateDTO dto, Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        // 调用Service发起事件
        String eventId = eventService.createEvent(dto, userId);
        return ResultDTO.success("事件发起成功", eventId);
    }

    /**
     * 2. 查询附近事件（1公里内）
     */
    @GetMapping("/nearby")
    public ResultDTO<List<NearbyEventVO>> getNearbyEvents(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false, defaultValue = "1000") double radius,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        List<NearbyEventVO> nearbyEvents = eventService.getNearbyEvents(eventType, userId, radius);
        return ResultDTO.success("查询成功", nearbyEvents);
    }

    /**
     * 3. 参与事件
     */
    @PostMapping("/join")
    public ResultDTO<EventJoinResponseDTO> joinEvent(
            @RequestParam String eventId,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        EventJoinResponseDTO response = eventService.joinEvent(eventId, userId);
        return ResultDTO.success("参与成功", response);
    }

    /**
     * 4. 退出事件（10分钟内）
     */
    @PostMapping("/quit")
    public ResultDTO<Void> quitEvent(
            @RequestParam String eventId,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        eventService.quitEvent(eventId, userId);
        return ResultDTO.success("退出成功");
    }

    /**
     * 5. 查询事件历史
     */
    @GetMapping("/history")
    public ResultDTO<List<EventHistoryVO>> getEventHistory(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        List<EventHistoryVO> historyList = eventService.getEventHistory(userId, pageNum, pageSize);
        return ResultDTO.success("查询成功", historyList);
    }

    /**
     * 6. 查询已完成事件（含参与者）
     */
    @GetMapping("/completed")
    public ResultDTO<List<CompletedEventVO>> getCompletedEvents(Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }

        Long userId = Long.valueOf(principal.getName());
        List<CompletedEventVO> completedEvents = eventService.getCompletedEvents(userId);
        return ResultDTO.success("查询成功", completedEvents);
    }

    /**
     * 7. 获取事件详情
     */
    @GetMapping("/detail")
    public ResultDTO<EventDetailVO> getEventDetail(
            @RequestParam String eventId,
            Principal principal) {
        Long userId = null;
        if (principal != null) {
            userId = Long.valueOf(principal.getName());
        }
        EventDetailVO detail = eventService.getEventDetail(eventId, userId);
        return ResultDTO.success("查询成功", detail);
    }

    /**
     * 8. 搜索事件
     */
    @PostMapping("/search")
    public ResultDTO<List<EventHistoryVO>> searchEvents(@RequestBody EventSearchDTO searchDTO) {
        List<EventHistoryVO> events = eventService.searchEvents(searchDTO);
        return ResultDTO.success("查询成功", events);
    }

    /**
     * 9. 取消事件（仅发起者可操作）
     */
    @PostMapping("/cancel")
    public ResultDTO<Void> cancelEvent(
            @RequestParam String eventId,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        eventService.cancelEvent(eventId, userId);
        return ResultDTO.success("事件已取消");
    }
    
    /**
     * 10. 查询我的事件（发起的+参与的）
     * @param type 类型：all-全部, created-我发起的, joined-我参与的
     */
    @GetMapping("/my-events")
    public ResultDTO<List<EventHistoryVO>> getMyEvents(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal) {
        if (principal == null) {
            throw new BusinessException("用户身份无效");
        }
        
        Long userId = Long.valueOf(principal.getName());
        List<EventHistoryVO> events = eventService.getMyEvents(userId, type, pageNum, pageSize);
        return ResultDTO.success("查询成功", events);
    }
}