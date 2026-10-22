INSERT INTO sys_user (username, password, name, student_no, grade, role, status) VALUES
('admin', '123456', '管理员', '20210001', '大四', 'admin', 'active'),
('zhangsan', '123456', '张三', '20220101', '大三', 'student', 'active'),
('lisi', '123456', '李四', '20220102', '大三', 'student', 'active'),
('wangwu', '123456', '王五', '20220103', '大三', 'student', 'active');

INSERT INTO club (name, description, president_id) VALUES
('计算机协会', '学习计算机相关知识', 1),
('机器人社', '机器人设计与制作', 2);

INSERT INTO club_member (club_id, user_id, position) VALUES
(1, 1, '会长'),
(1, 2, 'member'),
(1, 3, 'member'),
(2, 2, '会长'),
(2, 4, 'member');
