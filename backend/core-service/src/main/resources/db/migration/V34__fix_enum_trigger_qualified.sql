SET search_path TO core_service;

CREATE OR REPLACE FUNCTION core_service.cast_enum_on_insert_update()
RETURNS TRIGGER AS $$
BEGIN
  -- Cast course_type from VARCHAR to course_type ENUM if needed
  IF NEW.course_type IS NOT NULL THEN
    BEGIN
      NEW.course_type := NEW.course_type::text::core_service.course_type;
    EXCEPTION WHEN OTHERS THEN
      -- If cast fails, use default
      NEW.course_type := 'required'::core_service.course_type;
    END;
  END IF;
  
  -- Cast component_type from VARCHAR to component_type ENUM if needed
  IF NEW.component_type IS NOT NULL THEN
    BEGIN
      NEW.component_type := NEW.component_type::text::core_service.component_type;
    EXCEPTION WHEN OTHERS THEN
      -- If cast fails, use default
      NEW.component_type := 'major'::core_service.component_type;
    END;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
