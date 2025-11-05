# E-FileConnect Implementation Tasks

## Sprint 1: Core Workflow Fixes (Priority: HIGH)

### Backend Tasks

#### 1. Document Status Refactoring
- [ ] **Update DocumentStatus enum**
  - File: `backend/src/main/java/com/efile/core/document/DocumentStatus.java`
  - Remove: `ARCHIVED`
  - Add: `DRAFT`, `SUBMITTED`, `WITHDRAWN`
  - Keep: `PENDING` → rename to `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`

- [ ] **Implement Status Transition Logic**
  - File: `backend/src/main/java/com/efile/core/document/DocumentService.java`
  - Add `submitDocument(Long documentId)` method
  - Add `withdrawDocument(Long documentId)` method
  - Add `startReview(Long documentId)` method
  - Update `approveDocument()` to validate current status
  - Update `rejectDocument()` to validate current status
  - Add status transition validation matrix

- [ ] **Update Document Repository Queries**
  - File: `backend/src/main/java/com/efile/core/document/DocumentRepository.java`
  - Add methods for new status-based queries
  - Update existing methods to use new statuses

#### 2. Case Management Enhancement
- [ ] **Update CaseStatus enum**
  - File: `backend/src/main/java/com/efile/core/casemanagement/CaseStatus.java`
  - Remove: `ARCHIVED`
  - Add: `ACTIVE`, `UNDER_REVIEW`, `COMPLETED`, `ON_HOLD`
  - Keep: `OPEN`, `CLOSED`

- [ ] **Enhance CaseService**
  - File: `backend/src/main/java/com/efile/core/casemanagement/CaseService.java`
  - Add `getCaseWithDocuments(Long caseId)` method
  - Add `updateCaseStatus(Long caseId, CaseStatus status)` method
  - Add case progression logic based on document statuses
  - Add case permission validation

- [ ] **Update Case DTOs**
  - File: `backend/src/main/java/com/efile/core/casemanagement/dto/CaseResponse.java`
  - Add list of documents to response
  - Add document status summary
  - Add case progression indicators

#### 3. Department Integration
- [ ] **Enhance User-Department Relationship**
  - File: `backend/src/main/java/com/efile/core/user/UserService.java`
  - Add department-based document routing
  - Add department head validation
  - Add departmental access control

- [ ] **Update DocumentService Department Logic**
  - File: `backend/src/main/java/com/efile/core/document/DocumentService.java`
  - Auto-assign reviews to department heads
  - Add department-specific document type validation
  - Add departmental approval workflows

#### 4. Database Migration
- [ ] **Create Migration Script**
  - File: `backend/src/main/resources/db/migration/V2__update_document_statuses.sql`
  - Update existing document statuses
  - Update existing case statuses
  - Handle data consistency

### Frontend Tasks

#### 1. Update Document Types and Statuses
- [ ] **Update Document Type Definition**
  - File: `frontend/src/types/document.ts`
  - Update DocumentStatus enum to match backend
  - Add new status transition types

- [ ] **Update Document Service**
  - File: `frontend/src/services/documentService.ts`
  - Add `submitDocument()` method
  - Add `withdrawDocument()` method
  - Add `startReview()` method
  - Update status handling in existing methods

#### 2. Enhance Document Management UI
- [ ] **Update Documents Page**
  - File: `frontend/src/pages/DocumentsPage.tsx`
  - Add status-based action buttons
  - Implement status transition flows
  - Remove ARCHIVED status filter
  - Add new status filters

- [ ] **Update Document Components**
  - Enhance document cards with appropriate actions
  - Add status transition modals
  - Improve status visualization

#### 3. Case Management UI Enhancement
- [ ] **Update Case Detail View**
  - File: `frontend/src/pages/CaseDetailPage.tsx` (create if needed)
  - Show all documents in case
  - Add case status management
  - Add document status summary

- [ ] **Update Case Service**
  - File: `frontend/src/services/caseService.ts`
  - Add method to get case with documents
  - Add case status update methods

#### 4. Department Integration UI
- [ ] **Update User Management**
  - File: `frontend/src/pages/UsersPage.tsx`
  - Show department assignments
  - Add department-based filtering

- [ ] **Update Dashboard**
  - File: `frontend/src/pages/DashboardPage.tsx`
  - Add department-specific metrics
  - Update status displays

### Testing Tasks

#### 1. Backend Tests
- [ ] **Document Status Transition Tests**
  - File: `backend/src/test/java/com/efile/core/document/DocumentServiceTest.java`
  - Test all valid status transitions
  - Test invalid transition rejections

- [ ] **Case Management Tests**
  - File: `backend/src/test/java/com/efile/core/casemanagement/CaseServiceTest.java`
  - Test case-document relationships
  - Test case progression logic

#### 2. Frontend Tests
- [ ] **Document Workflow Tests**
  - File: `frontend/src/components/__tests__/DocumentWorkflow.test.tsx`
  - Test status transition UI
  - Test permission-based action visibility

- [ ] **Case Detail Tests**
  - File: `frontend/src/pages/__tests__/CaseDetailPage.test.tsx`
  - Test document listing
  - Test case status updates

## Sprint 2: Enhanced Features (Priority: MEDIUM)

### Backend Tasks

#### 1. Multi-level Approval System
- [ ] **Create Approval Workflow Entity**
  - File: `backend/src/main/java/com/efile/core/approval/ApprovalWorkflow.java`
  - Define workflow steps and approvers

- [ ] **Implement Workflow Engine**
  - File: `backend/src/main/java/com/efile/core/approval/WorkflowService.java`
  - Sequential and parallel approvals
  - Approval delegation

#### 2. Case Templates
- [ ] **Create Case Template Entity**
  - File: `backend/src/main/java/com/efile/core/casemanagement/CaseTemplate.java`
  - Define required documents and workflows

- [ ] **Template Management Service**
  - File: `backend/src/main/java/com/efile/core/casemanagement/CaseTemplateService.java`
  - CRUD operations for templates

#### 3. Department Analytics
- [ ] **Create Analytics Service**
  - File: `backend/src/main/java/com/efile/core/analytics/AnalyticsService.java`
  - Department performance metrics
  - Processing time calculations

### Frontend Tasks

#### 1. Advanced UI Components
- [ ] **Workflow Visualization**
  - Create workflow progress components
  - Approval chain display

- [ ] **Template Management UI**
  - Case template creation and editing
  - Template-based case creation

#### 2. Analytics Dashboard
- [ ] **Department Analytics**
  - Performance charts
  - Processing time reports
  - Workload distribution

## Sprint 3: Production Features (Priority: LOW)

### Backend Tasks

#### 1. Document Versioning
- [ ] **Version Management Entity**
  - File: `backend/src/main/java/com/efile/core/document/DocumentVersion.java`
  - Track document changes

#### 2. Advanced Audit
- [ ] **Enhanced Audit Trail**
  - File: `backend/src/main/java/com/efile/core/audit/AuditLog.java`
  - Complete action tracking

### Frontend Tasks

#### 1. Advanced Features
- [ ] **Version History UI**
- [ ] **Audit Log Viewer**
- [ ] **Advanced Search Interface**

## Database Migration Scripts

### Migration 1: Update Document Statuses
```sql
-- File: V2__update_document_statuses.sql
UPDATE documents SET status = 'SUBMITTED' WHERE status = 'PENDING';
UPDATE documents SET status = 'WITHDRAWN' WHERE status = 'ARCHIVED';
-- Add any other status transitions needed
```

### Migration 2: Update Case Statuses
```sql
-- File: V3__update_case_statuses.sql
UPDATE cases SET status = 'CLOSED' WHERE status = 'ARCHIVED';
-- Add any other status transitions needed
```

## Deployment Checklist

### Pre-deployment
- [ ] All tests passing
- [ ] Database migrations tested on staging
- [ ] Frontend build successful
- [ ] API documentation updated

### Post-deployment
- [ ] Monitor for errors
- [ ] Verify data migration success
- [ ] Test key user workflows
- [ ] Performance monitoring

## Risk Mitigation

### Data Migration Risks
- Create database backups before migration
- Test migrations on copy of production data
- Have rollback plan ready

### Feature Rollout Risks
- Use feature flags for new functionality
- Gradual user rollout
- Monitor user feedback and errors

### Performance Risks
- Load test new workflows
- Monitor database query performance
- Optimize slow queries before release
