-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    avatar VARCHAR(255) DEFAULT 'default_avatar.png',
    password VARCHAR(255) NOT NULL,
    credit_score INTEGER DEFAULT 80,
    last_login_ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建校园点位表
CREATE TABLE IF NOT EXISTS campus_point (
    id BIGSERIAL PRIMARY KEY,
    point_name VARCHAR(100) NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    point_type VARCHAR(20) NOT NULL,
    is_valid BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建事件历史表
CREATE TABLE IF NOT EXISTS event_history (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    target_num INTEGER NOT NULL,
    current_num INTEGER NOT NULL,
    expire_minutes INTEGER,
    ext_meta JSONB,
    participants TEXT DEFAULT NULL,
    status VARCHAR(50) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    settle_time TIMESTAMP DEFAULT NULL,
    UNIQUE(event_id, user_id) -- 同一用户同一事件只能有一条记录
);

-- 创建信用分记录表
CREATE TABLE IF NOT EXISTS credit_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id VARCHAR(50),
    change_score INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);