-- 数据库迁移脚本：修复字段约束
-- 如果表已存在，需要执行此脚本来修复字段约束

-- 修改participants字段，允许NULL值
ALTER TABLE event_history ALTER COLUMN participants DROP NOT NULL;
ALTER TABLE event_history ALTER COLUMN participants SET DEFAULT NULL;

-- 修改settle_time字段，允许NULL值
ALTER TABLE event_history ALTER COLUMN settle_time DROP NOT NULL;
ALTER TABLE event_history ALTER COLUMN settle_time SET DEFAULT NULL;

-- 清理可能存在的无效数据
UPDATE event_history SET participants = NULL WHERE participants = '{}';
UPDATE event_history SET settle_time = NULL WHERE settle_time IS NOT NULL AND status = 'active';
