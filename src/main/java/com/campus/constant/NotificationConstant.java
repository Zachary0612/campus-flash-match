package com.campus.constant;

/**
 * 通知常量类：定义通知相关的常量和类型
 */
public class NotificationConstant {
    
    // 通知类型
    public static final String TYPE_EVENT_JOIN = "event_join";        // 有人加入事件
    public static final String TYPE_EVENT_FULL = "event_full";        // 事件满员
    public static final String TYPE_EVENT_SETTLE = "event_settle";    // 事件结算
    public static final String TYPE_FOLLOW = "follow";                // 被关注
    public static final String TYPE_COMMENT = "comment";              // 收到评论
    public static final String TYPE_RATING = "rating";                // 收到评价
    public static final String TYPE_SYSTEM = "system";                // 系统通知
    
    // 通知标题模板
    public static final String TITLE_EVENT_JOIN = "有人加入了你的事件";
    public static final String TITLE_EVENT_FULL = "事件已满员";
    public static final String TITLE_EVENT_SETTLE = "事件已结算";
    public static final String TITLE_NEW_FOLLOWER = "新增关注";
    public static final String TITLE_NEW_COMMENT = "收到评论";
    public static final String TITLE_NEW_RATING = "收到新评价";
    
    private NotificationConstant() {
        // 私有构造函数，防止实例化
    }
}
