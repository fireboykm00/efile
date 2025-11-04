# E-File Connect Frontend - Setup & Running Guide

## Quick Start

### Prerequisites
- Node.js 18+
- Bun (preferred) or npm
- Backend API running on `http://localhost:8080`

### Installation

```bash
cd frontend

# Install dependencies (using bun - preferred)
bun install

# Or using npm
npm install
```

### Environment Setup

Create `.env.local` in the frontend directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Development Server

```bash
# Using bun (recommended)
bun run dev

# Or using npm
npm run dev
```

The application will be available at `http://localhost:5173`

## Building for Production

```bash
# Build the application
bun run build

# Preview the build
bun run preview
```

## Linting

```bash
# Run ESLint
bun run lint

# Fix linting issues
npm run lint -- --fix
```

## Project Structure

```
src/
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── DocumentsPage.tsx
│   ├── CasesPage.tsx
│   └── AdminPage.tsx
├── components/         # Reusable components
│   ├── auth/          # Authentication components
│   ├── layout/        # Layout components (Header, Sidebar)
│   ├── dashboard/     # Dashboard widgets
│   ├── documents/     # Document management components
│   ├── common/        # Common utilities (ErrorBoundary)
│   └── ui/            # shadcn/ui components
├── services/          # API service layers
│   ├── api.ts         # Axios instance
│   ├── authService.ts
│   ├── documentService.ts
│   ├── caseService.ts
│   ├── userService.ts
│   ├── communicationService.ts
│   └── dashboardService.ts
├── hooks/             # Custom React hooks
│   ├── useDashboard.ts
│   ├── useDocuments.ts
│   └── useCases.ts
├── stores/            # State management (Zustand)
│   └── authStore.ts
├── types/             # TypeScript definitions
│   ├── user.ts
│   ├── document.ts
│   ├── case.ts
│   ├── communication.ts
│   ├── api.ts
│   └── auth.ts
├── utils/             # Utility functions
│   └── toast.ts       # Toast notification helpers
└── App.tsx            # Main app component
```

## Key Features Implemented

### ✅ Authentication
- Email/password login
- 2FA OTP verification
- JWT token management
- Persistent login with token refresh

### ✅ Dashboard
- Real-time data fetching with polling
- Summary statistics
- Pending documents widget
- Assigned cases widget
- Recent notifications widget

### ✅ Document Management
- Drag-and-drop file upload
- File validation (size, type)
- Document filtering by status
- Download functionality
- Approval/rejection workflows

### ✅ Case Management
- Create new cases
- View all cases
- Filter by status
- Case assignments

### ✅ User Management (Admin)
- Create users with role assignment
- View user list
- User status management
- Department management (UI ready)

### ✅ Error Handling
- Global error boundary
- Toast notifications for user feedback
- Network error handling
- Graceful error recovery

### ✅ Role-Based Access
- 8 different user roles: ADMIN, CEO, CFO, PROCUREMENT, ACCOUNTANT, AUDITOR, IT, INVESTOR
- Role-based navigation in sidebar
- Protected routes
- Role-specific features

## Supported Browsers

- Chrome/Chromium (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## API Integration Points

The frontend expects the backend API at the base URL configured in `.env.local`. All API calls go through the `api.ts` service layer which handles:

- Request/response interceptors
- JWT token injection
- Error handling
- Base URL configuration

### Key API Endpoints Used

**Authentication:**
- `POST /auth/login`
- `POST /auth/verify-otp`
- `POST /auth/refresh`

**Dashboard:**
- `GET /dashboard/summary`
- `GET /dashboard/pending-documents`
- `GET /dashboard/assigned-cases`
- `GET /dashboard/notifications`

**Documents:**
- `GET /documents`
- `POST /documents/upload`
- `PUT /documents/{id}/approve`
- `PUT /documents/{id}/reject`
- `GET /documents/{id}/download`

**Cases:**
- `GET /cases`
- `POST /cases`
- `PUT /cases/{id}`

**Users:**
- `GET /users`
- `POST /users`
- `GET /users/me`

## Troubleshooting

### Port Already in Use
If port 5173 is already in use:
```bash
# Specify a different port
bun run dev -- --port 3000
```

### CORS Errors
Ensure the backend has CORS configured to allow requests from `http://localhost:5173`

### API Connection Issues
1. Verify backend is running on `http://localhost:8080`
2. Check `.env.local` has correct `VITE_API_BASE_URL`
3. Check browser console for detailed error messages

### Build Errors
```bash
# Clear cache and reinstall
rm -rf node_modules bun.lockb
bun install
bun run build
```

## Development Tips

### Adding New Services
1. Create a new file in `src/services/`
2. Use `apiClient` from `src/services/api.ts`
3. Export service functions
4. Create corresponding hook in `src/hooks/`

### Adding New Pages
1. Create page component in `src/pages/`
2. Add route to `src/App.tsx`
3. Wrap with `<ProtectedRoute>` if authenticated-only
4. Use `DashboardLayout` for consistent styling

### Adding UI Components
Most common UI components are already available via shadcn/ui. To add more:
```bash
npx shadcn-ui@latest add [component-name]
```

## Performance Monitoring

- Dashboard updates every 30 seconds
- Document search debounced to 300ms
- Pagination support for large lists
- Optimized re-renders with proper React hooks

## Security Notes

- JWT tokens stored in-memory (not localStorage) to prevent XSS
- Sensitive data not logged to console in production
- CORS policy enforced
- Input validation on all forms with Zod

## Next Steps

1. **Backend Integration**: Connect to live backend API
2. **Testing**: Add unit and integration tests
3. **Monitoring**: Set up error logging (Sentry)
4. **Optimization**: Implement React Query for better caching
5. **PWA**: Add service worker for offline support
6. **Analytics**: Integrate analytics tracking

## Support & Documentation

- Backend Tasks: `.kiro/specs/e-file-connect/tasks.md`
- Frontend Implementation: `.kiro/specs/e-file-connect/FRONTEND_IMPLEMENTATION.md`
- API Documentation: (Backend team)

## License

Internal Project - E-File Connect System
