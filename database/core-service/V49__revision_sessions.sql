-- ==========================================
-- V47: Revision Sessions for Post-Publication Workflow
-- ==========================================
-- Purpose: Track revision sessions when fixing published syllabi based on student feedback

-- Create revision session status enum
DO $$ BEGIN
    CREATE TYPE core_service.revision_session_status AS ENUM (
        'OPEN',           -- Admin opened session, collecting feedback
        'IN_PROGRESS',    -- Lecturer is working on fixes
        'PENDING_HOD',    -- Submitted to HOD for review
        'COMPLETED',      -- Session completed and republished
        'CANCELLED'       -- Session cancelled
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create revision_sessions table
CREATE TABLE IF NOT EXISTS core_service.revision_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Reference to syllabus version being revised
    syllabus_version_id UUID NOT NULL REFERENCES core_service.syllabus_versions(id) ON DELETE CASCADE,
    
    -- Session metadata
    session_number INTEGER NOT NULL DEFAULT 1,
    status core_service.revision_session_status NOT NULL DEFAULT 'OPEN',
    
    -- Who initiated this revision session
    initiated_by UUID NOT NULL REFERENCES core_service.users(id),
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Description of what needs to be fixed
    description TEXT,
    
    -- Lecturer assigned to fix
    assigned_lecturer_id UUID REFERENCES core_service.users(id),
    
    -- Timeline
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- HOD approval tracking
    hod_reviewed_by UUID REFERENCES core_service.users(id),
    hod_reviewed_at TIMESTAMP,
    hod_decision VARCHAR(20), -- APPROVED, REJECTED
    hod_comment TEXT,
    
    -- Admin republish tracking
    republished_by UUID REFERENCES core_service.users(id),
    republished_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add revision_session_id to syllabus_error_reports
ALTER TABLE core_service.syllabus_error_reports 
ADD COLUMN IF NOT EXISTS revision_session_id UUID REFERENCES core_service.revision_sessions(id);

-- Add resolved_in_version fields
ALTER TABLE core_service.syllabus_error_reports 
ADD COLUMN IF NOT EXISTS resolved_in_version_id UUID REFERENCES core_service.syllabus_versions(id);

ALTER TABLE core_service.syllabus_error_reports 
ADD COLUMN IF NOT EXISTS resolved_in_version_no VARCHAR(20);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_revision_session_syllabus 
ON core_service.revision_sessions(syllabus_version_id);

CREATE INDEX IF NOT EXISTS idx_revision_session_status 
ON core_service.revision_sessions(status);

CREATE INDEX IF NOT EXISTS idx_revision_session_initiated_at 
ON core_service.revision_sessions(initiated_at);

CREATE INDEX IF NOT EXISTS idx_error_reports_revision_session 
ON core_service.syllabus_error_reports(revision_session_id);

CREATE INDEX IF NOT EXISTS idx_error_reports_resolved_version 
ON core_service.syllabus_error_reports(resolved_in_version_id);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION core_service.update_revision_session_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_revision_session_timestamp
BEFORE UPDATE ON core_service.revision_sessions
FOR EACH ROW
EXECUTE FUNCTION core_service.update_revision_session_timestamp();

-- Comments
COMMENT ON TABLE core_service.revision_sessions IS 
'Tracks revision sessions for fixing published syllabi based on student feedback. Allows batching multiple feedbacks into one revision cycle.';

COMMENT ON COLUMN core_service.revision_sessions.session_number IS 
'Sequential number for this syllabus (1, 2, 3...). First revision session is 1.';

COMMENT ON COLUMN core_service.revision_sessions.status IS 
'OPEN: Collecting feedback, IN_PROGRESS: Lecturer working, PENDING_HOD: Waiting HOD approval, COMPLETED: Republished, CANCELLED: Session cancelled';

COMMENT ON COLUMN core_service.syllabus_error_reports.revision_session_id IS 
'Which revision session is handling this feedback. Null if not yet assigned to a session.';

COMMENT ON COLUMN core_service.syllabus_error_reports.resolved_in_version_id IS 
'Which version resolved this feedback. Set when admin republishes.';
