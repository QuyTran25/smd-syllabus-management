/* 
 * DEBUG SCRIPT - Paste vào Console của trình duyệt
 * 
 * Chạy script này sau khi đăng nhập để kiểm tra trạng thái
 */

console.log('========================================');
console.log('🔍 KIỂM TRA TRẠNG THÁI ĐĂNG NHẬP');
console.log('========================================');

// 1. Kiểm tra localStorage
console.log('\n📦 LocalStorage:');
const token = localStorage.getItem('smd_access_token');
const refreshToken = localStorage.getItem('smd_refresh_token');
const userData = localStorage.getItem('smd_user_data');

console.log('  - smd_access_token:', token ? `✅ TỒN TẠI (${token.length} chars)` : '❌ KHÔNG CÓ');
console.log('  - smd_refresh_token:', refreshToken ? `✅ TỒN TẠI (${refreshToken.length} chars)` : '❌ KHÔNG CÓ');
console.log('  - smd_user_data:', userData ? '✅ TỒN TẠI' : '❌ KHÔNG CÓ');

if (userData) {
  try {
    const user = JSON.parse(userData);
    console.log('\n👤 Thông tin User:');
    console.log('  - Email:', user.email);
    console.log('  - Role:', user.role);
    console.log('  - Full Name:', user.fullName);
    console.log('  - ID:', user.id);
  } catch (e) {
    console.error('❌ Không parse được user data:', e);
  }
}

// 2. Kiểm tra token có hợp lệ không (thử decode JWT)
if (token) {
  try {
    const parts = token.split('.');
    if (parts.length === 3) {
      const payload = JSON.parse(atob(parts[1]));
      console.log('\n🔑 JWT Payload:');
      console.log('  - Subject (userId):', payload.sub);
      console.log('  - Issued At:', new Date(payload.iat * 1000).toLocaleString());
      console.log('  - Expires At:', new Date(payload.exp * 1000).toLocaleString());
      
      const now = Date.now() / 1000;
      if (payload.exp < now) {
        console.error('  - ⚠️ TOKEN ĐÃ HẾT HẠN!');
      } else {
        console.log('  - ✅ Token còn hiệu lực');
      }
    }
  } catch (e) {
    console.warn('⚠️ Không decode được JWT:', e);
  }
}

// 3. Kiểm tra có API nào đang chạy không
console.log('\n🌐 Để kiểm tra Network:');
console.log('  1. Bấm F12 → Tab Network');
console.log('  2. Bấm vào menu item');
console.log('  3. Xem có request nào đỏ (401, 403, 500) không');
console.log('  4. Nếu có, click vào để xem chi tiết');

console.log('\n========================================');
console.log('✅ HOÀN TẤT KIỂM TRA');
console.log('========================================\n');
