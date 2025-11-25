package com.campus.constant;

/**
 * Redis常量类：定义Redis键前缀和配置
 */
public class RedisConstant {
    
    // 用户相关键前缀
    public static final String REDIS_KEY_USER_INFO = "user:info:";             // 用户信息缓存
    public static final String REDIS_KEY_USER_TOKEN = "user:token:";           // 用户登录Token
    public static final String REDIS_KEY_USER_LOCATION = "user:location:";     // 用户位置信息
    public static final String REDIS_KEY_USER_LOCATION_TEMP = "user:location:temp:"; // 临时位置缓存
    public static final String REDIS_KEY_USER_CREDIT = "user:credit:";         // 用户信用分
    public static final String REDIS_KEY_USER_ONLINE = "user:online:";         // 用户在线状态
    public static final String REDIS_KEY_EMAIL_VERIFY_CODE = "user:email:code:"; // 邮箱验证码
    public static final String REDIS_KEY_EMAIL_VERIFY_CODE_LIMIT = "user:email:code:limit:"; // 验证码发送频控
    
    // 事件相关键前缀
    public static final String REDIS_KEY_EVENT_LIST = "event:list:";           // 事件列表（按类型）
    public static final String REDIS_KEY_EVENT_GEO = "event:geo:";             // 事件地理位置索引
    public static final String REDIS_KEY_EVENT_PARTICIPANTS = "event:participants:"; // 事件参与者集合
    public static final String REDIS_KEY_EVENT_JOIN_LOCK = "event:join:lock:"; // 事件参与锁
    
    // 信标相关键前缀
    public static final String REDIS_KEY_BEACON_GEO = "beacon:geo:";           // 信标地理位置索引
    public static final String REDIS_KEY_BEACON_REPORT = "beacon:report:";     // 信标举报计数
    
    // 缓存过期时间（秒）
    public static final int EXPIRE_TIME_USER_TOKEN = 7 * 24 * 60 * 60;          // Token过期：7天
    public static final int EXPIRE_TIME_USER_LOCATION = 24 * 60 * 60;           // 位置缓存：24小时
    public static final int EXPIRE_TIME_TEMP_LOCATION = 60 * 60;               // 临时位置：1小时
    public static final int EXPIRE_TIME_EVENT_INFO = 2 * 60 * 60;              // 事件信息：2小时
    public static final int EXPIRE_TIME_JOIN_LOCK = 10;                         // 参与锁：10秒
    public static final int EXPIRE_TIME_RETRY_LOCK = 30;                        // 重试锁：30秒
    public static final int EXPIRE_TIME_EMAIL_CODE = 5 * 60;                    // 邮箱验证码：5分钟
    public static final int EMAIL_CODE_RESEND_INTERVAL = 60;                    // 验证码重发间隔：60秒
    
    // Geo相关配置
    public static final int GEO_RADIUS_MAX_DISTANCE = 1000;                    // 默认最大搜索半径（米）
    public static final int GEO_RADIUS_MAX_RESULTS = 50;                        // 默认最大返回结果数
    
    // 事务和锁配置
    public static final int LOCK_RETRY_TIMES = 3;                              // 锁重试次数
    public static final long LOCK_RETRY_DELAY = 100;                           // 锁重试延迟（毫秒）
    
    // 缓存刷新间隔
    public static final long CACHE_REFRESH_INTERVAL = 30 * 60 * 1000;           // 缓存刷新间隔：30分钟
}