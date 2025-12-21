# SMD Shared Java Common

Shared DTOs, Enums, and Constants for SMD Microservices (Gateway, Core Service).

## 📦 Package Structure

```
vn.edu.smd.shared/
├── constants/          # API constants, error codes, validation messages
│   ├── ApiConstants.java
│   ├── ErrorCodes.java
│   ├── ValidationMessages.java
│   └── BusinessConstants.java
├── enums/              # Shared enumerations
│   ├── UserRole.java
│   ├── SyllabusStatus.java
│   ├── ApprovalAction.java
│   ├── Gender.java
│   ├── AuthProvider.java
│   ├── SubjectType.java
│   └── UserStatus.java
└── dto/                # Data Transfer Objects
    ├── common/         # Common response structures
    │   ├── ApiResponse.java
    │   ├── PaginatedResponse.java
    │   └── ErrorResponse.java
    ├── auth/           # Authentication DTOs
    │   ├── LoginRequest.java
    │   ├── LoginResponse.java
    │   ├── RegisterRequest.java
    │   ├── RefreshTokenRequest.java
    │   └── RefreshTokenResponse.java
    ├── user/           # User DTOs
    │   └── UserDTO.java
    ├── syllabus/       # Syllabus DTOs
    │   ├── SyllabusListDTO.java
    │   ├── SyllabusDetailDTO.java
    │   ├── SyllabusCreateRequest.java
    │   └── SyllabusApprovalRequest.java
    ├── academic/       # Academic entity DTOs
    │   ├── FacultyDTO.java
    │   ├── DepartmentDTO.java
    │   ├── SubjectDTO.java
    │   ├── CurriculumDTO.java
    │   └── AcademicTermDTO.java
    ├── assessment/     # Learning outcomes & assessment
    │   ├── PLODTO.java
    │   ├── CLODTO.java
    │   ├── AssessmentSchemeDTO.java
    │   └── CloPlOMappingDTO.java
    ├── notification/   # Notification DTOs
    │   └── NotificationDTO.java
    ├── feedback/       # Feedback DTOs
    │   └── FeedbackDTO.java
    └── audit/          # Audit log DTOs
        └── AuditLogDTO.java
```

## 🔧 Build & Install

### Install to local Maven repository:
```bash
cd backend/shared/java-common
mvn clean install
```

### Use in other projects (pom.xml):
```xml
<dependency>
    <groupId>vn.edu.smd</groupId>
    <artifactId>shared-java-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## ✅ Features

### 1. **Bean Validation**
All DTOs include Jakarta Bean Validation annotations:
- `@NotBlank`, `@NotNull` - Required fields
- `@Email` - Email format validation
- `@Pattern` - Regex pattern validation (e.g., password strength)
- `@Size` - String length constraints

Example:
```java
@NotBlank(message = "Email không được để trống")
@Email(message = "Email không đúng định dạng")
private String email;

@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
    message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số"
)
private String password;
```

### 2. **Lombok Integration**
Reduces boilerplate code:
- `@Data` - Getters, setters, toString, equals, hashCode
- `@Builder` - Builder pattern
- `@NoArgsConstructor`, `@AllArgsConstructor` - Constructors

### 3. **JSON Serialization**
Jackson annotations for API responses:
- `@JsonInclude(Include.NON_NULL)` - Exclude null fields
- `@JsonFormat` - Date/time formatting
- `@JsonProperty` - Custom field names

### 4. **Standard Response Wrappers**

**Success Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": { ... },
  "timestamp": "2025-12-20T10:30:00"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "errorCode": "VALIDATION_ERROR",
    "status": 400,
    "validationErrors": {
      "email": "Email không được để trống",
      "password": "Mật khẩu quá ngắn"
    }
  },
  "timestamp": "2025-12-20T10:30:00"
}
```

**Paginated Response:**
```json
{
  "data": [...],
  "page": 1,
  "pageSize": 10,
  "total": 150,
  "totalPages": 15,
  "hasNext": true,
  "hasPrevious": false
}
```

## 📝 Usage Examples

### Creating API Response:
```java
// Success with data
ApiResponse<UserDTO> response = ApiResponse.success(userDTO);

// Success with message
ApiResponse<Void> response = ApiResponse.success("User deleted successfully");

// Error
ApiResponse<Void> response = ApiResponse.error("User not found");
```

### Paginated Results:
```java
List<SyllabusListDTO> syllabi = ...;
PaginatedResponse<SyllabusListDTO> response = 
    PaginatedResponse.of(syllabi, page, pageSize, totalCount);
```

### Validation:
```java
@PostMapping("/auth/login")
public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // If validation fails, Spring will automatically return 400 with error details
    // ... login logic
}
```

## 🔐 Password Validation

Password requirements (defined in `BusinessConstants`):
- Minimum 8 characters
- Maximum 100 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number

Pattern: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$`

## 📚 Documentation

For detailed API documentation, see:
- [API Specification](../../docs/api/)
- [Database Schema](../../docs/database/)
- [Architecture Decision Records](../../docs/architecture/decision-records/)

## 🤝 Contributing

When adding new DTOs:
1. Follow existing package structure
2. Include validation annotations
3. Add Lombok annotations (`@Data`, `@Builder`)
4. Use `@JsonInclude(NON_NULL)` for optional fields
5. Document with JavaDoc comments
6. Update this README

## 📄 License

Copyright © 2025 SMD Team. All rights reserved.
