USE nju_market;

SET NAMES utf8mb4;

DELETE FROM order_item;
DELETE FROM good_image;
DELETE FROM favorite;
DELETE FROM rating;
DELETE FROM comment;
DELETE FROM report;
DELETE FROM `order`;
DELETE FROM good;
DELETE FROM admin;
DELETE FROM `user`;

INSERT INTO `user` (
  user_id, username, password, email, phone, avatar_url, campus, address, created_at, updated_at
) VALUES
  (1, 'alice', '123456', 'alice@example.com', '13800000001', 'https://example.com/avatar/alice.png', 'Xianlin', 'Dorm 1-201', '2026-04-20 09:00:00', '2026-04-20 09:00:00'),
  (2, 'bob', '123456', 'bob@example.com', '13800000002', 'https://example.com/avatar/bob.png', 'Gulou', 'Dorm 3-402', '2026-04-20 09:10:00', '2026-04-21 10:00:00'),
  (3, 'charlie', '123456', 'charlie@example.com', '13800000003', 'https://example.com/avatar/charlie.png', 'Xianlin', 'Dorm 5-118', '2026-04-20 09:20:00', '2026-04-22 11:00:00'),
  (4, 'diana', '123456', 'diana@example.com', '13800000004', 'https://example.com/avatar/diana.png', 'Pukou', 'Dorm 2-315', '2026-04-20 09:30:00', '2026-04-22 14:30:00');

INSERT INTO admin (
  admin_id, username, password, role, created_at
) VALUES
  (1, 'root', '123456', 'SUPER_ADMIN', '2026-04-20 08:00:00'),
  (2, 'auditor', '123456', 'CONTENT_ADMIN', '2026-04-20 08:30:00');

INSERT INTO good (
  good_id, seller_id, title, description, price, category, status, `condition`, view_count, created_at, updated_at
) VALUES
  (1, 1, '二手高数教材', '同济版高等数学，上册有少量笔记。', 25.50, 'book', 'ON_SALE', 'USED_GOOD', 35, '2026-04-21 09:00:00', '2026-04-23 12:00:00'),
  (2, 2, '机械键盘', '87 键青轴，带原装数据线。', 120.00, 'digital', 'ON_SALE', 'USED_GOOD', 62, '2026-04-21 10:00:00', '2026-04-24 15:20:00'),
  (3, 3, '宿舍台灯', '亮度正常，适合晚自习使用。', 18.80, 'daily', 'SOLD', 'USED_FAIR', 17, '2026-04-21 11:00:00', '2026-04-25 09:40:00'),
  (4, 1, '篮球', '七成新，适合日常训练。', 45.00, 'sports', 'ON_SALE', 'USED_GOOD', 29, '2026-04-21 14:00:00', '2026-04-25 18:10:00');

INSERT INTO `order` (
  order_id, buyer_id, seller_id, order_number, total_amount, status, created_at, updated_at
) VALUES
  (1, 2, 1, 'ORD202604270001', 25.50, 'PENDING', '2026-04-24 13:20:00', '2026-04-24 13:20:00'),
  (2, 4, 3, 'ORD202604270002', 18.80, 'COMPLETED', '2026-04-25 16:00:00', '2026-04-25 18:30:00'),
  (3, 3, 2, 'ORD202604270003', 120.00, 'CANCELLED', '2026-04-26 10:15:00', '2026-04-26 11:00:00');

INSERT INTO report (
  report_id, reporter_id, target_user_id, target_good_id, report_type, reason, status, handler_admin_id, handle_result, created_at, updated_at
) VALUES
  (1, 4, 2, 2, 'GOOD', '商品描述与实物不符', 'PROCESSING', 2, '已联系卖家补充说明', '2026-04-25 19:00:00', '2026-04-26 09:00:00'),
  (2, 1, 3, 3, 'USER', '交易后失联', 'RESOLVED', 1, '核实后已警告该用户', '2026-04-26 08:20:00', '2026-04-26 17:45:00');

INSERT INTO comment (
  comment_id, user_id, good_id, parent_comment_id, content, created_at
) VALUES
  (1, 2, 1, NULL, '请问这本书还在吗？', '2026-04-24 12:00:00'),
  (2, 1, 1, 1, '还在的，可以今天晚上面交。', '2026-04-24 12:15:00'),
  (3, 3, 2, NULL, '键盘有没有进水或者维修过？', '2026-04-24 18:30:00'),
  (4, 2, 4, NULL, '篮球可以刀一点吗？', '2026-04-25 20:00:00');

INSERT INTO rating (
  rating_id, user_id, good_id, score, created_at
) VALUES
  (1, 2, 1, 5, '2026-04-24 20:00:00'),
  (2, 4, 3, 4, '2026-04-25 19:00:00'),
  (3, 1, 2, 3, '2026-04-26 12:00:00');

INSERT INTO favorite (
  favorite_id, user_id, good_id, created_at
) VALUES
  (1, 2, 2, '2026-04-23 08:00:00'),
  (2, 3, 1, '2026-04-23 09:10:00'),
  (3, 4, 4, '2026-04-25 21:00:00');

INSERT INTO good_image (
  image_id, good_id, image_url, sort_order, created_at
) VALUES
  (1, 1, 'https://example.com/goods/math-book-1.png', 1, '2026-04-21 09:05:00'),
  (2, 1, 'https://example.com/goods/math-book-2.png', 2, '2026-04-21 09:06:00'),
  (3, 2, 'https://example.com/goods/keyboard-1.png', 1, '2026-04-21 10:05:00'),
  (4, 4, 'https://example.com/goods/basketball-1.png', 1, '2026-04-21 14:10:00');

INSERT INTO order_item (
  item_id, order_id, good_id, quantity, unit_price, subtotal
) VALUES
  (1, 1, 1, 1, 25.50, 25.50),
  (2, 2, 3, 1, 18.80, 18.80),
  (3, 3, 2, 1, 120.00, 120.00);
