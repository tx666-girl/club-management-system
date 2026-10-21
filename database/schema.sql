CREATE DATABASE IF NOT EXISTS club_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE club_system;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS join_application;
DROP TABLE IF EXISTS club_announcement;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS club_member;
DROP TABLE IF EXISTS club;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  name VARCHAR(50) NOT NULL,
  student_no VARCHAR(30),
  grade VARCHAR(20),
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE club (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  category VARCHAR(30) NOT NULL,
  description TEXT NOT NULL,
  image_url VARCHAR(255),
  member_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE club_member (
  club_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (club_id, user_id),
  CONSTRAINT fk_member_club
    FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE,
  CONSTRAINT fk_member_user
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  club_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL DEFAULT 'JOIN',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  reviewed_by BIGINT NULL,
  reviewed_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_application_user_club (user_id, club_id, type),
  CONSTRAINT fk_application_user
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_application_club
    FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE,
  CONSTRAINT fk_application_reviewer
    FOREIGN KEY (reviewed_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  content TEXT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_announcement_club_title (club_id, title),
  CONSTRAINT fk_announcement_club
    FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE,
  CONSTRAINT fk_announcement_user
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE activity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  club_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT NOT NULL,
  start_time DATETIME NOT NULL,
  location VARCHAR(150) NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_activity_club_title (club_id, title),
  CONSTRAINT fk_activity_club
    FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE,
  CONSTRAINT fk_activity_user
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE INDEX idx_club_category ON club(category);
CREATE INDEX idx_application_status ON application(status);
CREATE INDEX idx_announcement_club ON announcement(club_id, created_at);
CREATE INDEX idx_activity_club ON activity(club_id, start_time);
