-- V17__add_prerequisites.sql
-- Add prerequisite relationships for subjects

SET search_path TO core_service;

-- Add prerequisites (Môn tiên quyết)
INSERT INTO subject_relationships (subject_id, related_subject_id, type)
SELECT 
    s1.id as subject_id,
    s2.id as related_subject_id,
    'PREREQUISITE'::subject_relation_type as type
FROM subjects s1, subjects s2
WHERE (s1.code, s2.code) IN (
    -- Calculus II requires Calculus I
    ('122003', '122002'),
    
    -- Data Structures requires Programming Fundamentals
    ('124002', '124001'),
    
    -- Algorithms requires Data Structures
    ('123013', '124002'),
    
    -- Database Management requires Data Structures
    ('123033', '124002'),
    
    -- Object-Oriented Programming requires Programming Fundamentals
    ('122042', '124001')
)
AND NOT EXISTS (
    SELECT 1 FROM subject_relationships sr
    WHERE sr.subject_id = s1.id 
    AND sr.related_subject_id = s2.id 
    AND sr.type = 'PREREQUISITE'::subject_relation_type
);
