/*
 * V34__fix_component_type_enum.sql
 * Fix component_type ENUM casting issue in syllabus_versions
 * Solution: Add trigger to automatically cast VARCHAR to ENUM type
 */

SET search_path TO core_service;

-- Create a PL/pgSQL function to convert string values to proper ENUM types
CREATE OR REPLACE FUNCTION cast_enum_on_insert_update()
RETURNS TRIGGER AS $$
BEGIN
  -- Cast course_type from VARCHAR to course_type ENUM if needed
  IF NEW.course_type IS NOT NULL THEN
    BEGIN
      NEW.course_type := NEW.course_type::text::course_type;
    EXCEPTION WHEN OTHERS THEN
      -- If cast fails, use default
      NEW.course_type := 'required'::course_type;
    END;
  END IF;
  
  -- Cast component_type from VARCHAR to component_type ENUM if needed
  IF NEW.component_type IS NOT NULL THEN
    BEGIN
      NEW.component_type := NEW.component_type::text::component_type;
    EXCEPTION WHEN OTHERS THEN
      -- If cast fails, use default
      NEW.component_type := 'major'::component_type;
    END;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop existing trigger if it exists
DROP TRIGGER IF EXISTS trg_enum_cast_syllabus ON syllabus_versions;

-- Create trigger to apply casting on INSERT and UPDATE
CREATE TRIGGER trg_enum_cast_syllabus
BEFORE INSERT OR UPDATE ON syllabus_versions
FOR EACH ROW
EXECUTE FUNCTION cast_enum_on_insert_update();

-- Set proper defaults with explicit casting
ALTER TABLE syllabus_versions 
  ALTER COLUMN course_type SET DEFAULT 'required'::course_type,
  ALTER COLUMN component_type SET DEFAULT 'major'::component_type;
