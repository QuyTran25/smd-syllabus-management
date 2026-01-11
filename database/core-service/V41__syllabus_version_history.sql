-- ==========================================
-- V40: Syllabus Version History Table
-- ==========================================
-- Purpose: Store snapshots of syllabus versions for comparison and rollback
-- Single Active Record approach: Only one active syllabus per subject/term

-- Add version_number field to syllabus_versions
ALTER TABLE core_service.syllabus_versions 
ADD COLUMN IF NOT EXISTS version_number INTEGER DEFAULT 1;

-- Create index for version_number
CREATE INDEX IF NOT EXISTS idx_syllabus_version_number 
ON core_service.syllabus_versions(version_number);

-- Create syllabus version history table
CREATE TABLE IF NOT EXISTS core_service.syllabus_version_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Reference to current syllabus version
    syllabus_id UUID NOT NULL REFERENCES core_service.syllabus_versions(id) ON DELETE CASCADE,
    
    -- Version information
    version_number INTEGER NOT NULL,
    version_no VARCHAR(20) NOT NULL,
    
    -- Snapshot of status at time of saving
    status core_service.syllabus_status NOT NULL,
    
    -- Full content snapshot (JSONB for flexibility)
    content JSONB DEFAULT '{}',
    
    -- Snapshot metadata
    keywords TEXT[] DEFAULT '{}',
    description TEXT,
    objectives TEXT,
    student_tasks TEXT,
    student_duties TEXT,
    
    -- Snapshot of curriculum info
    snap_subject_code VARCHAR(20),
    snap_subject_name_vi VARCHAR(255),
    snap_subject_name_en VARCHAR(255),
    snap_credit_count INT,
    
    -- Course details snapshot
    course_type VARCHAR(20),
    component_type VARCHAR(20),
    theory_hours INTEGER,
    practice_hours INTEGER,
    self_study_hours INTEGER,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES core_service.users(id),
    
    -- Reason for creating this snapshot (rejection, revision, etc.)
    snapshot_reason VARCHAR(100)
);

-- Indexes for performance
CREATE INDEX idx_history_syllabus_id ON core_service.syllabus_version_history(syllabus_id);
CREATE INDEX idx_history_version_number ON core_service.syllabus_version_history(version_number);
CREATE INDEX idx_history_created_at ON core_service.syllabus_version_history(created_at);

-- Comment on table
COMMENT ON TABLE core_service.syllabus_version_history IS 
'Stores historical snapshots of syllabus versions for comparison, rollback, and audit purposes. Each snapshot is created when a version is rejected and before revision begins.';

COMMENT ON COLUMN core_service.syllabus_version_history.snapshot_reason IS 
'Reason for snapshot: REJECTED_BY_HOD, REJECTED_BY_AA, REJECTED_BY_PRINCIPAL, BEFORE_REVISION, etc.';
