#!/bin/bash
set -e

# ============================================
# PostgreSQL Initialization Script
# Creates schemas for Core Service and AI Service
# ============================================

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create schemas
    CREATE SCHEMA IF NOT EXISTS core_service;
    CREATE SCHEMA IF NOT EXISTS ai_service;

    -- Grant privileges
    GRANT ALL PRIVILEGES ON SCHEMA core_service TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON SCHEMA ai_service TO $POSTGRES_USER;

    -- Install pgvector extension for AI embeddings (if needed)
    CREATE EXTENSION IF NOT EXISTS vector;

    -- Log completion
    SELECT 'Database schemas created successfully!' AS status;
EOSQL

echo "PostgreSQL initialization completed!"
