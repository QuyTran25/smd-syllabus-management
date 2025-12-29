-- Create table for role change requests
CREATE TABLE IF NOT EXISTS core_service.role_change_requests (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  requested_role varchar(50) NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'PENDING',
  comment text,
  handled_by uuid,
  handled_at timestamp without time zone,
  created_at timestamp without time zone DEFAULT now()
);

ALTER TABLE core_service.role_change_requests
  ADD CONSTRAINT fk_role_request_user FOREIGN KEY (user_id) REFERENCES core_service.users(id);
