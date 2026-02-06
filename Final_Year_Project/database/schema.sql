-- Role-Based AI Interview Preparation System - Database Schema

CREATE DATABASE IF NOT EXISTS ai_interview_prep CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_interview_prep;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    target_role VARCHAR(255) NULL,
    role_selected_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
);

-- Interview sessions
CREATE TABLE interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_snapshot VARCHAR(255) NOT NULL,
    session_type VARCHAR(50) NOT NULL,
    topic VARCHAR(100),
    score INT,
    total_questions INT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_session_user (user_id),
    INDEX idx_session_role (role_snapshot)
);

-- Session questions
CREATE TABLE session_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    difficulty VARCHAR(20),
    topic VARCHAR(100),
    user_answer TEXT,
    ai_feedback TEXT,
    score INT,
    question_order INT,
    FOREIGN KEY (session_id) REFERENCES interview_sessions(id) ON DELETE CASCADE,
    INDEX idx_sq_session (session_id)
);
