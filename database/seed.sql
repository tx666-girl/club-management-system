USE club_system;

INSERT IGNORE INTO sys_user
  (username, password, name, role, status)
VALUES
  ('admin', '123456', '系统管理员', 'ADMIN', 'ACTIVE');

INSERT IGNORE INTO club
  (name, description, image_url)
VALUES
  ('计算机协会', '面向喜欢编程和技术实践的同学。', 'images/clubs/tech.jpg'),
  ('摄影社', '记录校园和城市中的精彩瞬间。', 'images/clubs/photo.jpg'),
  ('羽毛球社', '组织训练、比赛和社团交流。', 'images/clubs/sport.jpg'),
  ('青年志愿者协会', '参与校园和社区志愿服务活动。', 'images/clubs/volunteer.jpg');