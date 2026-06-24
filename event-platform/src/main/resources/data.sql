-- =================================================================================
-- UNIVERSAL EVENT ENGINE: CORRECTED SCHEMA
-- =================================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  system_role VARCHAR(50) DEFAULT 'JUDGE'
);

CREATE TABLE IF NOT EXISTS events (
  id SERIAL PRIMARY KEY,
  slug VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  config JSONB NOT NULL,
  current_global_round INT NOT NULL DEFAULT 1,
  is_active BOOLEAN DEFAULT TRUE 
);

CREATE TABLE IF NOT EXISTS registrations (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  event_id INT NOT NULL,
  unstop_team_id VARCHAR(255) NOT NULL,
  team_name VARCHAR(255) NOT NULL,
  team_passcode VARCHAR(50) NOT NULL,
  progress_state JSONB NOT NULL DEFAULT '{}'::jsonb,
  member_details JSONB,
  total_score DOUBLE PRECISION DEFAULT 0.0,
  registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_registrations_event
    FOREIGN KEY (event_id)
    REFERENCES events(id) ON DELETE CASCADE,
  CONSTRAINT uq_event_unstop_team
    UNIQUE(event_id, unstop_team_id)
);

CREATE TABLE IF NOT EXISTS submissions (
  id SERIAL PRIMARY KEY,
  registration_id UUID NOT NULL,
  round_number INT NOT NULL, 
  task_id VARCHAR(100) NOT NULL, -- '1a', 'feature-auth'
  submission_type VARCHAR(50) DEFAULT 'COMMIT',
  payload JSONB NOT NULL,
  status VARCHAR(50) DEFAULT 'PENDING',
  average_score DOUBLE PRECISION,
  rejection_reason TEXT,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_submissions_registration
    FOREIGN KEY (registration_id)
    REFERENCES registrations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS evaluations (
  id SERIAL PRIMARY KEY,
  submission_id INT NOT NULL, -- REMOVED 'UNIQUE' FROM HERE
  judge_id INT NOT NULL,
  score_breakdown JSONB,
  total_score INT NOT NULL,
  feedback TEXT,
  graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_evaluations_submission
    FOREIGN KEY (submission_id)
    REFERENCES submissions(id) ON DELETE CASCADE,
  CONSTRAINT fk_evaluations_judge
    FOREIGN KEY (judge_id) REFERENCES users(id),
  -- ADDED COMPOSITE UNIQUE CONSTRAINT HERE
  CONSTRAINT uq_evaluation_per_judge 
    UNIQUE(submission_id, judge_id) 
);

CREATE TABLE IF NOT EXISTS evaluation_audits (
  id SERIAL PRIMARY KEY,
  evaluation_id INT NOT NULL,
  old_score_breakdown JSONB,
  old_total_score INT NOT NULL,
  old_feedback TEXT,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_evaluation_audits_eval
    FOREIGN KEY (evaluation_id)
    REFERENCES evaluations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demo_calls (
  id SERIAL PRIMARY KEY,
  submission_id INT UNIQUE NOT NULL,
  judge_id INT,
  meeting_link VARCHAR(255),
  status VARCHAR(50) NOT NULL DEFAULT 'QUEUED',
  called_at TIMESTAMP,
  completed_at TIMESTAMP,

  CONSTRAINT fk_demo_calls_submission
    FOREIGN KEY (submission_id)
    REFERENCES submissions(id) ON DELETE CASCADE,
  CONSTRAINT fk_demo_calls_judge
    FOREIGN KEY (judge_id)
    REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS mentor_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    skills TEXT,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    current_status VARCHAR(255) NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS mentor_sessions (
    id SERIAL PRIMARY KEY,
    registration_id UUID NOT NULL REFERENCES registrations(id),
    mentor_id BIGINT NOT NULL REFERENCES users(id),
    issue_description TEXT NOT NULL,
    meeting_link VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    requested_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITHOUT TIME ZONE
);

-- =================================================================================
-- MOCK SEED DATA
-- =================================================================================

INSERT INTO users (username, password_hash, system_role) 
VALUES ('admin', '$2a$05$sWYFlT6AvrunOwyg6WqEye0HegK1uh7z1rcC6clRHy/XN8.lXvXlu', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password_hash, system_role) 
VALUES ('judge1', '$2a$05$sWYFlT6AvrunOwyg6WqEye0HegK1uh7z1rcC6clRHy/XN8.lXvXlu', 'ROLE_JUDGE')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password_hash, system_role) 
VALUES ('mentor1', '$2a$05$sWYFlT6AvrunOwyg6WqEye0HegK1uh7z1rcC6clRHy/XN8.lXvXlu', 'ROLE_JUDGE')
ON CONFLICT (username) DO NOTHING;

-- Optionally insert mentor_profiles if they don't exist
INSERT INTO mentor_profiles (user_id, skills, is_active, current_status)
SELECT id, 'React, Spring Boot, DevOps', FALSE, 'AVAILABLE' FROM users WHERE username = 'mentor1'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO events (slug, name, event_type, config, current_global_round, is_active) 
VALUES (
    'unlockd-2024', 
    'Unlock''D', 
    'PROGRESSIVE_PRODUCT_BUILDING_EVENT', 
    '{"total_rounds": 3, "passing_threshold": 60, "is_leaderboard_published": false, "meeting_link": "", "active_meeting_team_id": "", "roadmap": [{"step": 1, "task_id": "FEATURE-1", "round": 1, "rubric": ["functionality", "code_quality"]}, {"step": 2, "task_id": "FEATURE-2", "round": 1, "rubric": ["functionality", "code_quality"]}, {"step": 3, "task_id": "FEATURE-3", "round": 1, "rubric": ["functionality", "code_quality"]}, {"step": 4, "task_id": "ROUND-2", "round": 2, "rubric": ["ux", "polish", "innovation"]}, {"step": 5, "task_id": "ROUND-3", "round": 3, "rubric": ["presentation", "business_viability"]}]}'::jsonb, 
    1,
    TRUE
)
ON CONFLICT (slug) DO NOTHING;
