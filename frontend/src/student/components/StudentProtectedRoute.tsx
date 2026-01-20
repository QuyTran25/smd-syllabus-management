import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

// Hàm kiểm tra Token (Phiên bản Nâng cấp)
const isStudentLoggedIn = () => {
  // 1. Kiểm tra "mọi ngóc ngách" trong LocalStorage
  const studentToken = localStorage.getItem('student_token');
  const accessToken = localStorage.getItem('access_token'); // Key chuẩn của hệ thống

  // Debug: In ra để xem lúc bị lỗi thì token đang là gì
  // console.log("🔍 [RouteGuard] Checking Token:", { studentToken: !!studentToken, accessToken: !!accessToken });

  // 2. Chỉ cần 1 trong 2 có giá trị là coi như Đã đăng nhập
  return Boolean(studentToken || accessToken);
};

export default function StudentProtectedRoute({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const hasToken = isStudentLoggedIn();

  // 🛑 NẾU KHÔNG CÓ TOKEN -> CHẶN NGAY
  if (!hasToken) {
    console.error(
      '⛔ [StudentProtectedRoute] BLOCKING! Không thấy Token trong LocalStorage -> Chuyển về Login.'
    );

    // 👇 Nếu bạn muốn chắc chắn nó là thủ phạm, hãy bỏ comment dòng alert này:
    // alert("⛔ [StudentProtectedRoute] Dừng lại! Tôi sắp đá bạn về Login vì không thấy Token đâu cả!");

    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  // ✅ CÓ TOKEN -> CHO QUA
  return <>{children}</>;
}
