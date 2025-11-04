# Design Document

## Overview

E-FileConnect is a full-stack web application built with a React frontend (Vite + shadcn/ui + TailwindCSS) and Spring Boot backend. The system follows a clean architecture pattern with clear separation between presentation, business logic, and data access layers. The application uses JWT-based authentication, role-based authorization, and implements comprehensive audit logging for compliance.

### Technology Stack

**Frontend:**

- React 18 with TypeScript
- Vite (build tool)
- shadcn/ui (component library)
- TailwindCSS (styling)
- React Router (navigation)
- Axios (HTTP client)
- React Query (server state management)
- Zustand (client state management)
- Bun (runtime and package manager)

**Backend:**

- Spring Boot 3.x
- Spring Security (authentication/authorization)
- Spring Data JPA (data access)
- MySQL 8.x (database)
- JWT (token-based auth)
- Hibernate (ORM)
- Maven (build tool)

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  (React + Vite + shadcn/ui + TailwindCSS)                   │
│  - Authentication UI                                         │
│  - Dashboard                                                 │
│  - Document Management UI                                    │
│  - Case Management UI                                        │
│  - Communication UI                                          │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS/REST API
                     │ (JSON + JWT)
┌────────────────────▼────────────────────────────────────────┐
│                    API Gateway Layer                         │
│              (Spring Boot Controllers)                       │
│  - AuthController                                            │
│  - UserController                                            │
│  - DocumentController                                        │
│  - CaseController                                            │
│  - CommunicationController                                   │
│  - DepartmentController                                      │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                   Security Layer                             │
│         (Spring Security + JWT Filter)                       │
│  - JWT Authentication Filter                                 │
│  - Role-Based Authorization                                  │
│  - CORS Configuration                                        │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                   Service Layer                              │
│              (Business Logic)                                │
│  - AuthService (2FA, JWT generation)                         │
│  - UserService                                               │
│  - DocumentService (validation, approval)                    │
│  - CaseService                                               │
│  - CommunicationService (encryption)                         │
│  - AuditService                                              │
│  - EmailService                                              │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                 Repository Layer                             │
│            (Spring Data JPA)                                 │
│  - UserRepository                                            │
│  - DocumentRepository                                        │
│  - CaseRepository                                            │
│  - CommunicationRepository                                   │
│  - DepartmentRepository                                      │
│  - AuditLogRepository                                        │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                   Database Layer                             │
│                    (MySQL 8.x)                               │
│  Tables: users, cases, documents, communications,            │
│          departments, audit_logs, otp_tokens                 │
└─────────────────────────────────────────────────────────────┘
```

### Frontend Architecture

```
src/
├── components/
│   ├── ui/              # shadcn/ui components
│   ├── auth/            # Login, Register, 2FA
│   ├── dashboard/       # Dashboard widgets
│   ├── documents/       # Document upload, list, viewer
│   ├── cases/           # Case management
│   ├── communications/  # Messaging interface
│   └── layout/          # Header, Sidebar, Footer
├── pages/
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── DocumentsPage.tsx
│   ├── CasesPage.tsx
│   ├── CommunicationsPage.tsx
│   └── AdminPage.tsx
├── hooks/               # Custom React hooks
├── services/            # API client services
├── stores/              # Zustand stores
├── types/               # TypeScript interfaces
├── utils/               # Helper functions
└── lib/                 # Configuration
```

## Components and Interfaces

### Backend Components

#### 1. Authentication & Authorization

**AuthController**

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    POST /login          // Login with credentials
    POST /verify-otp     // Verify 2FA code
    POST /register       // Register new user (admin only)
    POST /refresh        // Refresh JWT token
    POST /logout         // Invalidate token
}
```

**JwtAuthenticationFilter**

- Intercepts all requests
- Validates JWT token
- Sets SecurityContext with user details
- Handles token expiration

**UserRole Enum**

```java
public enum UserRole {
    ADMIN,
    CEO,
    CFO,
    PROCUREMENT,
    ACCOUNTANT,
    AUDITOR,
    IT,
    INVESTOR
}
```

#### 2. User Management

**UserController**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    GET /                // List all users (admin only)
    GET /{id}            // Get user by ID
    POST /               // Create user (admin only)
    PUT /{id}            // Update user
    DELETE /{id}         // Deactivate user (admin only)
    GET /me              // Get current user profile
    PUT /me              // Update current user profile
}
```

**UserService**

- Password hashing with BCrypt
- Role assignment validation
- Department association
- Account activation/deactivation
- Profile updates with audit logging

#### 3. Document Management

**DocumentController**

```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    GET /                // List documents (filtered by role)
    GET /{id}            // Get document details
    POST /upload         // Upload document
    GET /{id}/download   // Download document file
    PUT /{id}/approve    // Approve document (CEO/CFO)
    PUT /{id}/reject     // Reject document (CEO/CFO)
    GET /search          // Search documents
    GET /{id}/receipt    // Generate digital receipt
}
```

**DocumentService**

- File validation (type, size)
- File storage management
- Approval workflow logic
- Status tracking
- Receipt generation
- Role-based access control

**File Storage Strategy**

- Store files in organized directory structure: `/uploads/{year}/{month}/{caseId}/{filename}`
- Store file path in database
- Implement file cleanup for rejected documents after 30 days

#### 4. Case Management

**CaseController**

```java
@RestController
@RequestMapping("/api/cases")
public class CaseController {
    GET /                // List cases (filtered by role)
    GET /{id}            // Get case details
    POST /               // Create case
    PUT /{id}            // Update case
    DELETE /{id}         // Archive case
    GET /{id}/documents  // Get case documents
    GET /{id}/communications // Get case communications
    PUT /{id}/assign     // Assign case to user
}
```

**CaseService**

- Case creation and validation
- Assignment logic
- Status management
- Document and communication aggregation

#### 5. Communication

**CommunicationController**

```java
@RestController
@RequestMapping("/api/communications")
public class CommunicationController {
    GET /                // List communications for user
    GET /{id}            // Get communication details
    POST /               // Send communication
    PUT /{id}/read       // Mark as read
    GET /unread-count    // Get unread count
}
```

**CommunicationService**

- Message encryption (AES-256)
- Role-based recipient validation
- Notification triggering
- Unread status tracking

#### 6. Department Management

**DepartmentController**

```java
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    GET /                // List all departments
    GET /{id}            // Get department details
    POST /               // Create department (admin only)
    PUT /{id}            // Update department (admin only)
    DELETE /{id}         // Delete department (admin only)
    GET /{id}/users      // Get department users
}
```

#### 7. Audit Logging

**AuditService**

- Automatic logging of all critical operations
- Immutable log entries
- Indexed by timestamp, user, and action type
- Queryable by auditors

### Frontend Components

#### 1. Authentication Components

**LoginForm**

- Email and password inputs
- Form validation
- Error handling
- Redirect after successful login

**TwoFactorAuth**

- OTP input (6 digits)
- Resend OTP functionality
- Timer display
- Auto-submit on complete

#### 2. Dashboard Components

**DashboardLayout**

- Role-specific sidebar navigation
- Header with user profile and notifications
- Main content area

**PendingDocumentsWidget**

- List of documents requiring approval
- Quick approve/reject actions
- Priority indicators

**AssignedCasesWidget**

- List of assigned cases
- Status indicators
- Quick navigation to case details

**NotificationsWidget**

- Unread communications count
- Recent activity feed
- Real-time updates

#### 3. Document Components

**DocumentUploadForm**

- File input with drag-and-drop
- Case selection dropdown
- Document type selection
- Title and description inputs
- Real-time validation feedback

**DocumentList**

- Filterable table (status, type, date)
- Search functionality
- Pagination
- Bulk actions (for admins)

**DocumentViewer**

- PDF/image preview
- Document metadata display
- Approval/rejection buttons (role-based)
- Download button
- Audit trail display

#### 4. Case Components

**CaseForm**

- Title and description inputs
- Status selection
- User assignment dropdown
- Department selection

**CaseDetails**

- Case information display
- Associated documents list
- Communications thread
- Status timeline

#### 5. Communication Components

**MessageComposer**

- Recipient selection (filtered by role)
- Case association
- Rich text editor
- Send button

**MessageThread**

- Chronological message display
- Read/unread indicators
- Sender information
- Timestamp display

## Data Models

### Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                          User                                │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│    │ name: String                                            │
│    │ email: String (unique)                                  │
│    │ passwordHash: String                                    │
│    │ role: UserRole (enum)                                   │
│ FK │ departmentId: Long                                      │
│    │ isActive: Boolean                                       │
│    │ createdAt: Timestamp                                    │
│    │ updatedAt: Timestamp                                    │
└──────────────┬──────────────────────────────────────────────┘
               │
               │ 1:N
               │
┌──────────────▼──────────────────────────────────────────────┐
│                       Department                             │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│    │ name: String (unique)                                   │
│ FK │ headId: Long (User)                                     │
│    │ createdAt: Timestamp                                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                          Case                                │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│    │ title: String                                           │
│    │ description: Text                                       │
│    │ status: CaseStatus (enum)                               │
│ FK │ assignedToId: Long (User)                               │
│ FK │ createdById: Long (User)                                │
│    │ createdAt: Timestamp                                    │
│    │ updatedAt: Timestamp                                    │
└──────────────┬──────────────────────────────────────────────┘
               │
               │ 1:N
               │
┌──────────────▼──────────────────────────────────────────────┐
│                       Document                               │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│    │ title: String                                           │
│    │ type: DocumentType (enum)                               │
│    │ filePath: String                                        │
│    │ fileSize: Long                                          │
│    │ status: DocumentStatus (enum)                           │
│ FK │ caseId: Long                                            │
│ FK │ uploadedById: Long (User)                               │
│ FK │ approvedById: Long (User, nullable)                     │
│    │ rejectionReason: Text (nullable)                        │
│    │ receiptNumber: String (unique)                          │
│    │ uploadedAt: Timestamp                                   │
│    │ processedAt: Timestamp (nullable)                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Communication                             │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│    │ type: CommunicationType (enum)                          │
│    │ content: Text (encrypted)                               │
│    │ isRead: Boolean                                         │
│ FK │ senderId: Long (User)                                   │
│ FK │ recipientId: Long (User)                                │
│ FK │ caseId: Long                                            │
│    │ sentAt: Timestamp                                       │
│    │ readAt: Timestamp (nullable)                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       AuditLog                               │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│    │ action: String                                          │
│    │ entityType: String                                      │
│    │ entityId: Long                                          │
│ FK │ userId: Long (User, nullable)                           │
│    │ ipAddress: String                                       │
│    │ details: JSON                                           │
│    │ timestamp: Timestamp                                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       OtpToken                               │
├─────────────────────────────────────────────────────────────┤
│ PK │ id: Long                                                │
│ FK │ userId: Long (User)                                     │
│    │ token: String                                           │
│    │ expiresAt: Timestamp                                    │
│    │ isUsed: Boolean                                         │
│    │ createdAt: Timestamp                                    │
└─────────────────────────────────────────────────────────────┘
```

### Enums

**UserRole**

```java
ADMIN, CEO, CFO, PROCUREMENT, ACCOUNTANT, AUDITOR, IT, INVESTOR
```

**CaseStatus**

```java
OPEN, IN_PROGRESS, CLOSED, ARCHIVED
```

**DocumentStatus**

```java
PENDING, UNDER_REVIEW, APPROVED, REJECTED, ARCHIVED
```

**DocumentType**

```java
FINANCIAL_REPORT, PROCUREMENT_BID, LEGAL_DOCUMENT,
AUDIT_REPORT, INVESTMENT_REPORT, GENERAL
```

**CommunicationType**

```java
MESSAGE, NOTIFICATION, SYSTEM_ALERT
```

### Database Indexes

```sql
-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_department ON users(department_id);

-- Documents
CREATE INDEX idx_documents_case ON documents(case_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by_id);
CREATE INDEX idx_documents_receipt ON documents(receipt_number);

-- Cases
CREATE INDEX idx_cases_assigned_to ON cases(assigned_to_id);
CREATE INDEX idx_cases_status ON cases(status);
CREATE INDEX idx_cases_created_at ON cases(created_at);

-- Communications
CREATE INDEX idx_communications_recipient ON communications(recipient_id);
CREATE INDEX idx_communications_case ON communications(case_id);
CREATE INDEX idx_communications_is_read ON communications(is_read);

-- Audit Logs
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
```

## Error Handling

### Backend Error Handling

**Global Exception Handler**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    - handleValidationException()
    - handleAuthenticationException()
    - handleAuthorizationException()
    - handleResourceNotFoundException()
    - handleFileUploadException()
    - handleGenericException()
}
```

**Error Response Format**

```json
{
  "timestamp": "2025-11-04T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "File size exceeds maximum limit of 10MB",
  "path": "/api/documents/upload"
}
```

### Frontend Error Handling

**Error Boundary Component**

- Catches React component errors
- Displays user-friendly error message
- Logs error details for debugging

**API Error Handling**

- Axios interceptor for global error handling
- Toast notifications for user feedback
- Automatic retry for network errors
- Token refresh on 401 errors

## Security Considerations

### Authentication Flow

1. User submits credentials
2. Backend validates credentials
3. If valid, generate OTP and send via email
4. User submits OTP
5. Backend validates OTP
6. If valid, generate JWT token (24-hour expiry)
7. Return JWT to client
8. Client stores JWT in memory (not localStorage)
9. Client includes JWT in Authorization header for all requests

### Authorization Strategy

**Role-Based Access Control Matrix**

| Endpoint           | ADMIN | CEO | CFO | PROCUREMENT | ACCOUNTANT | AUDITOR | IT  | INVESTOR |
| ------------------ | ----- | --- | --- | ----------- | ---------- | ------- | --- | -------- |
| User Management    | ✓     | -   | -   | -           | -          | -       | ✓   | -        |
| Create Case        | ✓     | ✓   | ✓   | ✓           | ✓          | -       | -   | -        |
| Upload Document    | ✓     | ✓   | ✓   | ✓           | ✓          | -       | -   | -        |
| Approve Document   | ✓     | ✓   | ✓   | -           | -          | -       | -   | -        |
| View All Documents | ✓     | ✓   | ✓   | -           | -          | ✓       | -   | -        |
| View Own Documents | ✓     | ✓   | ✓   | ✓           | ✓          | ✓       | ✓   | ✓        |
| Send Communication | ✓     | ✓   | ✓   | ✓           | ✓          | -       | ✓   | Limited  |
| View Audit Logs    | ✓     | -   | -   | -           | -          | ✓       | ✓   | -        |
| Export Data        | ✓     | ✓   | ✓   | -           | -          | ✓       | -   | Limited  |

### Data Protection

**Encryption**

- Passwords: BCrypt with cost factor 12
- Communications: AES-256 encryption at rest
- JWT: Signed with HS512 algorithm
- HTTPS: TLS 1.3 for all communications

**Input Validation**

- Server-side validation for all inputs
- SQL injection prevention via parameterized queries
- XSS prevention via input sanitization
- File upload validation (type, size, content)

**CORS Configuration**

```java
@Configuration
public class CorsConfig {
    allowedOrigins: ["http://localhost:5173"]
    allowedMethods: ["GET", "POST", "PUT", "DELETE"]
    allowedHeaders: ["Authorization", "Content-Type"]
    allowCredentials: true
}
```

## Testing Strategy

### Backend Testing

**Unit Tests**

- Service layer logic
- Validation methods
- Utility functions
- Target: 80% code coverage

**Integration Tests**

- Repository layer with test database
- Controller endpoints with MockMvc
- Security configuration
- File upload/download

**Test Tools**

- JUnit 5
- Mockito
- Spring Boot Test
- H2 in-memory database for tests

### Frontend Testing

**Component Tests**

- Individual component rendering
- User interactions
- Form validation
- Target: 70% coverage

**Integration Tests**

- Page-level workflows
- API integration with MSW (Mock Service Worker)
- Authentication flows

**E2E Tests (Optional)**

- Critical user journeys
- Login → Upload Document → Approve
- Case creation and management

**Test Tools**

- Vitest
- React Testing Library
- MSW (Mock Service Worker)

### Manual Testing Checklist

- [ ] User registration and login with 2FA
- [ ] Role-based access control for all endpoints
- [ ] Document upload with various file types and sizes
- [ ] Document approval/rejection workflow
- [ ] Case creation and assignment
- [ ] Communication sending and receiving
- [ ] Dashboard updates in real-time
- [ ] Search and filtering functionality
- [ ] Responsive design on mobile devices
- [ ] Error handling and user feedback

## Performance Considerations

### Backend Optimization

**Database**

- Connection pooling (HikariCP)
- Query optimization with proper indexes
- Pagination for large result sets
- Lazy loading for entity relationships

**Caching**

- Spring Cache for frequently accessed data
- User profile caching
- Department list caching
- Cache invalidation on updates

**File Handling**

- Streaming for large file downloads
- Async processing for file uploads
- Scheduled cleanup of orphaned files

### Frontend Optimization

**Code Splitting**

- Route-based code splitting
- Lazy loading of heavy components
- Dynamic imports for modals

**State Management**

- React Query for server state caching
- Optimistic updates for better UX
- Stale-while-revalidate strategy

**Asset Optimization**

- Image compression
- Tree-shaking unused code
- Minification and bundling with Vite

## Deployment Architecture

### Development Environment

- Frontend: Vite dev server (port 5173)
- Backend: Spring Boot embedded Tomcat (port 8080)
- Database: MySQL local instance (port 3306)

### Production Environment

- Frontend: Static files served via Nginx
- Backend: Spring Boot JAR on application server
- Database: MySQL with replication
- File Storage: Dedicated file server or S3-compatible storage
- Load Balancer: Nginx for backend instances
- SSL/TLS: Let's Encrypt certificates

## API Documentation

### OpenAPI/Swagger Integration

- Springdoc OpenAPI for automatic API documentation
- Available at `/swagger-ui.html`
- Interactive API testing interface

### API Versioning

- URL-based versioning: `/api/v1/...`
- Backward compatibility for at least 2 versions

## Monitoring and Logging

### Application Logging

- SLF4J with Logback
- Log levels: ERROR, WARN, INFO, DEBUG
- Structured logging with JSON format
- Log rotation and retention policies

### Metrics

- Spring Boot Actuator endpoints
- Health checks
- Performance metrics
- Custom business metrics

## Future Enhancements

1. **Real-time Notifications**: WebSocket integration for instant updates
2. **Advanced Search**: Elasticsearch for full-text search
3. **Document Versioning**: Track document revisions
4. **Digital Signatures**: PKI-based document signing
5. **Mobile App**: React Native mobile application
6. **AI Integration**: Document classification and extraction
7. **Analytics Dashboard**: Business intelligence reports
8. **Multi-language Support**: i18n for international users
