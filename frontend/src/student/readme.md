🛠️ 1. Cài đặt cần thiết
Máy phải cài sẵn:

Docker Desktop (để chạy Database)

Java 17 & Maven

Node.js

📥 2. Lấy code về
Mở Terminal (hoặc CMD/VS Code) tại thư mục muốn lưu dự án:

Bash

git pull origin main
🗄️ 3. Bật Database
Mở Terminal ngay tại thư mục gốc của dự án (chỗ có file docker-compose.yml), chạy lệnh:

Bash

docker compose up -d postgres
(Chờ 10-20 giây cho Database khởi động)

☕ 4. Chạy Backend (Quan trọng)
Phải chạy đúng 2 lệnh này theo thứ tự:

Bước 4.1: Cài đặt thư viện chung (Chạy tại thư mục backend)

Bash

cd backend
mvn clean install -DskipTests
(Đợi báo BUILD SUCCESS thì qua bước tiếp)

Bước 4.2: Khởi động Server (Chạy tại thư mục backend/core-service)

Bash

cd core-service
mvn spring-boot:run
(Thấy dòng chữ Started CoreServiceApplication... là thành công. Lúc này tài khoản và dữ liệu mẫu đã tự động được tạo).

⚛️ 5. Chạy Frontend
Mở thêm một tab Terminal mới (đừng tắt cái đang chạy Backend):

Bash

cd frontend
npm install
npm run dev:student
Trang web sẽ tự mở tại: http://localhost:3000

🔑 6. Tài khoản đăng nhập (Có sẵn)
Mật khẩu chung cho tất cả là: password123

Sinh viên: student@smd.edu.vn (Dùng để xem đề cương, báo lỗi)

Admin: admin@smd.edu.vn

Giảng viên: gv.nguyen@smd.edu.vn
