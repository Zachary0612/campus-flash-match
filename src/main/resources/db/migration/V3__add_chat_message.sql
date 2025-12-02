-- 添加聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(255),
    content TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_message_event_id ON chat_message(event_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_create_time ON chat_message(create_time);

COMMENT ON TABLE chat_message IS '聊天消息表';
COMMENT ON COLUMN chat_message.event_id IS '事件ID';
COMMENT ON COLUMN chat_message.user_id IS '发送者用户ID';
COMMENT ON COLUMN chat_message.nickname IS '发送者昵称';
COMMENT ON COLUMN chat_message.avatar IS '发送者头像';
COMMENT ON COLUMN chat_message.content IS '消息内容';
COMMENT ON COLUMN chat_message.create_time IS '发送时间';
