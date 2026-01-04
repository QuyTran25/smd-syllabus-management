# AI Components - Frontend Integration Guide

## 📦 Components Created

### 1. **aiService.ts** - API Client
Location: `frontend/src/services/aiService.ts`

Methods:
- `checkCloPloMapping(syllabusId, curriculumId)` → returns `task_id`
- `compareVersions(oldVersionId, newVersionId, subjectId)` → returns `task_id`
- `summarizeSyllabus(syllabusId)` → returns `task_id`
- `getTaskStatus(taskId)` → returns `TaskStatusResponse`

### 2. **useTaskPolling.ts** - React Hook
Location: `frontend/src/hooks/useTaskPolling.ts`

Features:
- Exponential backoff polling (1s → 2s → 5s)
- Auto timeout after 30 seconds
- Callbacks: `onSuccess`, `onError`
- Returns: `{ status, progress, result, error, isPolling }`

### 3. **CloPloCheckButton.tsx** - UI Component
Location: `frontend/src/components/ai/CloPloCheckButton.tsx`

Props:
```typescript
interface CloPloCheckButtonProps {
  syllabusId: string;
  curriculumId: string;
  onComplete?: (result: CloPloResult) => void;
}
```

Features:
- Trigger button with loading state
- Modal with progress bar during processing
- Display results with issues & suggestions
- Severity tags (HIGH/MEDIUM/LOW)
- Status tags (COMPLIANT/NEEDS_IMPROVEMENT/NON_COMPLIANT)

---

## 🚀 Quick Start

### Step 1: Import Component
```tsx
import { CloPloCheckButton } from '@/components/ai';
```

### Step 2: Use in Your Page
```tsx
<CloPloCheckButton
  syllabusId="123e4567-e89b-12d3-a456-426614174000"
  curriculumId="123e4567-e89b-12d3-a456-426614174001"
  onComplete={(result) => {
    console.log('Analysis completed:', result);
  }}
/>
```

---

## 📋 Data Types

### TaskStatusResponse
```typescript
{
  taskId: string;
  action?: string;
  status: 'QUEUED' | 'PROCESSING' | 'SUCCESS' | 'FAILED' | 'NOT_FOUND';
  progress: number;
  message?: string;
  timestamp?: number;
  result?: any;
  error?: string;
}
```

### CloPloResult
```typescript
{
  status: 'COMPLIANT' | 'NEEDS_IMPROVEMENT' | 'NON_COMPLIANT';
  score: number;
  total_mappings: number;
  compliant_mappings: number;
  issues: Array<{
    type: string;
    severity: 'HIGH' | 'MEDIUM' | 'LOW';
    clo_code: string;
    plo_codes: string[];
    message: string;
  }>;
  suggestions: Array<{
    clo_code: string;
    current_plos: string[];
    suggested_plos: string[];
    reason: string;
  }>;
}
```

---

## 🔧 Integration Points

### AA Role - Syllabus Review
File: `frontend/src/features/aa/SyllabusReviewPage.tsx`

Add button to Actions column in table:
```tsx
<CloPloCheckButton
  syllabusId={record.id}
  curriculumId={record.curriculumId}
/>
```

### Student Role - Syllabus Detail
File: `frontend/src/features/student/pages/StudentSyllabusDetailPage.tsx`

Add summarize button (TODO: Create SummarizeButton component):
```tsx
<Button onClick={async () => {
  const taskId = await aiService.summarizeSyllabus(syllabusId);
  // Poll for result...
}}>
  Summarize Syllabus
</Button>
```

---

## ⚙️ Backend Requirements

### Endpoints (Already Implemented)
- `POST /api/ai/syllabus/{id}/check-clo-plo?curriculumId={uuid}`
- `POST /api/ai/syllabus/compare?oldVersionId={uuid}&newVersionId={uuid}&subjectId={uuid}`
- `POST /api/ai/syllabus/{id}/summarize`
- `GET /api/ai/tasks/{taskId}/status`

### Backend Status
✅ Java DTOs created
✅ AITaskService with in-memory cache
✅ AIAnalysisController endpoints
✅ Python workers processing messages
⏳ PostgreSQL needed to run Core Service

---

## 🎯 TODO - Additional Components

### 1. CompareVersionsButton
Similar to CloPloCheckButton but for version comparison.

### 2. SummarizeButton
For student syllabus detail page.

### 3. TaskProgressList
Show all running AI tasks in a sidebar.

### 4. AIResultsHistory
Table showing past AI analysis results.

---

## 🐛 Testing

### Mock Data
Backend returns mock AI responses for testing. Real Hugging Face integration later.

### Test Steps
1. Start backend services (RabbitMQ, PostgreSQL, Core Service)
2. Click "Check CLO-PLO Compliance" button
3. Modal should show progress bar
4. After 2-3 seconds, results should display

### Without Backend
Component gracefully handles errors and shows error messages.

---

## 📝 Notes

- **Polling Strategy**: 1s (0-5s), 2s (5-15s), 5s (15-30s), timeout at 30s
- **Message Queue**: RabbitMQ with priority support (HIGH=5, MEDIUM=3, LOW=1)
- **Cache**: In-memory ConcurrentHashMap (TODO: Replace with Redis)
- **Security**: JWT token from localStorage automatically included in requests

---

## 🔗 Related Files

- Backend Plan: `docs/RABBITMQ-AI-INTEGRATION-PLAN.md`
- API DTOs: `backend/shared/java-common/dto/ai/`
- Python Workers: `backend/ai-service/app/workers/`
- Usage Examples: `frontend/src/components/ai/USAGE_EXAMPLES.tsx`
