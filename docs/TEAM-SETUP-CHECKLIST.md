# Setup Checklist cho Team Members

## ✅ Pre-requisites (Kiểm tra trước khi bắt đầu)

- [ ] Docker Desktop đã cài đặt và đang chạy
- [ ] Java JDK 17+ đã cài đặt (`java -version`)
- [ ] Maven 3.6+ đã cài đặt (`mvn -version`)
- [ ] Git đã cài đặt
- [ ] Port 5432 (PostgreSQL) chưa bị sử dụng
- [ ] Port 8081 (Backend) chưa bị sử dụng

## 📦 Bước 1: Clone Repository

```powershell
git clone <repository-url>
cd smd-syllabus-management
```

- [ ] Repository đã clone thành công
- [ ] Đã cd vào thư mục project

## 🐳 Bước 2: Start Docker Services

```powershell
# Start PostgreSQL
docker-compose up -d postgres

# Kiểm tra container đã chạy
docker ps
```

**Kết quả mong đợi**: Thấy container `smd-postgres` với status `Up`

- [ ] Container PostgreSQL đã chạy
- [ ] Port 5432 đã được expose

## 🗄️ Bước 3: Setup Database

```powershell
# Chạy migrations tự động
.\scripts\run-migrations.ps1
```

**Kết quả mong đợi**:
- `CREATE EXTENSION uuid-ossp` thành công
- Migrations V1-V8 chạy thành công
- Migration V9 seed data thành công
- Thấy message: "INSERT 0 5", "INSERT 0 9", "INSERT 0 45"

```powershell
# Verify database
docker exec smd-postgres psql -U smd_user -d smd_database -c "SELECT COUNT(*) FROM core_service.subjects;"
```

**Kết quả mong đợi**: 45 rows

- [ ] Migrations đã chạy thành công
- [ ] Database có 32 bảng
- [ ] Seed data có 45 môn học

## 🔧 Bước 4: Build Java Common Module

```powershell
cd backend\shared\java-common
mvn clean install -DskipTests
```

**Kết quả mong đợi**: `BUILD SUCCESS`

- [ ] java-common đã build thành công
- [ ] JAR đã được install vào Maven local repository

## 🚀 Bước 5: Build và Chạy Core Service

```powershell
cd ..\..\core-service
mvn clean package -DskipTests
```

**Kết quả mong đợi**: `BUILD SUCCESS` và file `target/core-service-1.0.0.jar` được tạo

- [ ] Core service đã build thành công
- [ ] JAR file tồn tại

```powershell
java -jar target\core-service-1.0.0.jar
```

**Kết quả mong đợi**: 
- Thấy log "Started CoreServiceApplication in X seconds"
- Tomcat chạy trên port 8081

- [ ] Application khởi động thành công
- [ ] Không có lỗi trong logs

## ✅ Bước 6: Verify Setup

### Test 1: Health Check

Mở browser hoặc dùng curl:
```powershell
curl http://localhost:8081/actuator/health
```

**Kết quả mong đợi**:
```json
{"status":"UP"}
```

- [ ] Health check trả về status UP

### Test 2: Database Connection

Trong application logs, tìm dòng:
- `HikariPool-1 - Start completed`
- `Initialized JPA EntityManagerFactory`

- [ ] Connection pool đã khởi tạo thành công
- [ ] JPA đã connect database

### Test 3: Query Database

```powershell
docker exec smd-postgres psql -U smd_user -d smd_database -c "SELECT code, current_name_vi FROM core_service.subjects LIMIT 5;"
```

**Kết quả mong đợi**: Thấy danh sách môn học với tiếng Việt

- [ ] Có thể query database
- [ ] Dữ liệu tiếng Việt hiển thị đúng

## 🎉 Hoàn Thành!

Nếu tất cả checkbox đều ✅, setup của bạn đã thành công!

## 🐛 Troubleshooting

### Lỗi "password authentication failed"

```powershell
# Kiểm tra password trong Docker
docker inspect smd-postgres | Select-String "POSTGRES_PASSWORD"

# Update application.properties nếu cần
# File: backend/core-service/src/main/resources/application.properties
# Line: spring.datasource.password=<password-từ-docker>
```

### Lỗi "cannot find symbol" khi build

```powershell
# Build lại java-common
cd backend\shared\java-common
mvn clean install -DskipTests -U
```

### Container PostgreSQL không start

```powershell
# Xem logs
docker logs smd-postgres

# Stop và start lại
docker-compose down
docker-compose up -d postgres
```

### Port 8081 bị chiếm

```powershell
# Tìm process đang dùng port
netstat -ano | findstr :8081

# Kill process (thay <PID> bằng process ID)
taskkill /PID <PID> /F
```

## 📚 Tài Liệu Bổ Sung

- [SETUP-GUIDE.md](./docs/SETUP-GUIDE.md) - Hướng dẫn chi tiết
- [QUICK-START.md](./QUICK-START.md) - Setup nhanh 5 phút
- [Docker Guide](./docs/DOCKER-POSTGRESQL-GUIDE.md) - Hướng dẫn Docker

## 💬 Nhờ Trợ Giúp

Nếu gặp vấn đề không có trong troubleshooting:
1. Kiểm tra logs: `docker logs smd-postgres`
2. Kiểm tra application logs
3. Tạo issue với thông tin chi tiết về lỗi

---

**Next Steps**: 
- [ ] Đọc [Architecture Documentation](./docs/architecture/)
- [ ] Setup IDE (IntelliJ IDEA/VS Code)
- [ ] Tạo branch mới cho feature
- [ ] Chạy tests: `mvn test`
