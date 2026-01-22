-- =====================================================
-- Migration V49: Add Firebase Cloud Messaging tokens
-- =====================================================
-- Purpose: Store FCM tokens for push notifications
-- Author: GitHub Copilot
-- Date: 2026-01-17

-- Add FCM token columns to users table
ALTER TABLE core_service.users 
ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(512),
ADD COLUMN IF NOT EXISTS fcm_token_updated_at TIMESTAMP;

-- Create index for faster token lookups
CREATE INDEX IF NOT EXISTS idx_users_fcm_token 
ON core_service.users(fcm_token) 
WHERE fcm_token IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN core_service.users.fcm_token IS 'Firebase Cloud Messaging token for realtime push notifications';
COMMENT ON COLUMN core_service.users.fcm_token_updated_at IS 'Last time FCM token was updated';
