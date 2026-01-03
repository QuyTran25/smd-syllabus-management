/**
 * TEST ROLE MATCHING - Paste vào Console
 * 
 * Script này kiểm tra role có match với allowedRoles không
 */

console.log('========================================');
console.log('🔍 TEST ROLE MATCHING');
console.log('========================================\n');

// 1. Lấy user data từ localStorage
const userData = localStorage.getItem('smd_user_data');

if (!userData) {
  console.error('❌ KHÔNG TÌM THẤY USER DATA trong localStorage!');
  console.log('  → Hãy đảm bảo bạn đã đăng nhập');
} else {
  try {
    const user = JSON.parse(userData);
    console.log('👤 User hiện tại:');
    console.log('  - Email:', user.email);
    console.log('  - Role:', user.role);
    console.log('  - Role type:', typeof user.role);
    console.log('  - Full Name:', user.fullName);
    
    // 2. Định nghĩa các role enum
    const UserRole = {
      ADMIN: 'ADMIN',
      LECTURER: 'LECTURER',
      HOD: 'HOD',
      AA: 'AA',
      PRINCIPAL: 'PRINCIPAL',
      STUDENT: 'STUDENT',
    };
    
    // 3. Test với các allowedRoles khác nhau
    const testCases = [
      {
        name: 'Admin Routes',
        allowedRoles: [UserRole.ADMIN, UserRole.HOD, UserRole.AA, UserRole.PRINCIPAL],
      },
      {
        name: 'Admin Only',
        allowedRoles: [UserRole.ADMIN],
      },
      {
        name: 'HOD Only',
        allowedRoles: [UserRole.HOD],
      },
      {
        name: 'Lecturer Routes',
        allowedRoles: [UserRole.LECTURER],
      },
    ];
    
    console.log('\n📋 Testing role matching:\n');
    
    testCases.forEach(testCase => {
      const hasAccess = testCase.allowedRoles.includes(user.role);
      const icon = hasAccess ? '✅' : '❌';
      const status = hasAccess ? 'CÓ QUYỀN' : 'KHÔNG CÓ QUYỀN';
      
      console.log(`${icon} ${testCase.name}:`, status);
      console.log(`   User role: "${user.role}"`);
      console.log(`   Allowed: [${testCase.allowedRoles.map(r => `"${r}"`).join(', ')}]`);
      console.log(`   Match: ${testCase.allowedRoles.map(r => `"${r}" === "${user.role}": ${r === user.role}`).join(', ')}`);
      console.log('');
    });
    
    // 4. Kiểm tra xem có vấn đề gì với role string không
    console.log('🔬 Chi tiết role string:');
    console.log('  - Length:', user.role.length);
    console.log('  - Char codes:', Array.from(user.role).map((c, i) => `${c}(${c.charCodeAt(0)})`).join(' '));
    console.log('  - Trimmed:', user.role.trim());
    console.log('  - Trimmed === original:', user.role.trim() === user.role);
    
  } catch (e) {
    console.error('❌ Lỗi khi parse user data:', e);
  }
}

console.log('\n========================================');
console.log('✅ TEST HOÀN TẤT');
console.log('========================================\n');
