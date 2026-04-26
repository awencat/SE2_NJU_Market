CREATE DATABASE IF NOT EXISTS nju_market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nju_market;

CREATE TABLE IF NOT EXISTS `user` (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(120),
  phone VARCHAR(30),
  avatar_url VARCHAR(255),
  campus VARCHAR(100),
  address VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin (
  admin_id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS good (
  good_id INT PRIMARY KEY AUTO_INCREMENT,
  seller_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  category VARCHAR(100),
  status VARCHAR(50),
  `condition` VARCHAR(50),
  view_count INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_good_seller FOREIGN KEY (seller_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS `order` (
  order_id INT PRIMARY KEY AUTO_INCREMENT,
  buyer_id INT NOT NULL,
  seller_id INT NOT NULL,
  order_number VARCHAR(100) NOT NULL UNIQUE,
  total_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS report (
  report_id INT PRIMARY KEY AUTO_INCREMENT,
  reporter_id INT NOT NULL,
  target_user_id INT,
  target_good_id INT,
  report_type VARCHAR(50),
  reason VARCHAR(255),
  status VARCHAR(50),
  handler_admin_id INT,
  handle_result VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_report_target_user FOREIGN KEY (target_user_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_report_target_good FOREIGN KEY (target_good_id) REFERENCES good (good_id),
  CONSTRAINT fk_report_admin FOREIGN KEY (handler_admin_id) REFERENCES admin (admin_id)
);

CREATE TABLE IF NOT EXISTS comment (
  comment_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  good_id INT NOT NULL,
  parent_comment_id INT,
  content VARCHAR(500) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_comment_good FOREIGN KEY (good_id) REFERENCES good (good_id),
  CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comment (comment_id)
);

CREATE TABLE IF NOT EXISTS rating (
  rating_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  good_id INT NOT NULL,
  score INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_rating_good FOREIGN KEY (good_id) REFERENCES good (good_id)
);

CREATE TABLE IF NOT EXISTS favorite (
  favorite_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  good_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_favorite_good FOREIGN KEY (good_id) REFERENCES good (good_id)
);

CREATE TABLE IF NOT EXISTS good_image (
  image_id INT PRIMARY KEY AUTO_INCREMENT,
  good_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  sort_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_good_image_good FOREIGN KEY (good_id) REFERENCES good (good_id)
);

CREATE TABLE IF NOT EXISTS order_item (
  item_id INT PRIMARY KEY AUTO_INCREMENT,
  order_id INT NOT NULL,
  good_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  unit_price DECIMAL(10, 2) NOT NULL,
  subtotal DECIMAL(10, 2) NOT NULL,
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order` (order_id),
  CONSTRAINT fk_order_item_good FOREIGN KEY (good_id) REFERENCES good (good_id)
);
