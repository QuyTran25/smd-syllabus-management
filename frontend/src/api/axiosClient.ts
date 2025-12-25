import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8081/api', 
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosClient.interceptors.request.use(
    (config) => {
        // --- ĐOẠN DEBUG (Thêm vào để kiểm tra) ---
        const tokenKey = 'smd_auth_token'; // Tên key phải khớp với LocalStorage
        const token = localStorage.getItem(tokenKey); 
        
        console.log(`[DEBUG AXIOS] Đang tìm token với key: "${tokenKey}"`);
        console.log(`[DEBUG AXIOS] Kết quả token tìm được:`, token ? "ĐÃ TÌM THẤY (Có độ dài " + token.length + ")" : "KHÔNG TÌM THẤY (NULL)");
        // -----------------------------------------

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default axiosClient;