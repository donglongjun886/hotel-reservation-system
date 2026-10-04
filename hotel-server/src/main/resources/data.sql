-- 预置数据（幂等：INSERT IGNORE）
-- admin 预置账号：登录名 admin，明文密码 admin123（BCrypt 密文如下，演示用）

INSERT IGNORE INTO `user` (`id`, `login_name`, `password_hash`, `role`) VALUES
  (1, 'admin', '$2a$10$HZ9v9LU77InQmy68viHTGOkNrz7dbcRbkU6hAD5.heWwYwtaBxKQG', 'ADMIN');

INSERT IGNORE INTO `room_type` (`id`, `name`, `price`, `description`) VALUES
  (1, '大床房',   288.50, '1.8 米大床，含双早，免费 WiFi'),
  (2, '双床房',   328.00, '两张 1.2 米单人床，适合结伴出行'),
  (3, '家庭房',   458.00, '一张大床加一张儿童床，适合三口之家'),
  (4, '行政套房', 588.00, '独立客厅与卧室，含行政礼遇');

INSERT IGNORE INTO `room` (`room_no`, `room_type_id`) VALUES
  ('A101', 1), ('A102', 1), ('A103', 1),
  ('B101', 2), ('B102', 2), ('B103', 2),
  ('C101', 3), ('C102', 3),
  ('D101', 4), ('D102', 4);
