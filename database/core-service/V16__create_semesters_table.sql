-- Flyway migration: create core_service.semesters table
-- Adds semesters table used by the application

CREATE SCHEMA IF NOT EXISTS core_service;

CREATE TABLE IF NOT EXISTS core_service.semesters (
  id uuid PRIMARY KEY,
  academic_year varchar(50),
  code varchar(50),
  created_at timestamptz DEFAULT now(),
  created_by uuid,
  end_date date,
  is_active boolean DEFAULT true,
  name varchar(255),
  semester_number integer,
  start_date date,
  updated_at timestamptz,
  updated_by uuid
);

-- Optional indexes for common lookups
CREATE INDEX IF NOT EXISTS idx_semesters_code ON core_service.semesters(code);
CREATE INDEX IF NOT EXISTS idx_semesters_is_active ON core_service.semesters(is_active);
