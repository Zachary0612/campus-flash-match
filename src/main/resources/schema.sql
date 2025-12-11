-- =====================================================
-- 校园闪聊匹配系统 - 完整数据库架构
-- 版本: 2.0
-- =====================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    avatar VARCHAR(255) DEFAULT 'default_avatar.png',
    password VARCHAR(255) NOT NULL,
    credit_score INTEGER DEFAULT 80,
    last_login_ip VARCHAR(50),
    bio VARCHAR(500),
    gender INTEGER DEFAULT 0,
    major VARCHAR(100),
    grade VARCHAR(50),
    interests VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 校园点位表
CREATE TABLE IF NOT EXISTS campus_point (
    id BIGSERIAL PRIMARY KEY,
    point_name VARCHAR(100) NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    point_type VARCHAR(20) NOT NULL,
    is_valid BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 事件历史表
CREATE TABLE IF NOT EXISTS event_history (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    target_num INTEGER NOT NULL,
    current_num INTEGER NOT NULL DEFAULT 1,
    expire_minutes INTEGER,
    ext_meta JSONB,
    participants TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    comment_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_time TIMESTAMP NOT NULL,
    settle_time TIMESTAMP,
    UNIQUE(event_id, user_id)
);

-- 4. 信用分记录表
CREATE TABLE IF NOT EXISTS credit_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id VARCHAR(50),
    change_score INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 通知消息表
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id VARCHAR(100),
    sender_id BIGINT,
    is_read INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 用户关注关系表
CREATE TABLE IF NOT EXISTS user_follow (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (follower_id, following_id)
);

-- 7. 事件评价表
CREATE TABLE IF NOT EXISTS event_rating (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    rater_id BIGINT NOT NULL,
    rated_user_id BIGINT NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    tags VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (event_id, rater_id, rated_user_id)
);

-- 8. 事件收藏表
CREATE TABLE IF NOT EXISTS event_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, event_id)
);

-- 9. 事件评论表
CREATE TABLE IF NOT EXISTS event_comment (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    reply_to_user_id BIGINT,
    like_count INTEGER DEFAULT 0,
    is_deleted INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
