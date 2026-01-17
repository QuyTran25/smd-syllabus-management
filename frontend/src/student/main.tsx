import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntdApp } from 'antd'; // 1. Thêm cái này
import viVN from 'antd/locale/vi_VN'; // 2. Thêm tiếng Việt cho đồng bộ
import App from './App';
import '@/index.css';

// 3. QUAN TRỌNG: Import AuthProvider (Đảm bảo đúng đường dẫn tới file AuthContext.tsx bạn đã tạo)
import { AuthProvider } from '@/features/auth/AuthContext';

const queryClient = new QueryClient();

// Copy theme từ main chính sang cho đồng bộ màu sắc
const smdTheme = {
  token: {
    colorPrimary: '#018486',
    colorLink: '#1EA69A',
    fontSize: 17,
    fontFamily: '"Nunito", sans-serif',
    borderRadius: 6,
  },
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      {/* 4. Thêm ConfigProvider để nhận theme và tiếng Việt */}
      <ConfigProvider locale={viVN} theme={smdTheme}>
        {/* 5. Thêm AntdApp để dùng được hook App.useApp() trong Notification */}
        <AntdApp>
          <BrowserRouter>
            {/* 6. QUAN TRỌNG NHẤT: Bọc AuthProvider ở đây để sửa lỗi useAuth */}
            <AuthProvider>
              <App />
            </AuthProvider>
          </BrowserRouter>
        </AntdApp>
      </ConfigProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
