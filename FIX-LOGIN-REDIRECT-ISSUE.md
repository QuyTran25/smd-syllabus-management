# ✅ ĐÃ FIX: Vấn đề Đăng Nhập và Redirect về Login

## 🐛 Vấn đề gặp phải

### 1. **Warning: Static function can not consume context**
```
Warning: [antd: message] Static function can not consume context like dynamic theme. 
Please use 'App' component instead.
```

### 2. **Bấm menu bị văng về trang login**
- Đăng nhập thành công
- Nhưng khi bấm vào chức năng trong menu → Tự động quay về `/login`

---

## 🔍 Nguyên nhân

### Nguyên nhân 1: Sử dụng Antd message static (không qua context)
**File lỗi:** 
- `frontend/src/config/api-config.ts`
- `frontend/src/features/auth/AuthContext.tsx`

**Vấn đề:**
```typescript
// ❌ SAI - Import trực tiếp và dùng static
import { message } from 'antd';

message.success('Đăng nhập thành công');  // ← Warning!
```

**Lý do:** Ant Design v5 yêu cầu các hàm `message`, `notification`, `modal` phải được gọi qua `<App>` context để nhận được theme configuration.

---

### Nguyên nhân 2: Logic 401 quá nhạy cảm
**File lỗi:** `frontend/src/config/api-config.ts`

**Vấn đề:**
```typescript
// ❌ SAI - Logout quá sớm
if (error.response?.status === 401) {
  if (url.includes('/api/auth/login') || 
      url.includes('/api/auth/register') || 
      url.includes('/api/auth/me')) {
    // Clear storage và redirect về login
  }
}
```

**Lý do:** 
- Khi bấm menu, một số API có thể trả về 401 (ví dụ: permission denied)
- Code cũ sẽ kiểm tra nếu URL chứa `/api/auth/...` → Logout ngay
- Điều này làm mất token dù user chỉ đơn giản là không có quyền truy cập tài nguyên đó

---

### Nguyên nhân 3: Storage keys không đồng bộ
**Vấn đề:**
- `AuthContext.tsx` dùng: `'smd_auth_token'`
- `constants/index.ts` định nghĩa: `'smd_access_token'`

→ Token được lưu vào 1 key nhưng đọc từ key khác → Mất token!

---

## ✅ Giải pháp đã áp dụng

### Fix 1: Thay `message` static bằng `App.useApp()`

#### File: `frontend/src/features/auth/AuthContext.tsx`
```typescript
// ✅ ĐÚNG
import { App } from 'antd';  // ← Import App thay vì message

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const { message } = App.useApp();  // ← Lấy message từ App context
  
  // ... rest of code
  message.success('Đăng nhập thành công');  // ← Không còn warning!
};
```

#### File: `frontend/src/config/api-config.ts`
```typescript
// ✅ ĐÚNG - Xóa import message
// import { message } from 'antd';  // ← Đã xóa

// Response interceptor - Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // ⚠️ KHÔNG dùng message.error() ở đây nữa
    // Component sẽ tự handle và hiển thị message
    return Promise.reject(error);
  }
);
```

**Lưu ý:** Để `message` hoạt động được, cần bao bọc app trong `<App>` component (đã có trong `main.tsx`):
```typescript
// frontend/src/main.tsx
<AntdApp>
  <AuthProvider>
    <App />
  </AuthProvider>
</AntdApp>
```

---

### Fix 2: Chỉ logout khi token THẬT SỰ expired

#### File: `frontend/src/config/api-config.ts`
```typescript
// Response interceptor - Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // ✅ ĐÚNG - CHỈ logout khi /api/auth/me thất bại
    if (error.response?.status === 401) {
      const url = error.config?.url || '';
      
      // CHỈ clear storage KHI verify token (/api/auth/me) thất bại
      // Đây là dấu hiệu token thật sự expired hoặc invalid
      if (url.includes('/api/auth/me')) {
        console.log('❌ Token verification failed, logging out');
        localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER_DATA);
        window.location.href = '/login';
      } else {
        // Các API khác trả về 401: chỉ log, KHÔNG logout
        console.log('⚠️ API returned 401 but NOT /api/auth/me, user stays logged in');
      }
    }

    return Promise.reject(error);
  }
);
```

**Giải thích:**
- `/api/auth/me` là endpoint verify token → Nếu fail = token expired
- Các API khác (syllabus, user management...) có thể trả 401 vì permission → KHÔNG nên logout

---

### Fix 3: Đồng bộ storage keys

#### File: `frontend/src/features/auth/AuthContext.tsx`
```typescript
// ❌ SAI - Hard-code key names
const TOKEN_KEY = 'smd_auth_token';
const REFRESH_TOKEN_KEY = 'smd_refresh_token';

// ✅ ĐÚNG - Import từ constants
import { STORAGE_KEYS } from '@/constants';

// Sử dụng:
localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(user));
```

#### File: `frontend/src/constants/index.ts`
```typescript
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'smd_access_token',
  REFRESH_TOKEN: 'smd_refresh_token',
  USER_DATA: 'smd_user_data',
};
```

---

## 🧪 Cách test

### Test 1: Không còn warning Antd message
1. Mở Console (F12)
2. Đăng nhập
3. ✅ Không thấy warning: `Static function can not consume context`

### Test 2: Không bị văng về login khi bấm menu
1. Đăng nhập thành công
2. Bấm vào các menu items: Dashboard, User Management, Syllabus...
3. ✅ Không bị redirect về `/login`
4. ✅ Chỉ hiển thị thông báo nếu không có quyền truy cập

### Test 3: Token được lưu và giữ đúng cách
1. Đăng nhập
2. Mở F12 → Application → Local Storage
3. ✅ Thấy:
   - `smd_access_token`
   - `smd_refresh_token`
   - `smd_user_data`
4. Refresh trang (F5)
5. ✅ Vẫn ở trang đã đăng nhập (không bị logout)

---

## 📝 Tóm tắt thay đổi

| File | Thay đổi |
|------|----------|
| `frontend/src/features/auth/AuthContext.tsx` | • Import `App` từ antd<br>• Dùng `const { message } = App.useApp()`<br>• Dùng `STORAGE_KEYS` từ constants |
| `frontend/src/config/api-config.ts` | • Xóa `import { message } from 'antd'`<br>• Chỉ logout khi `/api/auth/me` fail<br>• Xóa các `message.error()` trong interceptor |

---

## 🎯 Kết quả

✅ **Không còn warning về Antd message**  
✅ **Không bị văng về login khi bấm menu**  
✅ **Token được lưu và quản lý đúng cách**  
✅ **Chỉ logout khi token thật sự expired**

---

## 📚 Tài liệu tham khảo

- [Ant Design v5 - App Component](https://ant.design/components/app)
- [Ant Design v5 - Static Methods](https://ant.design/components/message#why-not-use-static-methods)

---

**Ngày fix:** January 2, 2026  
**Developer:** GitHub Copilot
