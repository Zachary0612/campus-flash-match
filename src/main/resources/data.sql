插入一些初始校园点位数据
使用 INSERT INTO ... ON CONFLICT DO NOTHING 来避免重复插入错误
INSERT INTO campus_point (point_name, longitude, latitude, point_type, is_valid) VALUES
('第一教学楼', 116.3974, 39.9093, 'teaching', true),
('第二教学楼', 116.3980, 39.9095, 'teaching', true),
('图书馆', 116.3970, 39.9085, 'library', true),
('学生宿舍1号楼', 116.3960, 39.9075, 'dorm', true),
('学生宿舍2号楼', 116.3965, 39.9070, 'dorm', true),
('食堂A', 116.3985, 39.9080, 'dining', true),
('食堂B', 116.3990, 39.9082, 'dining', true)
ON CONFLICT DO NOTHING;