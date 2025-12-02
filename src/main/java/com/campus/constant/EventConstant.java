package com.campus.constant;

/**
 * 事件常量类：定义事件相关的常量、状态、类型和消息队列配置
 */
public class EventConstant {
    // Redis Keys
    public static final String REDIS_KEY_EVENT_LOCATION = "event:location:";
    public static final String REDIS_KEY_EVENT_INFO = "event:info:";
    public static final String REDIS_KEY_EVENT_PARTICIPANTS = "event:participants:";

    // Event Status
    public static final String EVENT_STATUS_ACTIVE = "active";           // 事件状态：进行中
    public static final String EVENT_STATUS_PENDING_CONFIRM = "pending_confirm"; // 事件状态：待确认（满员后等待所有成员确认）
    public static final String EVENT_STATUS_COMPLETED = "settled";   // 事件状态：已完成
    public static final String EVENT_STATUS_CANCELLED = "cancelled";   // 事件状态：已取消
    public static final String EVENT_STATUS_EXPIRED = "expired";       // 事件状态：已过期
    
    // Redis Keys - Confirmation
    public static final String REDIS_KEY_EVENT_CONFIRMATIONS = "event:confirmations:"; // 已确认用户集合
    
    // Settlement Status
    public static final String SETTLE_STATUS_SUCCESS = "SUCCESS";          // 结算状态：成功
    public static final String SETTLE_STATUS_FAILED_TIMEOUT = "FAILED_TIMEOUT"; // 结算状态：超时失败
    public static final String SETTLE_STATUS_FAILED_PARTIAL = "FAILED_PARTIAL"; // 结算状态：部分失败

    // Event Types
    public static final String EVENT_TYPE_GROUP_BUY = "group_buy";      // 事件类型：拼单
    public static final String EVENT_TYPE_MEETUP = "meetup";           // 事件类型：约伴
    public static final String EVENT_TYPE_BEACON = "beacon";           // 事件类型：信标

    // MQ Constants - Settlement
    public static final String MQ_EXCHANGE_EVENT_SETTLEMENT = "event.settlement.exchange"; // 兼容旧配置
    public static final String MQ_EXCHANGE_SETTLEMENT = "event.settlement.exchange";      // 结算交换机
    public static final String MQ_QUEUE_EVENT_SETTLEMENT = "event.settlement.queue";      // 结算队列
    public static final String MQ_ROUTING_KEY_EVENT_SETTLEMENT = "event.settlement.routing.key"; // 结算路由键
    
    // MQ Constants - Notification
    public static final String MQ_EXCHANGE_NOTIFICATION = "notification.exchange";        // 通知交换机
    public static final String MQ_QUEUE_NOTIFICATION = "notification.queue";             // 通知队列
    public static final String MQ_ROUTING_KEY_NOTIFICATION = "notification";             // 通知路由键
    
    // MQ Constants - Dead Letter
    public static final String MQ_EXCHANGE_DEAD_LETTER = "dead.letter.exchange";          // 死信交换机
    public static final String MQ_QUEUE_DEAD_LETTER = "dead.letter.queue";               // 死信队列
    public static final String MQ_ROUTING_KEY_DEAD_LETTER = "dead.letter.routing.key";   // 死信路由键
    
    // Default Values
    public static final int DEFAULT_EVENT_RADIUS = 1000;                // 默认搜索半径（米）
    public static final int DEFAULT_EVENT_MAX_PARTICIPANTS = 10;        // 默认最大参与人数
    public static final long EVENT_CANCEL_WINDOW = 10 * 60 * 1000;      // 退出窗口（10分钟）
    public static final int DEFAULT_CREDIT_SCORE = 80;                 // 默认信用分
    public static final int MIN_CREDIT_TO_PARTICIPATE = 60;             // 参与事件的最低信用分
}