# Implementation Plan

## Backend Implementation

- [ ] 1. Set up database schema and core entities
- [ ] 1.1 Create User entity with UserRole enum and BCrypt password encoding

  - Implement User entity with fields: id, name, email, passwordHash, role, departmentId, isActive, timestamps
  - Create UserRole enum with all 8 roles (ADMIN, CEO, CFO, PROCUREMENT, ACCOUNTANT, AUDITOR, IT, INVESTOR)
  - Configure BCrypt password encoder with cost factor 12
  - _Requirements: 1.1, 1.4, 2.1, 2.2, 3.1, 3.3_

- [ ] 1.2 Create Department entity and establish User-Department relationship

  - Implement Department entity with fields: id, name, headId, createdAt
  - Add @ManyToOne relationship from User to Department
  - _Requirements: 3.2, 11.1, 11.2, 11.3_

- [ ] 1.3 Create Case entity with status enum and user relationships

  - Implement Case entity with fields: id, title, description, status, assignedToId, createdById, timestamps
  - Create CaseStatus enum (OPEN, IN_PROGRESS, CLOSED, ARCHIVED)
  - Add relationships to User for assignment and creation tracking
  - _Requirements: 7.1, 7.2, 7.3, 7.5_

- [ ] 1.4 Create Document entity with approval workflow fields

  - Implement Document entity with fields: id, title, type, filePath, fileSize, status, caseId, uploadedById, approvedById, rejectionReason, receiptNumber, timestamps
  - Create DocumentStatus enum (PENDING, UNDER_REVIEW, APPROVED, REJECTED, ARCHIVED)
  - Create DocumentType enum (FINANCIAL_REPORT, PROCUREMENT_BID, LEGAL_DOCUMENT, AUDIT_REPORT, INVESTMENT_REPORT, GENERAL)
  - Add relationships to Case and User entities
  - _Requirements: 4.3, 5.1, 5.2, 5.3, 6.1_

- [ ] 1.5 Create Communication entity with encryption support

  - Implement Communication entity with fields: id, type, content, isRead, senderId, recipientId, caseId, sentAt, readAt
  - Create CommunicationType enum (MESSAGE, NOTIFICATION, SYSTEM_ALERT)
  - Add relationships to User and Case entities
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 1.6 Create AuditLog and OtpToken entities

  - Implement AuditLog entity with fields: id, action, entityType, entityId, userId, ipAddress, details (JSON), timestamp
  - Implement OtpToken entity with fields: id, userId, token, expiresAt, isUsed, createdAt
  - _Requirements: 1.2, 1.3, 10.1, 10.2, 10.4_

- [ ] 1.7 Create Spring Data JPA repositories for all entities

  - Create UserRepository with custom query methods (findByEmail, findByRole, findByDepartmentId)
  - Create DepartmentRepository, CaseRepository, DocumentRepository, CommunicationRepository
  - Create AuditLogRepository with query methods for filtering
  - Create OtpTokenRepository with methods to find valid tokens
  - _Requirements: All data access requirements_

- [ ] 1.8 Configure database connection and apply indexes

  - Configure MySQL connection in application.properties
  - Set up HikariCP connection pooling
  - Create database indexes as specified in design (users_email, documents_case, etc.)
  - _Requirements: Performance and data access requirements_

- [ ] 2. Implement authentication and security infrastructure
- [ ] 2.1 Create JWT utility class for token generation and validation

  - Implement JwtUtil class with methods: generateToken, validateToken, extractUsername, extractClaims
  - Configure JWT secret key and 24-hour expiration
  - Use HS512 signing algorithm
  - _Requirements: 1.1, 1.5_

- [ ] 2.2 Implement OTP generation and email service

  - Create OtpService to generate 6-digit OTP codes
  - Implement EmailService using Spring Mail to send OTP emails
  - Store OTP in database with 5-minute expiration
  - Track OTP usage to prevent reuse
  - _Requirements: 1.2, 1.3_

- [ ] 2.3 Create JWT authentication filter

  - Implement JwtAuthenticationFilter extending OncePerRequestFilter
  - Extract JWT from Authorization header
  - Validate token and set SecurityContext
  - Handle token expiration gracefully
  - _Requirements: 1.1, 1.5_

- [ ] 2.4 Configure Spring Security with role-based authorization

  - Create SecurityConfig class with @EnableWebSecurity
  - Configure HTTP security with JWT filter
  - Set up role-based access rules for all endpoints
  - Configure CORS for frontend origin (localhost:5173)
  - Disable CSRF for stateless API
  - _Requirements: 2.2, 2.3, 2.4, Authorization matrix from design_

- [ ] 2.5 Implement AuthService with login and 2FA logic

  - Create login method that validates credentials and generates OTP
  - Create verifyOtp method that validates OTP and generates JWT
  - Implement account lockout after 3 failed OTP attempts (15 minutes)
  - Create token refresh logic
  - _Requirements: 1.1, 1.2, 1.3, 1.5_

- [ ] 2.6 Create AuthController with authentication endpoints

  - Implement POST /api/auth/login endpoint
  - Implement POST /api/auth/verify-otp endpoint
  - Implement POST /api/auth/refresh endpoint
  - Implement POST /api/auth/logout endpoint
  - Add request/response DTOs for all endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.5_

- [ ] 3. Implement user and department management
- [ ] 3.1 Create UserService with CRUD operations

  - Implement createUser with password hashing and validation
  - Implement updateUser with audit logging
  - Implement deactivateUser (soft delete)
  - Implement getUserById, getAllUsers, getUsersByRole
  - Implement getCurrentUser from SecurityContext
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 3.2 Create DepartmentService with management operations

  - Implement createDepartment with unique name validation
  - Implement updateDepartment
  - Implement deleteDepartment with user reassignment check
  - Implement getDepartmentUsers
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 3.3 Create UserController with REST endpoints

  - Implement GET /api/users (admin only)
  - Implement GET /api/users/{id}
  - Implement POST /api/users (admin only)
  - Implement PUT /api/users/{id}
  - Implement DELETE /api/users/{id} (admin only)
  - Implement GET /api/users/me and PUT /api/users/me
  - Add DTOs for requests and responses
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 3.4 Create DepartmentController with REST endpoints

  - Implement GET /api/departments
  - Implement GET /api/departments/{id}
  - Implement POST /api/departments (admin only)
  - Implement PUT /api/departments/{id} (admin only)
  - Implement DELETE /api/departments/{id} (admin only)
  - Implement GET /api/departments/{id}/users
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 4. Implement document management with file handling
- [ ] 4.1 Create file storage service with validation

  - Implement FileStorageService to handle file uploads
  - Create directory structure: /uploads/{year}/{month}/{caseId}/
  - Validate file type (PDF, DOCX, XLSX, PNG)
  - Validate file size (max 10MB)
  - Generate unique filenames to prevent conflicts
  - _Requirements: 4.1, 4.2, 4.4_

- [ ] 4.2 Create DocumentService with approval workflow

  - Implement uploadDocument with file validation and storage
  - Implement approveDocument (CEO/CFO only) with status update
  - Implement rejectDocument with required rejection reason
  - Implement getDocumentById with role-based access control
  - Implement searchDocuments with filtering (status, type, date range)
  - Generate unique receipt numbers for uploaded documents
  - _Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.4, 13.1, 13.2, 13.3_

- [ ] 4.3 Implement receipt generation service

  - Create ReceiptService to generate PDF receipts
  - Include document title, upload timestamp, case ID, receipt number
  - Implement email delivery of receipts
  - Provide download endpoint for receipts
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 4.4 Create DocumentController with file upload endpoints

  - Implement GET /api/documents with role-based filtering
  - Implement GET /api/documents/{id}
  - Implement POST /api/documents/upload with MultipartFile
  - Implement GET /api/documents/{id}/download with streaming
  - Implement PUT /api/documents/{id}/approve (CEO/CFO only)
  - Implement PUT /api/documents/{id}/reject (CEO/CFO only)
  - Implement GET /api/documents/search with query parameters
  - Implement GET /api/documents/{id}/receipt
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 6.1, 6.2, 6.3, 6.4, 12.1, 12.5, 13.1, 13.2, 13.3, 13.4_

- [ ] 5. Implement case management
- [ ] 5.1 Create CaseService with business logic

  - Implement createCase with validation (title min 5 chars, description min 20 chars)
  - Implement updateCase with audit logging
  - Implement assignCase with user validation
  - Implement getCaseById with associated documents and communications
  - Implement getCasesByUser with role-based filtering
  - Implement archiveCase (status change to ARCHIVED)
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 5.2 Create CaseController with REST endpoints

  - Implement GET /api/cases with role-based filtering
  - Implement GET /api/cases/{id}
  - Implement POST /api/cases
  - Implement PUT /api/cases/{id}
  - Implement DELETE /api/cases/{id} (archive)
  - Implement GET /api/cases/{id}/documents
  - Implement GET /api/cases/{id}/communications
  - Implement PUT /api/cases/{id}/assign
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 6. Implement secure communication system
- [ ] 6.1 Create encryption service for message content

  - Implement EncryptionService using AES-256
  - Create methods: encrypt(plaintext) and decrypt(ciphertext)
  - Store encryption key securely in application properties
  - _Requirements: 8.4_

- [ ] 6.2 Create CommunicationService with role-based restrictions

  - Implement sendCommunication with content encryption
  - Validate recipient based on sender role (INVESTOR restrictions)
  - Implement markAsRead functionality
  - Implement getUnreadCount for user
  - Implement getCommunicationsForUser with case filtering
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 6.3 Create CommunicationController with messaging endpoints

  - Implement GET /api/communications (user's communications)
  - Implement GET /api/communications/{id}
  - Implement POST /api/communications
  - Implement PUT /api/communications/{id}/read
  - Implement GET /api/communications/unread-count
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 7. Implement audit logging and compliance
- [ ] 7.1 Create AuditService with automatic logging

  - Implement logAction method with parameters: action, entityType, entityId, userId, ipAddress, details
  - Create aspect to automatically log critical operations (document approval, user creation, etc.)
  - Implement getAuditLogs with filtering (date range, user, action type)
  - Ensure audit logs are immutable (no update/delete operations)
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 7.2 Create AuditController for auditor access

  - Implement GET /api/audit-logs (AUDITOR, ADMIN, IT only)
  - Add query parameters for filtering: startDate, endDate, userId, actionType
  - Implement pagination for large result sets
  - _Requirements: 10.1, 10.2, 10.3_

- [ ] 8. Implement dashboard and notification features
- [ ] 8.1 Create DashboardService to aggregate user-specific data

  - Implement getPendingDocuments for approvers
  - Implement getAssignedCases for user
  - Implement getUnreadCommunications for user
  - Implement priority calculation (overdue, due soon, normal)
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 8.2 Create DashboardController with aggregated endpoints

  - Implement GET /api/dashboard/summary
  - Implement GET /api/dashboard/pending-documents
  - Implement GET /api/dashboard/assigned-cases
  - Implement GET /api/dashboard/notifications
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 9. Implement data export functionality
- [ ] 9.1 Create ExportService with CSV and PDF generation

  - Implement exportDocuments with role-based filtering
  - Implement exportCases with role-based filtering
  - Support CSV format for tabular data
  - Support PDF format for reports
  - Implement async processing for large exports (>1000 records)
  - Send email with download link for async exports
  - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_

- [ ] 9.2 Create ExportController with export endpoints

  - Implement GET /api/export/documents with format parameter
  - Implement GET /api/export/cases with format parameter
  - Add role-based restrictions (INVESTOR limited to investment reports)
  - _Requirements: 15.1, 15.5_

- [ ] 10. Implement global exception handling and validation
- [ ] 10.1 Create global exception handler

  - Implement @RestControllerAdvice class
  - Handle ValidationException with 400 status
  - Handle AuthenticationException with 401 status
  - Handle AuthorizationException with 403 status
  - Handle ResourceNotFoundException with 404 status
  - Handle FileUploadException with 400 status
  - Handle generic exceptions with 500 status
  - Return consistent error response format with timestamp, status, error, message, path
  - _Requirements: Error handling from design_

- [ ] 10.2 Add validation annotations to DTOs

  - Add @Valid annotations to controller methods
  - Add @NotNull, @NotBlank, @Size, @Email annotations to DTO fields
  - Create custom validators for business rules (file type, role permissions)
  - _Requirements: Input validation from design_

- [ ] 11. Configure application properties and profiles
- [ ] 11.1 Set up application.properties for development

  - Configure MySQL connection (URL, username, password)
  - Configure JPA/Hibernate settings
  - Configure file upload settings (max size, allowed types)
  - Configure JWT settings (secret, expiration)
  - Configure email settings (SMTP server, credentials)
  - Configure CORS allowed origins
  - _Requirements: All configuration requirements_

- [ ] 11.2 Create application-prod.properties for production
  - Use environment variables for sensitive data
  - Configure production database settings
  - Set appropriate logging levels
  - _Requirements: Production deployment_

## Frontend Implementation

- [ ] 12. Set up React project structure and core dependencies
- [ ] 12.1 Initialize project configuration and install dependencies

  - Verify Vite, React, TypeScript setup
  - Install shadcn/ui CLI and initialize with TailwindCSS
  - Install dependencies: react-router-dom, axios, @tanstack/react-query, zustand, react-hook-form, zod
  - Configure path aliases in tsconfig.json and vite.config.ts
  - _Requirements: Frontend technology stack_

- [ ] 12.2 Create project folder structure

  - Create folders: components/ui, components/auth, components/dashboard, components/documents, components/cases, components/communications, components/layout
  - Create folders: pages, hooks, services, stores, types, utils, lib
  - _Requirements: Frontend architecture_

- [ ] 12.3 Set up shadcn/ui components

  - Add button, input, card, table, dialog, dropdown-menu, toast, badge, tabs components
  - Add form, label, select, textarea components
  - Customize theme colors in tailwind.config.js
  - _Requirements: UI component library_

- [ ] 13. Implement authentication and routing
- [ ] 13.1 Create TypeScript types and interfaces

  - Create types/user.ts with User, UserRole interfaces
  - Create types/auth.ts with LoginRequest, LoginResponse, OtpRequest interfaces
  - Create types/document.ts, types/case.ts, types/communication.ts
  - Create types/api.ts with ApiResponse, ApiError interfaces
  - _Requirements: Type safety_

- [ ] 13.2 Create API client service with Axios

  - Create services/api.ts with configured Axios instance
  - Set base URL to http://localhost:8080/api
  - Add request interceptor to include JWT token
  - Add response interceptor for error handling and token refresh
  - _Requirements: 1.1, 1.5, API communication_

- [ ] 13.3 Create authentication service

  - Create services/authService.ts with login, verifyOtp, logout, refreshToken methods
  - Implement token storage in memory (not localStorage)
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 13.4 Create auth store with Zustand

  - Create stores/authStore.ts
  - Store user, token, isAuthenticated state
  - Implement login, logout, setUser actions
  - _Requirements: State management_

- [ ] 13.5 Create LoginForm component

  - Create components/auth/LoginForm.tsx
  - Use react-hook-form with zod validation
  - Implement email and password inputs using shadcn/ui
  - Handle form submission and error display
  - _Requirements: 1.1_

- [ ] 13.6 Create TwoFactorAuth component

  - Create components/auth/TwoFactorAuth.tsx
  - Implement 6-digit OTP input
  - Add resend OTP button with countdown timer
  - Handle OTP verification
  - _Requirements: 1.2_

- [ ] 13.7 Create LoginPage and set up routing

  - Create pages/LoginPage.tsx with LoginForm and TwoFactorAuth flow
  - Install and configure react-router-dom
  - Create App.tsx with routes
  - Create ProtectedRoute component for authenticated routes
  - _Requirements: 1.1, 1.2, Navigation_

- [ ] 14. Implement dashboard and layout
- [ ] 14.1 Create layout components

  - Create components/layout/Header.tsx with user profile and notifications
  - Create components/layout/Sidebar.tsx with role-based navigation
  - Create components/layout/DashboardLayout.tsx combining header and sidebar
  - _Requirements: 9.1, 14.1, 14.2_

- [ ] 14.2 Create dashboard service and hooks

  - Create services/dashboardService.ts with API calls
  - Create hooks/useDashboard.ts using React Query
  - Fetch pending documents, assigned cases, unread communications
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 14.3 Create dashboard widgets

  - Create components/dashboard/PendingDocumentsWidget.tsx
  - Create components/dashboard/AssignedCasesWidget.tsx
  - Create components/dashboard/NotificationsWidget.tsx
  - Display counts and priority indicators
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 14.4 Create DashboardPage

  - Create pages/DashboardPage.tsx
  - Arrange widgets in responsive grid
  - Implement real-time updates with React Query polling
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 15. Implement document management UI
- [ ] 15.1 Create document service and hooks

  - Create services/documentService.ts with upload, download, approve, reject, search methods
  - Create hooks/useDocuments.ts, hooks/useDocumentUpload.ts using React Query
  - _Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 6.1, 6.2, 6.4_

- [ ] 15.2 Create DocumentUploadForm component

  - Create components/documents/DocumentUploadForm.tsx
  - Implement file input with drag-and-drop using shadcn/ui
  - Add case selection dropdown
  - Add document type and title inputs
  - Show real-time validation feedback
  - Display upload progress
  - _Requirements: 4.1, 4.2, 4.4_

- [ ] 15.3 Create DocumentList component

  - Create components/documents/DocumentList.tsx
  - Display documents in table with shadcn/ui Table
  - Implement filtering by status, type, date range
  - Add search input
  - Implement pagination
  - _Requirements: 6.1, 6.2, 6.4, 13.1, 13.2, 13.3, 13.4_

- [ ] 15.4 Create DocumentViewer component

  - Create components/documents/DocumentViewer.tsx
  - Display document metadata
  - Show PDF preview for PDF files
  - Add approve/reject buttons (role-based)
  - Add download button
  - Display status history
  - _Requirements: 5.1, 5.2, 5.3, 6.1, 6.3_

- [ ] 15.5 Create DocumentsPage

  - Create pages/DocumentsPage.tsx
  - Combine DocumentList and upload functionality
  - Add modal for DocumentViewer
  - _Requirements: Document management UI_

- [ ] 16. Implement case management UI
- [ ] 16.1 Create case service and hooks

  - Create services/caseService.ts with create, update, assign, archive methods
  - Create hooks/useCases.ts, hooks/useCase.ts using React Query
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [ ] 16.2 Create CaseForm component

  - Create components/cases/CaseForm.tsx
  - Add title and description inputs
  - Add status selection dropdown
  - Add user assignment dropdown
  - Use react-hook-form with validation
  - _Requirements: 7.1, 7.2, 7.3_

- [ ] 16.3 Create CaseList component

  - Create components/cases/CaseList.tsx
  - Display cases in card grid
  - Show status badges
  - Add filtering by status and assigned user
  - _Requirements: 7.1, 7.5_

- [ ] 16.4 Create CaseDetails component

  - Create components/cases/CaseDetails.tsx
  - Display case information
  - Show associated documents list
  - Show communications thread
  - Display status timeline
  - _Requirements: 7.4_

- [ ] 16.5 Create CasesPage

  - Create pages/CasesPage.tsx
  - Combine CaseList and create functionality
  - Add modal for CaseDetails
  - _Requirements: Case management UI_

- [ ] 17. Implement communication UI
- [ ] 17.1 Create communication service and hooks

  - Create services/communicationService.ts with send, markAsRead, getUnreadCount methods
  - Create hooks/useCommunications.ts using React Query
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 17.2 Create MessageComposer component

  - Create components/communications/MessageComposer.tsx
  - Add recipient selection (filtered by role)
  - Add case association dropdown
  - Add message textarea
  - Implement send functionality
  - _Requirements: 8.1, 8.5_

- [ ] 17.3 Create MessageThread component

  - Create components/communications/MessageThread.tsx
  - Display messages chronologically
  - Show read/unread indicators
  - Display sender info and timestamps
  - Auto-scroll to latest message
  - _Requirements: 8.2, 8.3_

- [ ] 17.4 Create CommunicationsPage

  - Create pages/CommunicationsPage.tsx
  - Combine MessageThread and MessageComposer
  - Add case filter
  - Implement real-time updates with polling
  - _Requirements: Communication UI_

- [ ] 18. Implement user and department management UI (Admin)
- [ ] 18.1 Create user service and hooks

  - Create services/userService.ts with CRUD methods
  - Create hooks/useUsers.ts using React Query
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 18.2 Create UserForm component

  - Create components/admin/UserForm.tsx
  - Add name, email, role, department inputs
  - Add password field for new users
  - Use react-hook-form with validation
  - _Requirements: 3.1, 3.2_

- [ ] 18.3 Create UserList component

  - Create components/admin/UserList.tsx
  - Display users in table
  - Add filtering by role and department
  - Add activate/deactivate toggle
  - _Requirements: 3.1, 3.4_

- [ ] 18.4 Create DepartmentForm component

  - Create components/admin/DepartmentForm.tsx
  - Add name and head selection inputs
  - _Requirements: 11.1, 11.2_

- [ ] 18.5 Create AdminPage

  - Create pages/AdminPage.tsx
  - Add tabs for Users and Departments
  - Combine list and form components
  - Restrict access to ADMIN and IT roles
  - _Requirements: User and department management UI_

- [ ] 19. Implement search and export features
- [ ] 19.1 Create SearchBar component

  - Create components/common/SearchBar.tsx
  - Implement debounced search input
  - Add filter dropdowns
  - _Requirements: 13.1, 13.2, 13.3_

- [ ] 19.2 Create export service

  - Create services/exportService.ts with export methods
  - Handle CSV and PDF downloads
  - Show loading state for async exports
  - _Requirements: 15.1, 15.2, 15.3, 15.4_

- [ ] 19.3 Add export buttons to DocumentList and CaseList

  - Add export dropdown with CSV/PDF options
  - Implement download handling
  - Show toast notifications for export status
  - _Requirements: 15.1, 15.2_

- [ ] 20. Implement error handling and notifications
- [ ] 20.1 Create error boundary component

  - Create components/common/ErrorBoundary.tsx
  - Display user-friendly error message
  - Add retry button
  - Log errors to console
  - _Requirements: Frontend error handling_

- [ ] 20.2 Set up toast notifications

  - Configure shadcn/ui toast component
  - Create utils/toast.ts with helper functions
  - Add toast notifications for success/error actions
  - _Requirements: User feedback_

- [ ] 20.3 Create loading states

  - Create components/common/LoadingSpinner.tsx
  - Add skeleton loaders for data fetching
  - Use React Query loading states
  - _Requirements: User experience_

- [ ] 21. Implement responsive design and accessibility
- [ ] 21.1 Make all components responsive

  - Use TailwindCSS responsive utilities
  - Test on mobile, tablet, desktop viewports
  - Adjust navigation for mobile (hamburger menu)
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [ ] 21.2 Add accessibility features

  - Ensure proper ARIA labels
  - Add keyboard navigation support
  - Ensure color contrast meets WCAG standards
  - Add focus indicators
  - _Requirements: Accessibility compliance_

- [ ] 22. Final integration and testing
- [ ] 22.1 Connect frontend to backend API

  - Update API base URL in production
  - Test all API endpoints
  - Handle CORS issues if any
  - _Requirements: Full stack integration_

- [ ] 22.2 Test complete user workflows

  - Test login with 2FA flow
  - Test document upload and approval workflow
  - Test case creation and assignment
  - Test communication sending and receiving
  - Test role-based access control
  - _Requirements: All functional requirements_

- [ ] 22.3 Perform cross-browser testing

  - Test on Chrome, Firefox, Safari, Edge
  - Fix any browser-specific issues
  - _Requirements: Browser compatibility_

- [ ] 22.4 Create initial admin user and seed data
  - Create SQL script to insert admin user
  - Create sample departments
  - Create sample users with different roles
  - _Requirements: System initialization_
