/**
 * DEBUG AUTHENTICATION FLOW
 * Paste vào Console SAU KHI đăng nhập
 */

console.log('\n=== 🔍 DEBUG AUTHENTICATION FLOW ===\n');

// 1. Check localStorage
console.log('📦 localStorage:');
const token = localStorage.getItem('smd_access_token');
const userData = localStorage.getItem('smd_user_data');

console.log('  • smd_access_token:', token ? `${token.substring(0, 30)}...` : '❌ NOT FOUND');
console.log('  • smd_user_data:', userData ? '✅ EXISTS' : '❌ NOT FOUND');

if (userData) {
  try {
    const user = JSON.parse(userData);
    console.log('  • Parsed user:');
    console.log('    - email:', user.email);
    console.log('    - fullName:', user.fullName);
    console.log('    - role:', user.role);
    console.log('    - role type:', typeof user.role);
  } catch (e) {
    console.error('❌ Failed to parse user data:', e);
  }
}

// 2. Check current URL
console.log('\n🌐 Current URL:', window.location.href);
console.log('  • pathname:', window.location.pathname);
console.log('  • hash:', window.location.hash);

// 3. Check what layout is being used
const checkLayout = () => {
  const siderElements = document.querySelectorAll('.ant-layout-sider');
  console.log('\n🎨 Layout Detection:');
  console.log('  • Number of siders:', siderElements.length);
  
  siderElements.forEach((sider, index) => {
    const header = sider.querySelector('div[style*="background"]');
    if (header) {
      const text = header.textContent;
      console.log(`  • Sider ${index + 1}: "${text}"`);
      if (text.includes('SMD')) {
        console.log('    → MainLayout (Admin/HOD/AA/Principal)');
      } else if (text.includes('LECTURER') || text === 'L') {
        console.log('    → LecturerLayout');
      }
    }
  });
};

checkLayout();

// 4. Define UserRole enum
const UserRole = {
  ADMIN: 'ADMIN',
  LECTURER: 'LECTURER',
  HOD: 'HOD',
  AA: 'AA',
  PRINCIPAL: 'PRINCIPAL',
  STUDENT: 'STUDENT',
};

// 5. Check route matching
console.log('\n🛣️ Route Analysis:');
const path = window.location.pathname;

if (userData) {
  const user = JSON.parse(userData);
  
  // Admin routes
  if (path.startsWith('/admin')) {
    console.log('  • Current route: ADMIN area (/admin/*)');
    const allowedRoles = [UserRole.ADMIN, UserRole.HOD, UserRole.AA, UserRole.PRINCIPAL];
    const hasAccess = allowedRoles.includes(user.role);
    console.log('  • Allowed roles:', allowedRoles.join(', '));
    console.log('  • User role:', user.role);
    console.log('  • Has access:', hasAccess ? '✅ YES' : '❌ NO');
  }
  
  // Lecturer routes
  if (path.startsWith('/lecturer')) {
    console.log('  • Current route: LECTURER area (/lecturer/*)');
    const allowedRoles = [UserRole.LECTURER];
    const hasAccess = allowedRoles.includes(user.role);
    console.log('  • Allowed roles:', allowedRoles.join(', '));
    console.log('  • User role:', user.role);
    console.log('  • Has access:', hasAccess ? '✅ YES' : '❌ NO');
  }
  
  // Root redirect
  if (path === '/') {
    console.log('  • Current route: ROOT (/)');
    console.log('  • Should redirect to:');
    switch (user.role) {
      case UserRole.LECTURER:
        console.log('    → /lecturer');
        break;
      case UserRole.STUDENT:
        console.log('    → /student');
        break;
      case UserRole.ADMIN:
      case UserRole.HOD:
      case UserRole.AA:
      case UserRole.PRINCIPAL:
        console.log('    → /admin/dashboard');
        break;
      default:
        console.log('    → /login');
    }
  }
}

// 6. Check menu items
console.log('\n📋 Menu Analysis:');
const menuItems = document.querySelectorAll('.ant-menu-item');
console.log('  • Number of menu items:', menuItems.length);
menuItems.forEach((item, index) => {
  const text = item.textContent;
  console.log(`  • Menu ${index + 1}: "${text}"`);
});

console.log('\n=== ✅ DEBUG COMPLETE ===\n');
console.log('👉 Nếu có vấn đề, copy TẤT CẢ output trên và gửi cho tôi!\n');
