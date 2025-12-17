AI Service Database Analysis
1. Mục đích tài liệu

Tài liệu này phân tích thiết kế Database cho AI Service trong hệ thống quản lý đề cương học phần (Syllabus Management System – SMD).

Mục tiêu:

Giải thích vì sao database AI Service được thiết kế như hiện tại

Trình bày các quy tắc tách schema, stateless design và vector indexing

Minh họa luồng xử lý AI Jobs & Vector Embeddings

Làm căn cứ để bảo vệ đồ án và review kiến trúc

2. Phạm vi AI Service

AI Service chịu trách nhiệm cho các nghiệp vụ AI liên quan đến đề cương, bao gồm:

**Core AI Jobs:**
- `VECTORIZE` - Tạo vector embeddings cho Semantic Search / RAG
- `ANALYZE_BLOOM` - Phân tích Bloom's Taxonomy cho CLO
- `GENERATE_OUTLINE` - Tạo outline đề cương từ mô tả
- `CHECK_DUPLICATE` - Kiểm tra trùng lặp nội dung
- `RECOMMEND_SUBJECT` - Gợi ý môn học liên quan
- `MATCH_REVIEWER` - Đề xuất người review phù hợp

**Business Requirement Jobs (theo SYSTEM_ANALYSIS.md):**
- `DIFF_VERSIONS` - So sánh 2 phiên bản, highlight thay đổi (HoD/AA/Principal)
- `SUMMARIZE_SYLLABUS` - Tóm tắt đề cương cho sinh viên
- `SUGGEST_CLO` - Gợi ý CLO từ course description
- `VALIDATE_PLO_MAPPING` - Kiểm tra CLO-PLO alignment
- `EXTRACT_KEYWORDS` - Trích xuất keywords cho search
- `ANALYZE_PREREQUISITES` - Phân tích môn tiên quyết hợp lý

**Lưu trữ kết quả AI:**
- `version_diffs` - Kết quả so sánh phiên bản
- `syllabus_summaries` - Tóm tắt AI cho sinh viên
- `clo_suggestions` - Gợi ý CLO từ AI
- `plo_mapping_validations` - Kiểm tra CLO-PLO alignment
- `prerequisite_analyses` - Phân tích môn tiên quyết

❌ Không bao gồm:

Authentication / Authorization (tách ra Core Service)

Thực thi workflow, duyệt đề cương

Lưu trữ dữ liệu gốc ngoài AI payload

3. Nguyên tắc thiết kế
3.1 Stateless & Microservices

AI Service hoàn toàn độc lập: Chỉ lưu UUID tham chiếu syllabus_version_id từ Core Service, không có FK cứng

Payload snapshot: AI jobs lưu toàn bộ dữ liệu cần xử lý, giúp chạy được ngay cả khi Core Service offline

3.2 Tách Schema & Migration

V1__init_ai_service.sql: Tạo bảng + B-Tree index cơ bản, lightweight, phù hợp để insert dữ liệu seed nhanh

V2__vector_indexes.sql: Tạo HNSW index chuyên dụng cho Semantic Search, nặng, build sau khi seed xong

Giải thích: tách V2 giúp tránh circular dependency và cải thiện performance

3.3 Audit & Tracking

Các bảng ai_jobs và vector_embeddings có created_at / updated_at

Trigger update_timestamp() tự động cập nhật khi thay đổi

Bảng ai_logs ghi lại các message, level, metadata

4. Phân tích bảng chính
4.1 AI Jobs

Mục tiêu:

Quản lý hàng đợi xử lý bất đồng bộ

Theo dõi trạng thái job, input, output, retry, audit

Thiết kế nổi bật:

Cột	Mô tả
id	UUID PK
syllabus_version_id	UUID tham chiếu Core Service
type	Enum job_type (VECTORIZE, ANALYZE_BLOOM...)
status	Enum job_status (PENDING, PROCESSING, DONE, FAILED)
input_payload	JSONB snapshot dữ liệu
result	JSONB kết quả output
retry_count	Số lần retry
started_at, finished_at	Timestamp theo dõi performance
created_at, updated_at	Audit timestamp tự động

Index: syllabus_version_id, status, type (B-Tree)

4.2 Vector Embeddings

Mục tiêu:

Lưu vector embedding của các phần đề cương để phục vụ Semantic Search / RAG

Thiết kế nổi bật:

Cột	Mô tả
id	UUID PK
syllabus_version_id	UUID tham chiếu Core Service
section_type	Enum embedding_section_type (CLO, COURSE_DESCRIPTION...)
content_chunk	Nội dung gốc của đoạn text
embedding	vector(1536)
model_name	Tên model embedding
created_at, updated_at	Audit timestamp

Index:

B-Tree: syllabus_version_id, section_type

HNSW: embedding (tạo ở V2, hiệu năng cao cho Semantic Search)

Lý do tách HNSW:

Build index tốn CPU/RAM

Insert bulk data nhanh hơn nếu chưa có index

Sau khi seed xong, chạy V2 để build HNSW

4.3 AI Logs

Mục tiêu:

Debug và audit các jobs AI

Cột	Mô tả
id	UUID PK
job_id	FK ai_jobs(id)
level	INFO / WARN / ERROR
message	Nội dung log
metadata	JSONB chứa thông tin bổ sung
created_at	Timestamp
5. Luồng xử lý AI Jobs
```
1. Core Service gửi snapshot syllabus → tạo AI Job
2. AI Service lưu job vào ai_jobs (status=PENDING)
3. Worker pick job:
   ├─ Vectorize → lưu vào vector_embeddings
   ├─ Analyze Bloom / Check Duplicate → ghi result
   ├─ Diff Versions → lưu vào version_diffs
   ├─ Summarize Syllabus → lưu vào syllabus_summaries
   ├─ Suggest CLO → lưu vào clo_suggestions
   ├─ Validate PLO → lưu vào plo_mapping_validations
   └─ Analyze Prerequisites → lưu vào prerequisite_analyses
4. Ghi logs vào ai_logs
5. Cập nhật status (PROCESSING → DONE/FAILED)
6. Trigger update_timestamp() tự động cập nhật updated_at
7. Core Service có thể query result & vector embeddings
```

6. Ma trận Coverage: Nghiệp vụ vs Job Types

| Nghiệp vụ (SYSTEM_ANALYSIS.md) | Job Type | Bảng lưu kết quả |
|-------------------------------|----------|------------------|
| AI phát hiện thay đổi (HoD/AA/Principal) | `DIFF_VERSIONS` | `version_diffs` |
| Tóm tắt AI cho sinh viên | `SUMMARIZE_SYLLABUS` | `syllabus_summaries` |
| Gợi ý CLO từ mô tả môn học | `SUGGEST_CLO` | `clo_suggestions` |
| Kiểm tra CLO-PLO mapping | `VALIDATE_PLO_MAPPING` | `plo_mapping_validations` |
| Semantic Search đề cương | `VECTORIZE` | `vector_embeddings` |
| Kiểm tra trùng lặp | `CHECK_DUPLICATE` | `ai_jobs.result` |
| Đề xuất người review | `MATCH_REVIEWER` | `ai_jobs.result` |
| Phân tích môn tiên quyết | `ANALYZE_PREREQUISITES` | `prerequisite_analyses` |

7. Đánh giá Coverage

✅ **100% nghiệp vụ AI trong SYSTEM_ANALYSIS.md đã được cover**

| Tiêu chí | Status |
|----------|--------|
| AI phát hiện thay đổi phiên bản | ✅ `DIFF_VERSIONS` + `version_diffs` |
| Tóm tắt AI cho sinh viên | ✅ `SUMMARIZE_SYLLABUS` + `syllabus_summaries` |
| Semantic Search | ✅ `VECTORIZE` + `vector_embeddings` + HNSW index |
| Gợi ý CLO | ✅ `SUGGEST_CLO` + `clo_suggestions` |
| Kiểm tra PLO alignment | ✅ `VALIDATE_PLO_MAPPING` + `plo_mapping_validations` |
| Audit & Logging | ✅ `ai_logs` với `log_level` enum |

8. Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────┐
│                        AI SERVICE                                │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │    ai_jobs      │  │vector_embeddings│  │    ai_logs      │  │
│  │  (Job Queue)    │  │ (Semantic Search)│ │   (Audit)       │  │
│  └────────┬────────┘  └─────────────────┘  └─────────────────┘  │
│           │                                                      │
│  ┌────────┴───────────────────────────────────────────────────┐ │
│  │                   AI RESULT TABLES                          │ │
│  ├──────────────┬──────────────┬─────────────┬────────────────┤ │
│  │version_diffs │syllabus_     │clo_         │plo_mapping_    │ │
│  │              │summaries     │suggestions  │validations     │ │
│  └──────────────┴──────────────┴─────────────┴────────────────┘ │
│           │                          │                          │
│  ┌────────┴──────────────────────────┴────────────────────────┐ │
│  │             prerequisite_analyses                           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```