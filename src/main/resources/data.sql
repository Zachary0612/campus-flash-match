-- 1. 清空campus_point表原有数据
DELETE FROM campus_point;

-- （可选：若id是自增序列，重置序列起始值，避免id断层）
-- ALTER SEQUENCE campus_point_id_seq RESTART WITH 402;

-- 2. 插入西南大学建筑信息（字段对应：point_name/longitude/latitude/point_type/is_valid/create_time）
INSERT INTO campus_point (point_name, longitude, latitude, point_type, is_valid, create_time)
VALUES
-- 北碚主校区-教学/办公楼
('雨僧楼（一教）', 106.4225, 29.8282, 'teaching', true, NOW()),
('兰华楼（二教）', 106.4235, 29.8275, 'teaching', true, NOW()),
('绩镛楼', 106.4220, 29.8270, 'teaching', true, NOW()),
('咏修楼', 106.4230, 29.8270, 'teaching', true, NOW()),
('师元楼（六教）', 106.4230, 29.8260, 'teaching', true, NOW()),
('立惠楼（物理大楼）', 106.4235, 29.8255, 'teaching', true, NOW()),
('白南楼', 106.4230, 29.8250, 'teaching', true, NOW()),
('隆平楼', 106.4240, 29.8240, 'office', true, NOW()),
('行署楼（行政楼）', 106.4250, 29.8270, 'office', true, NOW()),
('八一楼（研究生院）', 106.4250, 29.8260, 'office', true, NOW()),
('中心图书馆', 106.4240, 29.8250, 'library', true, NOW()),
('弘文楼', 106.4245, 29.8240, 'library', true, NOW()),
('光大礼堂', 106.4220, 29.8250, 'auditorium', true, NOW()),
('敬德楼', 106.4210, 29.8260, 'teaching', true, NOW()),
('崇德楼（九教）', 106.4210, 29.8240, 'teaching', true, NOW()),
('含弘楼（十教）', 106.4220, 29.8230, 'teaching', true, NOW()),
('三十四楼（书楠楼）', 106.4270, 29.8280, 'teaching', true, NOW()),
('三十三教', 106.4200, 29.8230, 'teaching', true, NOW()),

-- 北碚主校区-特色建筑
('田家炳（教育学部）', 106.4240, 29.8270, 'teaching', true, NOW()),
('作孚楼', 106.4240, 29.8280, 'teaching', true, NOW()),
('光炯楼（侯光炯纪念馆）', 106.4245, 29.8260, 'exhibition', true, NOW()),
('君展楼（校医院）', 106.4210, 29.8270, 'medical', true, NOW()),
('干训楼', 106.4200, 29.8260, 'office', true, NOW()),
('出版大楼', 106.4220, 29.8240, 'office', true, NOW()),
('禾丰楼（北区教职工食堂）', 106.4260, 29.8270, 'dining', true, NOW()),
('金桂楼（桂园宾馆）', 106.4260, 29.8275, 'hotel', true, NOW()),
('丹桂楼（桂园宾馆）', 106.4260, 29.8275, 'hotel', true, NOW()),
('蚕学宫', 106.4260, 29.8250, 'laboratory', true, NOW()),

-- 北碚主校区-宿舍（示例）
('梅园1舍', 106.4220, 29.8290, 'dorm', true, NOW()),
('竹园1舍', 106.4210, 29.8250, 'dorm', true, NOW()),
('楠园1舍', 106.4210, 29.8220, 'dorm', true, NOW()),

-- 北碚主校区-校门
('含弘门（1号门）', 106.4260, 29.8260, 'gate', true, NOW()),
('学行门（2号门）', 106.4215, 29.8137, 'gate', true, NOW()),
('天生门（3号门）', 106.4270, 29.8210, 'gate', true, NOW()),

-- 荣昌校区-主要建筑
('荣昌校区行政楼', 105.5790, 29.3960, 'office', true, NOW()),
('荣昌校区图书馆', 105.5780, 29.3950, 'library', true, NOW()),
('荣昌校区第一教学楼', 105.5790, 29.3950, 'teaching', true, NOW()),
('荣昌校区学生宿舍1栋', 105.5780, 29.3960, 'dorm', true, NOW()),
('荣昌校区食堂', 105.5770, 29.3970, 'dining', true, NOW());