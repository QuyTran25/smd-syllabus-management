import axios from 'axios';
import { STORAGE_KEYS } from '@/constants';

const gatewayUrl = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';
const axiosTimeout = Number(import.meta.env.VITE_API_TIMEOUT) || 10000;

const axiosClient = axios.create({
    baseURL: gatewayUrl.endsWith('/') ? `${gatewayUrl}api` : `${gatewayUrl}/api`,
    timeout: axiosTimeout,
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosClient.interceptors.request.use(
    (config) => {
        const tokenKey = STORAGE_KEYS.ACCESS_TOKEN; // centralized key
        const token = localStorage.getItem(tokenKey);

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor - log server errors for debugging Save Draft / permissions
axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            console.error(`[AXIOS ERROR] Status: ${error.response.status}`);
            console.error('[AXIOS ERROR] Data:', error.response.data);

            if (error.response.status === 401) {
                // token invalid/expired — optional handling
                // window.location.href = '/login';
            }
            if (error.response.status === 403) {
                // forbidden — user lacks permission
                console.warn('[AXIOS ERROR] Forbidden: user may lack permission');
            }
        } else if (error.request) {
            console.error('[AXIOS ERROR] No response received from server');
        } else {
            console.error('[AXIOS ERROR] Message:', error.message);
        }
        return Promise.reject(error);
    }
);

export default axiosClient;