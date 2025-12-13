# ADR 001: Sử dụng Monorepo Architecture

## Trạng thái
Accepted

## Ngày quyết định
2025-12-12

## Người quyết định
SMD Development Team

## Bối cảnh

Dự án SMD (Syllabus Management & Digitalization) bao gồm nhiều thành phần:
- Frontend (React)
- Gateway Service (Spring Cloud Gateway)
- Core Service (Java Spring Boot)
- AI Service (Python FastAPI)
- Shared Libraries (Java & Python)
- Infrastructure configurations

Chúng ta cần quyết định cách tổ chức code: **Monorepo** (tất cả trong 1 repository) hoặc **Multi-repo** (mỗi service 1 repository riêng).

## Quyết định

Chúng ta quyết định sử dụng **Monorepo** cho dự án SMD.

## Lý do

### Ưu điểm của Monorepo cho dự án này:

1. **Đơn giản hóa quản lý**
   - Chỉ cần 1 lần `git clone`
   - Tất cả code ở một nơi, dễ tìm kiếm
   - Phù hợp với team nhỏ (4-5 người)

2. **Atomic Changes**
   - Thay đổi API có thể cập nhật cả Backend lẫn Frontend trong 1 commit
   - Shared DTOs/Constants được đồng bộ ngay lập tức
   - Dễ review code liên quan đến nhiều service

3. **Triển khai đơn giản**
   - Một lệnh `docker-compose up` khởi động toàn bộ hệ thống
   - CI/CD pipeline tập trung
   - Versioning đồng bộ giữa các service

4. **Chia sẻ code dễ dàng**
   - Shared libraries (DTOs, Constants, Enums) dùng chung
   - Tránh duplicate code giữa services
   - Refactoring dễ dàng hơn

5. **Developer Experience tốt hơn**
   - Setup môi trường dev chỉ 1 lần
   - IDE indexing toàn bộ codebase
   - Debugging cross-service đơn giản

### Nhược điểm đã cân nhắc:

1. **Repository lớn**
   - Giải pháp: Git LFS cho binary files
   - Với project size hiện tại (~100MB sau build) là chấp nhận được

2. **Build time lâu**
   - Giải pháp: Build selective trong CI/CD (chỉ build service thay đổi)
   - Cache Docker layers

3. **Quyền truy cập khó phân chia**
   - Không phải vấn đề với team nhỏ
   - Nếu cần, có thể dùng CODEOWNERS file

## Hậu quả

### Tích cực:
- Team làm việc hiệu quả hơn
- Onboarding thành viên mới nhanh hơn
- Giảm overhead quản lý dependencies

### Tiêu cực (có thể):
- Clone repo lần đầu lâu hơn
- Cần quy ước rõ ràng về cấu trúc thư mục

## Alternatives đã xem xét

### Multi-repo
- Mỗi service 1 repo riêng
- [REJECTED] Phức tạp quá mức với team nhỏ
- [REJECTED] Khó đồng bộ changes giữa services
- [REJECTED] CI/CD phải setup nhiều lần

### Hybrid (Mono + Multi)
- Core services trong monorepo, infrastructure riêng
- [REJECTED] Thêm complexity không cần thiết

## Ghi chú

Quyết định này có thể được review lại khi:
- Team mở rộng > 15 người
- Cần phân quyền truy cập rõ ràng giữa các team
- Repository size > 1GB

## Tham khảo

- [Google's Monorepo Philosophy](https://research.google/pubs/pub45424/)
- [Monorepo vs Polyrepo](https://github.com/joelparkerhenderson/monorepo-vs-polyrepo)
