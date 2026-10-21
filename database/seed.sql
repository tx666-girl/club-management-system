USE club_system;

INSERT IGNORE INTO sys_user
  (username, password, name, student_no, grade, role, status)
VALUES
  ('admin', '123456', '系统管理员', 'A00001', '教师', 'ADMIN', 'ACTIVE'),
  ('zhangsan', '123456', '张三', '123456', '2024级', 'USER', 'ACTIVE'),
  ('lisi', '123456', '李四', '123457', '2024级', 'USER', 'ACTIVE'),
  ('wangwu', '123456', '王五', '123458', '2023级', 'USER', 'ACTIVE');

INSERT IGNORE INTO club
  (name, category, description, image_url, member_count)
VALUES
  ('计算机协会', 'tech', '技术分享、项目实践和编程交流，适合喜欢动手实践的同学。', 'assets/clubs/tech.svg', 128),
  ('摄影社', 'art', '校园外拍、作品交流和摄影基础课程，记录生活里的光。', 'assets/clubs/photo.svg', 86),
  ('羽毛球社', 'sport', '固定训练、友谊赛和新手教学，欢迎零基础同学加入。', 'assets/clubs/badminton.svg', 96),
  ('青年志愿者协会', 'volunteer', '校园公益、社区服务和志愿活动，用行动连接校园与社会。', 'assets/clubs/volunteer.svg', 214),
  ('篮球社', 'sport', '日常训练、院系联赛和观赛交流，一起在球场上挥汗。', 'assets/clubs/basketball.svg', 74),
  ('足球社', 'sport', '组织训练和友谊赛，适合想保持运动习惯的同学。', 'assets/clubs/football.svg', 68),
  ('动漫社', 'art', '动画鉴赏、同人创作和校园漫展，欢迎热爱二次元的同学。', 'assets/clubs/anime.svg', 152),
  ('辩论社', 'tech', '围绕校园和社会话题训练表达、逻辑与团队协作能力。', 'assets/clubs/debate.svg', 47);

INSERT IGNORE INTO club_member (club_id, user_id)
SELECT c.id, u.id
FROM club c, sys_user u
WHERE c.name = '计算机协会' AND u.username = 'zhangsan';

INSERT IGNORE INTO club_member (club_id, user_id)
SELECT c.id, u.id
FROM club c, sys_user u
WHERE c.name = '摄影社' AND u.username = 'zhangsan';

INSERT IGNORE INTO application (user_id, club_id, type, status)
SELECT u.id, c.id, 'JOIN', 'PENDING'
FROM sys_user u, club c
WHERE u.username = 'lisi' AND c.name = '计算机协会';

INSERT IGNORE INTO application (user_id, club_id, type, status)
SELECT u.id, c.id, 'JOIN', 'PENDING'
FROM sys_user u, club c
WHERE u.username = 'wangwu' AND c.name = '摄影社';

INSERT IGNORE INTO announcement (club_id, title, content, created_by)
SELECT c.id, '本周活动报名开始', '本周技术分享活动开放报名，请提前十分钟到场。', u.id
FROM club c, sys_user u
WHERE c.name = '计算机协会' AND u.username = 'admin';

INSERT IGNORE INTO announcement (club_id, title, content, created_by)
SELECT c.id, '秋季外拍集合时间', '摄影社本周日早上八点在图书馆门口集合。', u.id
FROM club c, sys_user u
WHERE c.name = '摄影社' AND u.username = 'admin';

INSERT IGNORE INTO activity
  (club_id, title, description, start_time, location, created_by)
SELECT c.id, '前端项目实战分享', '从页面结构到接口联调，完成一个可运行的校园活动页面。',
       '2026-10-24 14:00:00', '活动室 A', u.id
FROM club c, sys_user u
WHERE c.name = '计算机协会' AND u.username = 'admin';

INSERT IGNORE INTO activity
  (club_id, title, description, start_time, location, created_by)
SELECT c.id, 'Java Web 入门工作坊', 'Servlet、JDBC 和数据库连接的基础实践。',
       '2026-10-31 19:00:00', '实验楼 203', u.id
FROM club c, sys_user u
WHERE c.name = '计算机协会' AND u.username = 'admin';

INSERT IGNORE INTO activity
  (club_id, title, description, start_time, location, created_by)
SELECT c.id, '秋季校园外拍', '带上相机，一起记录校园秋天的色彩。',
       '2026-11-02 08:00:00', '图书馆门口', u.id
FROM club c, sys_user u
WHERE c.name = '摄影社' AND u.username = 'admin';
