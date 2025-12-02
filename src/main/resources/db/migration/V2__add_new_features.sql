-- =====================================================
-- 校园闪聊匹配系统 - 新功能数据库迁移脚本
-- 版本: V2
-- 描述: 添加用户资料扩展、通知、关注、评价、收藏、评论等功能
-- =====================================================

-- 1. 扩展用户表字段
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS bio VARCHAR(500);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS gender INTEGER DEFAULT 0;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS major VARCHAR(100);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS grade VARCHAR(50);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS interests VARCHAR(500);

COMMENT ON COLUMN sys_user.bio IS '个人简介';
COMMENT ON COLUMN sys_user.gender IS '性别（0-未知，1-男，2-女）';
COMMENT ON COLUMN sys_user.major IS '专业';
COMMENT ON COLUMN sys_user.grade IS '年级';
COMMENT ON COLUMN sys_user.interests IS '兴趣标签（逗号分隔）';

-- 2. 创建通知消息表
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id VARCHAR(100),
    sender_id BIGINT,
    is_read INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notification(type);
CREATE INDEX IF NOT EXISTS idx_notification_is_read ON notification(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON notification(create_time DESC);

COMMENT ON TABLE notification IS '通知消息表';
COMMENT ON COLUMN notification.type IS '通知类型（event_join/event_full/event_settle/follow/comment/rating/system）';
COMMENT ON COLUMN notification.is_read IS '是否已读（0-未读，1-已读）';

-- 3. 创建用户关注关系表
CREATE TABLE IF NOT EXISTS user_follow (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_follow_relation UNIQUE (follower_id, following_id)
);

CREATE INDEX IF NOT EXISTS idx_follow_follower_id ON user_follow(follower_id);
CREATE INDEX IF NOT EXISTS idx_follow_following_id ON user_follow(following_id);

COMMENT ON TABLE user_follow IS '用户关注关系表';
COMMENT ON COLUMN user_follow.follower_id IS '关注者ID';
COMMENT ON COLUMN user_follow.following_id IS '被关注者ID';

-- 4. 创建事件评价表
CREATE TABLE IF NOT EXISTS event_rating (
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

CREATE INDEX IF NOT EXISTS idx_rating_event_id ON event_rating(event_id);
CREATE INDEX IF NOT EXISTS idx_rating_rater_id ON event_rating(rater_id);
CREATE INDEX IF NOT EXISTS idx_rating_rated_user_id ON event_rating(rated_user_id);
CREATE INDEX IF NOT EXISTS idx_rating_create_time ON event_rating(create_time DESC);

COMMENT ON TABLE event_rating IS '事件评价表';
COMMENT ON COLUMN event_rating.score IS '评分（1-5星）';
COMMENT ON COLUMN event_rating.tags IS '评价标签（逗号分隔）';

-- 5. 创建事件收藏表
CREATE TABLE IF NOT EXISTS event_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_favorite_unique UNIQUE (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_favorite_user_id ON event_favorite(user_id);
CREATE INDEX IF NOT EXISTS idx_favorite_event_id ON event_favorite(event_id);
CREATE INDEX IF NOT EXISTS idx_favorite_create_time ON event_favorite(create_time DESC);

COMMENT ON TABLE event_favorite IS '事件收藏表';

-- 6. 创建事件评论表
CREATE TABLE IF NOT EXISTS event_comment (
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

CREATE INDEX IF NOT EXISTS idx_comment_event_id ON event_comment(event_id);
CREATE INDEX IF NOT EXISTS idx_comment_user_id ON event_comment(user_id);
CREATE INDEX IF NOT EXISTS idx_comment_parent_id ON event_comment(parent_id);
CREATE INDEX IF NOT EXISTS idx_comment_create_time ON event_comment(create_time DESC);

COMMENT ON TABLE event_comment IS '事件评论表';
COMMENT ON COLUMN event_comment.parent_id IS '父评论ID（回复时使用）';
COMMENT ON COLUMN event_comment.reply_to_user_id IS '回复目标用户ID';
COMMENT ON COLUMN event_comment.is_deleted IS '是否删除（0-正常，1-已删除）';

-- 7. 添加事件历史表的评论数和收藏数字段（可选，用于快速查询）
ALTER TABLE event_history ADD COLUMN IF NOT EXISTS comment_count INTEGER DEFAULT 0;
ALTER TABLE event_history ADD COLUMN IF NOT EXISTS favorite_count INTEGER DEFAULT 0;

COMMENT ON COLUMN event_history.comment_count IS '评论数';
COMMENT ON COLUMN event_history.favorite_count IS '收藏数';
