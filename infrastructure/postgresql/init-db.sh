#!/bin/bash
set -e

# ============================================
# PostgreSQL Initialization Script
# Creates schemas for Core Service and AI Service
# Then runs all migration files automatically
# ============================================

echo "🚀 Starting PostgreSQL initialization..."

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

echo "✅ Schemas created!"

# ============================================
# NOTE: Migrations are handled by Flyway in Spring Boot
# Docker init only creates schemas, NOT running migrations
# This ensures consistency when team members pull the code
# ============================================
echo "⚠️  Skipping migrations - Flyway will handle them"

# ============================================
# AI Service: Keep for pgvector-specific setup if needed
# ============================================
echo "📦 Checking AI Service setup..."

AI_MIGRATIONS_DIR="/docker-entrypoint-initdb.d/migrations/ai-service"

# AI Service migrations also handled by Flyway (if AI service uses Flyway)
# Or keep them here if AI service doesn't use Flyway
echo "⚠️  AI Service migrations also handled by application"

echo "🎉 PostgreSQL initialization completed!"
