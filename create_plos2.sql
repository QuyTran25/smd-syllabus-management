-- Create PLOs with correct enum values
INSERT INTO core_service.plos (curriculum_id, code, description, category) VALUES
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO1', 'Áp dụng kiến thức toán học, khoa học và kỹ thuật để giải quyết vấn đề CNTT', 'Knowledge'),
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO2', 'Thiết kế và phát triển hệ thống phần mềm đáp ứng yêu cầu kỹ thuật', 'Knowledge'),
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO3', 'Phân tích và đánh giá các giải pháp công nghệ phù hợp với bối cảnh thực tế', 'Skills'),
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO4', 'Làm việc nhóm hiệu quả trong môi trường đa văn hóa', 'Skills'),
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO5', 'Giao tiếp chuyên nghiệp bằng văn bản và thuyết trình', 'Skills'),
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO6', 'Tuân thủ đạo đức nghề nghiệp và trách nhiệm xã hội', 'Attitude'),
('a1b2c3d4-e5f6-4789-abcd-ef0123456789', 'PLO7', 'Tự học và phát triển bản thân trong suốt sự nghiệp', 'Competence');

SELECT id, code, description FROM core_service.plos;
