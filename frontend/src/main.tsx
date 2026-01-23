import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { ConfigProvider, App as AntdApp } from 'antd';
import viVN from 'antd/locale/vi_VN';
import dayjs from 'dayjs';
import 'dayjs/locale/vi';
import { AuthProvider } from './features/auth';
import App from './App';
import './index.css';
import './styles/tables.css';

// Configure dayjs
dayjs.locale('vi');

// Handle notification click from Service Worker
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'NOTIFICATION_CLICKED') {
      console.log('🔔 Navigation from notification:', event.data.url);
      window.location.href = event.data.url;
    }
  });
}

// Create TanStack Query client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

// SMD Theme configuration
const smdTheme = {
  token: {
    colorPrimary: '#018486',
    colorLink: '#1EA69A',
    fontSize: 17,
    fontFamily: '"Nunito", sans-serif',
    borderRadius: 6,
  },
  components: {
    Layout: {
      headerBg: '#018486',
      headerColor: '#ffffff',
      siderBg: '#ffffff',
      bodyBg: '#f5f5f5',
    },
    Menu: {
      itemSelectedBg: '#e6f7f7',
      itemSelectedColor: '#018486',
      itemHoverBg: '#f0fafa',
      itemHoverColor: '#018486',
    },
  },
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter
      future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true,
      }}
    >
      <QueryClientProvider client={queryClient}>
        <ConfigProvider locale={viVN} theme={smdTheme}>
          <AntdApp>
            <AuthProvider>
              <App />
            </AuthProvider>
          </AntdApp>
        </ConfigProvider>
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </BrowserRouter>
  </React.StrictMode>
);
