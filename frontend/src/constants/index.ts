// API Configuration
// ⭐ Gọi trực tiếp Core Service (8081) để test, khi deploy sẽ dùng Gateway
export const API_BASE_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8081';
export const API_TIMEOUT = 10000;

// Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'smd_access_token',
  REFRESH_TOKEN: 'smd_refresh_token',
  USER_DATA: 'smd_user_data',
};

// Pagination
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: ['10', '20', '50', '100'],
};

// Date Formats
export const DATE_FORMATS = {
  DISPLAY: 'DD/MM/YYYY',
  DISPLAY_TIME: 'DD/MM/YYYY HH:mm',
  API: 'YYYY-MM-DD',
};
