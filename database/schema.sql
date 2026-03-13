-- ============================================================
-- Fake Social Media Account Detection - Database Schema
-- Database: PostgreSQL 15+
-- ============================================================

CREATE DATABASE fake_detection_db;
\c fake_detection_db;

-- EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- USERS TABLE (App users / analysts)
-- ============================================================
CREATE TABLE app_users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username      VARCHAR(50)  UNIQUE NOT NULL,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'ANALYST' CHECK (role IN ('ADMIN','ANALYST','VIEWER')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================================
-- SOCIAL ACCOUNTS TABLE (Accounts being analyzed)
-- ============================================================
CREATE TABLE social_accounts (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    platform            VARCHAR(30)  NOT NULL CHECK (platform IN ('TWITTER','INSTAGRAM','FACEBOOK','TIKTOK','LINKEDIN','OTHER')),
    username            VARCHAR(100) NOT NULL,
    display_name        VARCHAR(200),
    profile_url         TEXT,
    profile_pic_url     TEXT,
    bio                 TEXT,
    followers_count     BIGINT       DEFAULT 0,
    following_count     BIGINT       DEFAULT 0,
    posts_count         BIGINT       DEFAULT 0,
    account_age_days    INT          DEFAULT 0,
    is_verified         BOOLEAN      DEFAULT FALSE,
    has_profile_pic     BOOLEAN      DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    submitted_by        UUID REFERENCES app_users(id) ON DELETE SET NULL,
    UNIQUE (platform, username)
);

-- ============================================================
-- DETECTION RESULTS TABLE
-- ============================================================
CREATE TABLE detection_results (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    social_account_id   UUID NOT NULL REFERENCES social_accounts(id) ON DELETE CASCADE,
    analyzed_by         UUID REFERENCES app_users(id) ON DELETE SET NULL,
    fake_score          NUMERIC(5,2) NOT NULL CHECK (fake_score BETWEEN 0 AND 100),
    verdict             VARCHAR(20)  NOT NULL CHECK (verdict IN ('REAL','SUSPICIOUS','FAKE','UNKNOWN')),
    confidence_level    VARCHAR(10)  NOT NULL CHECK (confidence_level IN ('LOW','MEDIUM','HIGH')),
    analysis_notes      TEXT,
    analyzed_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    model_version       VARCHAR(20)  DEFAULT 'v1.0'
);

-- ============================================================
-- FEATURE SCORES TABLE (Individual ML feature scores)
-- ============================================================
CREATE TABLE feature_scores (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    detection_result_id UUID NOT NULL REFERENCES detection_results(id) ON DELETE CASCADE,
    feature_name        VARCHAR(100) NOT NULL,
    feature_value       NUMERIC(10,4),
    feature_score       NUMERIC(5,2),  -- 0-100 contribution to fake score
    feature_category    VARCHAR(50)    CHECK (feature_category IN ('PROFILE','ACTIVITY','NETWORK','CONTENT','BEHAVIORAL'))
);

-- ============================================================
-- AUDIT LOG TABLE
-- ============================================================
CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES app_users(id) ON DELETE SET NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   UUID,
    details     JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- BATCH ANALYSIS JOBS TABLE
-- ============================================================
CREATE TABLE batch_jobs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_name        VARCHAR(200) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','RUNNING','COMPLETED','FAILED')),
    total_accounts  INT DEFAULT 0,
    processed       INT DEFAULT 0,
    failed_count    INT DEFAULT 0,
    created_by      UUID REFERENCES app_users(id) ON DELETE SET NULL,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    error_message   TEXT
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_social_accounts_platform    ON social_accounts(platform);
CREATE INDEX idx_social_accounts_submitted   ON social_accounts(submitted_by);
CREATE INDEX idx_detection_results_account   ON detection_results(social_account_id);
CREATE INDEX idx_detection_results_verdict   ON detection_results(verdict);
CREATE INDEX idx_detection_results_analyzed  ON detection_results(analyzed_at DESC);
CREATE INDEX idx_feature_scores_result       ON feature_scores(detection_result_id);
CREATE INDEX idx_audit_logs_user             ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created          ON audit_logs(created_at DESC);

-- ============================================================
-- VIEWS
-- ============================================================
CREATE OR REPLACE VIEW v_account_summary AS
SELECT
    sa.id,
    sa.platform,
    sa.username,
    sa.display_name,
    sa.followers_count,
    sa.following_count,
    sa.posts_count,
    sa.account_age_days,
    sa.is_verified,
    sa.has_profile_pic,
    dr.fake_score,
    dr.verdict,
    dr.confidence_level,
    dr.analyzed_at,
    au.username AS submitted_by_user
FROM social_accounts sa
LEFT JOIN LATERAL (
    SELECT * FROM detection_results
    WHERE social_account_id = sa.id
    ORDER BY analyzed_at DESC LIMIT 1
) dr ON TRUE
LEFT JOIN app_users au ON sa.submitted_by = au.id;

-- Stats summary view
CREATE OR REPLACE VIEW v_detection_stats AS
SELECT
    COUNT(*) FILTER (WHERE verdict = 'FAKE')       AS total_fake,
    COUNT(*) FILTER (WHERE verdict = 'REAL')       AS total_real,
    COUNT(*) FILTER (WHERE verdict = 'SUSPICIOUS') AS total_suspicious,
    COUNT(*) FILTER (WHERE verdict = 'UNKNOWN')    AS total_unknown,
    COUNT(*)                                        AS total_analyzed,
    ROUND(AVG(fake_score), 2)                       AS avg_fake_score,
    MAX(analyzed_at)                                AS last_analysis
FROM detection_results;

-- ============================================================
-- SEED DATA
-- ============================================================
INSERT INTO app_users (username, email, password_hash, role) VALUES
('admin',    'admin@fakedetect.io',   crypt('Admin@123', gen_salt('bf')), 'ADMIN'),
('analyst1', 'analyst1@fakedetect.io', crypt('Analyst@123', gen_salt('bf')), 'ANALYST'),
('viewer1',  'viewer1@fakedetect.io',  crypt('Viewer@123', gen_salt('bf')), 'VIEWER');

INSERT INTO social_accounts (platform, username, display_name, followers_count, following_count, posts_count, account_age_days, is_verified, has_profile_pic, bio) VALUES
('TWITTER',  'suspicious_acc1', 'Free Money Giveaway!!!', 120, 4800, 3,   2,   FALSE, FALSE, 'Click link for free rewards!!!'),
('INSTAGRAM','real_user_99',    'John Traveller',         3200, 400, 210, 730, FALSE, TRUE,  'Travel photographer | Coffee lover'),
('FACEBOOK', 'bot_account_x',   'News Updates Daily',    890, 1200, 45,  15,  FALSE, FALSE, ''),
('TWITTER',  'verified_brand',  'TechCorp Official',     125000, 300, 1820, 1200, TRUE, TRUE, 'Official account of TechCorp');
