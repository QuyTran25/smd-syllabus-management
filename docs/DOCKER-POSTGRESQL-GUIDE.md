# THAO TAC DOCKER CAN BIET KHI DUNG POSTGRESQL CONTAINER

## 1. Khoi chay PostgreSQL container voi Docker Compose

**Lan dau tien (khoi tao moi):**
```powershell
docker-compose up -d
```
Hoac chi start infrastructure services:
```powershell
.\scripts\start-infrastructure.ps1
```

**Cac lan tiep theo (sau khi da co container):**
```powershell
docker-compose up -d
```

**Build lai application images (khi co thay doi code):**
```powershell
docker-compose up --build -d gateway core-service ai-service frontend
```

**Dung container:**
```powershell
docker-compose down
```

---

## 2. Vao container PostgreSQL de thao tac truc tiep

**Ket noi vao PostgreSQL:**
```powershell
docker exec -it smd-postgres psql -U smd_user -d smd_database
```

**Cac lenh PostgreSQL quan trong:**

```sql
-- Xem tat ca schemas
\dn

-- Chuyen sang schema cu the
SET search_path TO core_service;

-- Xem tat ca tables trong schema hien tai
\dt

-- Xem chi tiet cau truc bang
\d table_name

-- Tao bang moi
CREATE TABLE core_service.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- Them du lieu
INSERT INTO core_service.users (username, email) 
VALUES ('admin', 'admin@example.com');

-- Cap nhat du lieu
UPDATE core_service.users 
SET email = 'newemail@example.com' 
WHERE username = 'admin';

-- Xoa du lieu
DELETE FROM core_service.users WHERE id = 1;

-- Thoat khoi PostgreSQL
\q
```

**Luu y:** Cach nay tien hon vi khong phai khoi dong lai container.

---

## 3. Thay doi file init-db.sh (Reset database tu dau)

File `infrastructure/postgresql/init-db.sh` dung de:
- Tao 2 schemas: `core_service` va `ai_service`
- Cai dat extension pgvector (cho AI embeddings)
- Gan quyen truy cap

**Luu y quan trong:**
- File `init-db.sh` CHI CHAY KHI CONTAINER MOI DUOC TAO LAN DAU
- Neu container da ton tai, script nay KHONG CHAY LAI

**Neu muon thay doi init-db.sh va ap dung lai:**

1. Dung container:
```powershell
docker-compose down
```

2. Xoa volume du lieu (SE XOA TAT CA DATA!):
```powershell
docker volume rm smd-syllabus-management_postgres_data
```

3. Khoi dong lai de chay init-db.sh:
```powershell
docker-compose up -d postgres
```

**Hoac xoa tat ca va reset toan bo (CANH TRỌNG!):**
```powershell
.\scripts\clean-docker.ps1
```

---

## 4. Quan ly Database Schema voi Flyway (Production)

Thay vi sua init-db.sh, nen dung **Flyway migrations** cho moi thay doi schema:

**Tao migration file moi:**
```
backend/core-service/src/main/resources/db/migration/V1__create_users_table.sql
backend/core-service/src/main/resources/db/migration/V2__add_roles_table.sql
```

**Flyway tu dong chay cac migration khi Core Service khoi dong.**

---

## 5. Cac lenh Docker huu ich

**Xem logs cua PostgreSQL:**
```powershell
docker-compose logs -f postgres
```

**Xem tat ca containers dang chay:**
```powershell
docker-compose ps
```

**Kiem tra health cua PostgreSQL:**
```powershell
docker exec smd-postgres pg_isready -U smd_user
```

**Xem danh sach schemas:**
```powershell
docker exec smd-postgres psql -U smd_user -d smd_database -c "\dn"
```

**Backup database:**
```powershell
docker exec smd-postgres pg_dump -U smd_user smd_database > backup.sql
```

**Restore database:**
```powershell
Get-Content backup.sql | docker exec -i smd-postgres psql -U smd_user -d smd_database
```

---

## 6. Troubleshooting

**PostgreSQL khong start:**
- Kiem tra port 5432 co bi chiem khong: `netstat -ano | findstr ":5432"`
- Xem logs: `docker-compose logs postgres`

**Khong ket noi duoc database:**
- Kiem tra `.env` co dung thong tin: `DB_HOST=postgres`, `DB_PORT=5432`
- Test connection: `.\scripts\test-connections.ps1`

**Volume bi loi:**
- Xoa volume: `docker volume rm smd-syllabus-management_postgres_data`
- Khoi dong lai: `docker-compose up -d postgres`
