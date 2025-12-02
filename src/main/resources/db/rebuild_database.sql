-- =====================================================
-- 校园闪聊匹配系统 - 数据库重建脚本
-- 警告：此脚本会删除所有现有数据！
-- 执行前请确保已备份重要数据
-- =====================================================

-- 1. 删除所有现有表（按依赖顺序）
DROP TABLE IF EXISTS event_comment CASCADE;
DROP TABLE IF EXISTS event_favorite CASCADE;
DROP TABLE IF EXISTS event_rating CASCADE;
DROP TABLE IF EXISTS user_follow CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS credit_record CASCADE;
DROP TABLE IF EXISTS event_history CASCADE;
DROP TABLE IF EXISTS campus_point CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;

-- =====================================================
-- 2. 创建用户表
-- =====================================================
CREATE TABLE sys_user (
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

CREATE INDEX idx_user_student_id ON sys_user(student_id);
CREATE INDEX idx_user_email ON sys_user(email);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.student_id IS '学号（唯一）';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.avatar IS '头像路径';
COMMENT ON COLUMN sys_user.password IS '密码（加密）';
COMMENT ON COLUMN sys_user.credit_score IS '信用分（默认80）';
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.bio IS '个人简介';
COMMENT ON COLUMN sys_user.gender IS '性别（0-未知，1-男，2-女）';
COMMENT ON COLUMN sys_user.major IS '专业';
COMMENT ON COLUMN sys_user.grade IS '年级';
COMMENT ON COLUMN sys_user.interests IS '兴趣标签（逗号分隔）';

-- =====================================================
-- 3. 创建校园点位表
-- =====================================================
CREATE TABLE campus_point (
    id BIGSERIAL PRIMARY KEY,
    point_name VARCHAR(100) NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    point_type VARCHAR(20) NOT NULL,
    is_valid BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campus_point_type ON campus_point(point_type);
CREATE INDEX idx_campus_point_valid ON campus_point(is_valid);

COMMENT ON TABLE campus_point IS '校园点位表';
COMMENT ON COLUMN campus_point.point_name IS '点位名称';
COMMENT ON COLUMN campus_point.longitude IS '经度';
COMMENT ON COLUMN campus_point.latitude IS '纬度';
COMMENT ON COLUMN campus_point.point_type IS '点位类型（teaching/library/dining/dorm/gate/office等）';
COMMENT ON COLUMN campus_point.is_valid IS '是否有效';

-- =====================================================
-- 4. 创建事件历史表
-- =====================================================
CREATE TABLE event_history (
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
    CONSTRAINT fk_event_history_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_event_user UNIQUE (event_id, user_id)
);

CREATE INDEX idx_event_history_event_id ON event_history(event_id);
CREATE INDEX idx_event_history_user_id ON event_history(user_id);
CREATE INDEX idx_event_history_status ON event_history(status);
CREATE INDEX idx_event_history_event_type ON event_history(event_type);
CREATE INDEX idx_event_history_create_time ON event_history(create_time DESC);

COMMENT ON TABLE event_history IS '事件历史表';
COMMENT ON COLUMN event_history.event_id IS '事件唯一ID';
COMMENT ON COLUMN event_history.user_id IS '发起者用户ID';
COMMENT ON COLUMN event_history.event_type IS '事件类型（group_buy/meetup/beacon）';
COMMENT ON COLUMN event_history.title IS '事件标题';
COMMENT ON COLUMN event_history.target_num IS '目标人数';
COMMENT ON COLUMN event_history.current_num IS '当前人数';
COMMENT ON COLUMN event_history.expire_minutes IS '过期时间（分钟）';
COMMENT ON COLUMN event_history.ext_meta IS '扩展信息（JSON格式）';
COMMENT ON COLUMN event_history.participants IS '参与者信息（JSON数组）';
COMMENT ON COLUMN event_history.status IS '状态（active/settled/expired/cancelled）';
COMMENT ON COLUMN event_history.comment_count IS '评论数';
COMMENT ON COLUMN event_history.favorite_count IS '收藏数';
COMMENT ON COLUMN event_history.expire_time IS '过期时间';
COMMENT ON COLUMN event_history.settle_time IS '结算时间';

-- =====================================================
-- 5. 创建信用分记录表
-- =====================================================
CREATE TABLE credit_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id VARCHAR(50),
    change_score INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_credit_record_user_id ON credit_record(user_id);
CREATE INDEX idx_credit_record_event_id ON credit_record(event_id);
CREATE INDEX idx_credit_record_create_time ON credit_record(create_time DESC);

COMMENT ON TABLE credit_record IS '信用分变更记录表';
COMMENT ON COLUMN credit_record.user_id IS '用户ID';
COMMENT ON COLUMN credit_record.event_id IS '关联事件ID';
COMMENT ON COLUMN credit_record.change_score IS '分数变更值（正数加分，负数减分）';
COMMENT ON COLUMN credit_record.reason IS '变更原因';

-- =====================================================
-- 6. 创建通知消息表
-- =====================================================
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id VARCHAR(100),
    sender_id BIGINT,
    is_read INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_sender FOREIGN KEY (sender_id) REFERENCES sys_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_type ON notification(type);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_create_time ON notification(create_time DESC);

COMMENT ON TABLE notification IS '通知消息表';
COMMENT ON COLUMN notification.type IS '通知类型（event_join/event_full/event_settle/follow/comment/rating/system）';
COMMENT ON COLUMN notification.title IS '通知标题';
COMMENT ON COLUMN notification.content IS '通知内容';
COMMENT ON COLUMN notification.related_id IS '关联ID（事件ID等）';
COMMENT ON COLUMN notification.sender_id IS '发送者ID';
COMMENT ON COLUMN notification.is_read IS '是否已读（0-未读，1-已读）';

-- =====================================================
-- 7. 创建用户关注关系表
-- =====================================================
CREATE TABLE user_follow (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_follow_relation UNIQUE (follower_id, following_id)
);

CREATE INDEX idx_follow_follower_id ON user_follow(follower_id);
CREATE INDEX idx_follow_following_id ON user_follow(following_id);

COMMENT ON TABLE user_follow IS '用户关注关系表';
COMMENT ON COLUMN user_follow.follower_id IS '关注者ID';
COMMENT ON COLUMN user_follow.following_id IS '被关注者ID';

-- =====================================================
-- 8. 创建事件评价表
-- =====================================================
CREATE TABLE event_rating (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    rater_id BIGINT NOT NULL,
    rated_user_id BIGINT NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    tags VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rating_rater FOREIGN KEY (rater_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_rated_user FOREIGN KEY (rated_user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_rating_unique UNIQUE (event_id, rater_id, rated_user_id)
);

CREATE INDEX idx_rating_event_id ON event_rating(event_id);
CREATE INDEX idx_rating_rater_id ON event_rating(rater_id);
CREATE INDEX idx_rating_rated_user_id ON event_rating(rated_user_id);
CREATE INDEX idx_rating_create_time ON event_rating(create_time DESC);

COMMENT ON TABLE event_rating IS '事件评价表';
COMMENT ON COLUMN event_rating.event_id IS '事件ID';
COMMENT ON COLUMN event_rating.rater_id IS '评价者ID';
COMMENT ON COLUMN event_rating.rated_user_id IS '被评价者ID';
COMMENT ON COLUMN event_rating.score IS '评分（1-5星）';
COMMENT ON COLUMN event_rating.comment IS '评价内容';
COMMENT ON COLUMN event_rating.tags IS '评价标签（逗号分隔）';

-- =====================================================
-- 9. 创建事件收藏表
-- =====================================================
CREATE TABLE event_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_favorite_unique UNIQUE (user_id, event_id)
);

CREATE INDEX idx_favorite_user_id ON event_favorite(user_id);
CREATE INDEX idx_favorite_event_id ON event_favorite(event_id);
CREATE INDEX idx_favorite_create_time ON event_favorite(create_time DESC);

COMMENT ON TABLE event_favorite IS '事件收藏表';

-- =====================================================
-- 10. 创建事件评论表
-- =====================================================
CREATE TABLE event_comment (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    reply_to_user_id BIGINT,
    like_count INTEGER DEFAULT 0,
    is_deleted INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES event_comment(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_reply_to FOREIGN KEY (reply_to_user_id) REFERENCES sys_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_comment_event_id ON event_comment(event_id);
CREATE INDEX idx_comment_user_id ON event_comment(user_id);
CREATE INDEX idx_comment_parent_id ON event_comment(parent_id);
CREATE INDEX idx_comment_create_time ON event_comment(create_time DESC);

COMMENT ON TABLE event_comment IS '事件评论表';
COMMENT ON COLUMN event_comment.event_id IS '事件ID';
COMMENT ON COLUMN event_comment.user_id IS '评论者ID';
COMMENT ON COLUMN event_comment.content IS '评论内容';
COMMENT ON COLUMN event_comment.parent_id IS '父评论ID（回复时使用）';
COMMENT ON COLUMN event_comment.reply_to_user_id IS '回复目标用户ID';
COMMENT ON COLUMN event_comment.like_count IS '点赞数';
COMMENT ON COLUMN event_comment.is_deleted IS '是否删除（0-正常，1-已删除）';

-- =====================================================
-- 11. 插入校园点位初始数据（西南大学）
-- =====================================================
INSERT INTO campus_point (point_name, longitude, latitude, point_type) VALUES
-- 教学楼
('雨僧楼（一教）', 106.4225, 29.8282, 'teaching'),
('兰华楼（二教）', 106.4235, 29.8275, 'teaching'),
('绩镛楼', 106.4220, 29.8270, 'teaching'),
('咏修楼', 106.4230, 29.8270, 'teaching'),
('师元楼（六教）', 106.4230, 29.8260, 'teaching'),
('立惠楼（物理大楼）', 106.4235, 29.8255, 'teaching'),
('白南楼', 106.4230, 29.8250, 'teaching'),
('敬德楼', 106.4210, 29.8260, 'teaching'),
('崇德楼（九教）', 106.4210, 29.8240, 'teaching'),
('含弘楼（十教）', 106.4220, 29.8230, 'teaching'),
('三十四楼（书楠楼）', 106.4270, 29.8280, 'teaching'),
('三十三教', 106.4200, 29.8230, 'teaching'),
('田家炳（教育学部）', 106.4240, 29.8270, 'teaching'),
('作孚楼', 106.4240, 29.8280, 'teaching'),
-- 办公楼
('隆平楼', 106.4240, 29.8240, 'office'),
('行署楼（行政楼）', 106.4250, 29.8270, 'office'),
('八一楼（研究生院）', 106.4250, 29.8260, 'office'),
('干训楼', 106.4200, 29.8260, 'office'),
('出版大楼', 106.4220, 29.8240, 'office'),
-- 图书馆
('中心图书馆', 106.4240, 29.8250, 'library'),
('弘文楼', 106.4245, 29.8240, 'library'),
-- 其他
('光大礼堂', 106.4220, 29.8250, 'auditorium'),
('光炯楼（侯光炯纪念馆）', 106.4245, 29.8260, 'exhibition'),
('君展楼（校医院）', 106.4210, 29.8270, 'medical'),
('蚕学宫', 106.4260, 29.8250, 'laboratory'),
-- 食堂
('禾丰楼（北区教职工食堂）', 106.4260, 29.8270, 'dining'),
-- 宾馆
('金桂楼（桂园宾馆）', 106.4260, 29.8275, 'hotel'),
('丹桂楼（桂园宾馆）', 106.4260, 29.8275, 'hotel'),
-- 宿舍
('梅园1舍', 106.4220, 29.8290, 'dorm'),
('竹园1舍', 106.4210, 29.8250, 'dorm'),
('楠园1舍', 106.4210, 29.8220, 'dorm'),
-- 校门
('含弘门（1号门）', 106.4260, 29.8260, 'gate'),
('学行门（2号门）', 106.4215, 29.8137, 'gate'),
('天生门（3号门）', 106.4270, 29.8210, 'gate'),
-- 荣昌校区
('荣昌校区行政楼', 105.5790, 29.3960, 'office'),
('荣昌校区图书馆', 105.5780, 29.3950, 'library'),
('荣昌校区第一教学楼', 105.5790, 29.3950, 'teaching'),
('荣昌校区学生宿舍1栋', 105.5780, 29.3960, 'dorm'),
('荣昌校区食堂', 105.5770, 29.3970, 'dining');

-- =====================================================
-- 11. 创建聊天消息表
-- =====================================================
CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(255),
    content TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_message_event_id ON chat_message(event_id);
CREATE INDEX idx_chat_message_create_time ON chat_message(create_time);

COMMENT ON TABLE chat_message IS '聊天消息表';
COMMENT ON COLUMN chat_message.event_id IS '事件ID';
COMMENT ON COLUMN chat_message.user_id IS '发送者用户ID';
COMMENT ON COLUMN chat_message.nickname IS '发送者昵称';
COMMENT ON COLUMN chat_message.avatar IS '发送者头像';
COMMENT ON COLUMN chat_message.content IS '消息内容';
COMMENT ON COLUMN chat_message.create_time IS '发送时间';

-- =====================================================
-- 完成提示
-- =====================================================
-- 数据库重建完成！
-- 请确保同时清理 Redis 中的事件缓存数据
