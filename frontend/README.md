# Management Portal

Portal quản lý dành cho 4 roles: Admin, Principal, HoD (Trưởng bộ môn), và AA (Phòng Đào tạo).

## Features by Role

### Admin
- ✅ Quản lý người dùng (CRUD, lock/unlock, import CSV)
- ✅ Cấu hình hệ thống (PLO templates, semesters, workflow rules)
- ✅ Audit logs
- ✅ Quản lý feedback sinh viên
- ✅ Publish/Unpublish syllabi

### Principal (Hiệu trưởng)
- ✅ Batch approval (phê duyệt hàng loạt)
- ✅ Reports & Analytics
- ✅ Dashboard statistics

### HoD (Trưởng Bộ môn)
- ✅ AI Version Comparison
- ✅ Collaborative Review Management
- ✅ Teaching Assignment

### AA (Phòng Đào tạo)
- ✅ PLO Management
- ✅ Course Management (CRUD courses, prerequisites)
- ✅ AI PLO Compliance Check

### Lecturer (in Management Portal)
- ✅ Create/Edit Syllabus
- ✅ Manage Syllabi
- ✅ Collaborative Review

### Student (in Management Portal)
- ✅ Browse Published Syllabi
- ✅ View Syllabus Detail with AI Summary
- ✅ Follow/Report features

## Tech Stack

- **React 18.3.1** with TypeScript
- **Vite** (build tool)
- **Ant Design 5.22.5** (UI framework)
- **TanStack Query** (server state)
- **React Context API** (global state - NO Redux)
- **React Router v6** (routing với role-based guards)
- **Axios** (HTTP client với interceptors)

## Development

### Install Dependencies
```bash
cd frontend
npm install
```

### Run Management Portal
```bash
npm run dev:management
# Hoặc
cd apps/management-portal
npm run dev
```

Server: http://localhost:3000

### Build
```bash
cd apps/management-portal
npm run build
```

## Demo Accounts

### Admin
- Email: `admin@smd.edu.vn`
- Password: `123456`

### Principal
- Email: `principal@smd.edu.vn`
- Password: `123456`

### HoD
- Email: `hod@smd.edu.vn`
- Password: `123456`

### AA
- Email: `aa@smd.edu.vn`
- Password: `123456`

### Lecturer
- Email: `lecturer@smd.edu.vn`
- Password: `123456`

### Student
- Email: `student@smd.edu.vn`
- Password: `123456`

## Project Structure

```
src/
├── config/              # API configuration
│   └── api-config.ts   # Axios client với interceptors
├── features/            # Feature modules
│   ├── admin/          # Admin pages
│   ├── principal/      # Principal pages
│   ├── hod/            # HoD pages
│   ├── aa/             # AA pages
│   ├── lecturer/       # Lecturer pages
│   ├── student/        # Student pages
│   ├── auth/           # Authentication
│   ├── dashboard/      # Dashboard
│   └── syllabus/       # Syllabus management
├── services/            # API services (mock data)
│   ├── auth.service.ts
│   ├── syllabus.service.ts
│   ├── user.service.ts
│   └── ...
├── mock/                # Mock data
│   ├── syllabi.mock.ts
│   └── users.mock.ts
├── shared/              # Shared components, layouts
│   ├── components/
│   ├── layouts/
│   └── types/          # (deprecated - use @smd/shared-ui/types)
├── App.tsx              # Router configuration
└── main.tsx             # Entry point
```

## Key Features

### Role-Based Access Control
```typescript
<ProtectedRoute allowedRoles={[UserRole.ADMIN]}>
  <UserManagementPage />
</ProtectedRoute>
```

### API Ready Structure
```typescript
// services/syllabus.service.ts
// Currently using mock data
const syllabi = mockSyllabi.filter(...);

// Ready for real API:
// const { data } = await apiClient.get('/api/v1/syllabuses');
```

### Centralized Types
Tất cả types được import từ shared-ui package:
```typescript
import { User, Syllabus, UserRole } from '@smd/shared-ui/types';
```

## Environment Variables

Create `.env` file:
```env
VITE_API_GATEWAY_URL=http://localhost:8080
VITE_API_TIMEOUT=10000
VITE_APP_ENV=development
```

## Next Steps

1. **Backend Integration**: Replace mock services với real API calls
2. **Testing**: Add unit tests và E2E tests
3. **Optimization**: Code splitting, lazy loading
4. **Real-time**: WebSocket cho notifications
