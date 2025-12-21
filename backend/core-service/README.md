# SMD Core Service

Core backend service cho hệ thống Quản lý Đề cương môn học (Syllabus Management System).

## ✅ Hoàn thành Phase 1: Foundation

### 1. ✅ Shared DTOs với Validation
- `@NotBlank`, `@Email`, `@Pattern` đã được implement trong `shared-java-common`
- Validation cho LoginRequest, RegisterRequest, SyllabusCreateRequest, etc.

### 2. ✅ Core Service Setup (pom.xml)
Dependencies đã được cấu hình:
- ✅ `spring-boot-starter-oauth2-resource-server` (bao gồm Nimbus JOSE+JWT)
- ✅ `spring-boot-starter-data-jpa`
- ✅ `spring-boot-starter-validation`
- ✅ `spring-boot-starter-security`
- ✅ `postgresql` driver
- ✅ `spring-boot-starter-data-redis`
- ✅ `spring-boot-starter-amqp` (RabbitMQ)
- ✅ `spring-kafka`
- ✅ `hypersistence-utils-hibernate-63` (JSONB support)
- ✅ `mapstruct` (DTO mapping)
- ✅ `springdoc-openapi` (Swagger UI)

### 3. ✅ Entities + Repositories

#### Entities (20 entities)
1. **Organization & Users (V1)**
   - `Faculty` - Khoa
   - `Department` - Bộ môn
   - `User` - Người dùng
   - `Role` - Vai trò
   - `UserRole` - Phân quyền theo scope

2. **Academic Identity (V2)**
   - `Curriculum` - Chương trình đào tạo
   - `Subject` - Môn học
   - `SubjectRelationship` - Quan hệ môn học (tiên quyết, song hành)

3. **Syllabus Versioning (V3)**
   - `AcademicTerm` - Học kỳ
   - `SyllabusVersion` - Phiên bản đề cương

4. **Outcomes & Assessment (V4)**
   - `PLO` - Program Learning Outcome
   - `CLO` - Course Learning Outcome
   - `CloPlOMapping` - Mapping CLO-PLO
   - `AssessmentScheme` - Phương pháp đánh giá
   - `GradingScale` - Thang điểm

5. **Workflow & Approval (V5)**
   - `ApprovalWorkflow` - Quy trình phê duyệt
   - `ApprovalHistory` - Lịch sử phê duyệt

6. **Collaboration & Feedback (V6)**
   - `SyllabusCollaborator` - Người cộng tác
   - `ReviewComment` - Nhận xét review
   - `SyllabusErrorReport` - Báo lỗi đề cương

7. **System & Audit (V7)**
   - `SystemSetting` - Cấu hình hệ thống
   - `Notification` - Thông báo
   - `AuditLog` - Nhật ký truy vết

#### Repositories (23 repositories)
Tất cả entities đều có repository tương ứng với các query methods phù hợp:
- `FacultyRepository`
- `DepartmentRepository`
- `UserRepository`
- `RoleRepository`
- `UserRoleRepository`
- `CurriculumRepository`
- `SubjectRepository`
- `SubjectRelationshipRepository`
- `AcademicTermRepository`
- `SyllabusVersionRepository`
- `PLORepository`
- `CLORepository`
- `CloPlOMappingRepository`
- `AssessmentSchemeRepository`
- `GradingScaleRepository`
- `ApprovalWorkflowRepository`
- `ApprovalHistoryRepository`
- `SyllabusCollaboratorRepository`
- `ReviewCommentRepository`
- `SyllabusErrorReportRepository`
- `SystemSettingRepository`
- `NotificationRepository`
- `AuditLogRepository`

## 📁 Cấu trúc dự án

```
backend/core-service/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── vn/edu/smd/core/
│       │       ├── CoreServiceApplication.java
│       │       ├── entity/          # 20 JPA entities
│       │       └── repository/      # 23 Spring Data repositories
│       └── resources/
│           └── application.properties
```

## 🔧 Chạy dự án

### Yêu cầu
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+
- Kafka 7.5+

### Build shared library trước
```bash
cd backend/shared/java-common
mvn clean install -DskipTests
```

### Build core service
```bash
cd backend/core-service
mvn clean package -DskipTests
```

### Chạy với Docker
```bash
docker-compose up core-service
```

## 🎯 Tiếp theo: Phase 2
- [ ] Security Configuration (OAuth2 Resource Server)
- [ ] Service Layer
- [ ] Controller Layer
- [ ] Exception Handling
- [ ] DTO Mappers (MapStruct)

## 📚 Tài liệu tham khảo
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
