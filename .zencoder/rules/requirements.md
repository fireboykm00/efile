# Requirements Document

## Introduction

E-FileConnect is a secure, browser-based web application designed to digitize and streamline the submission, management, and official communication of legal and corporate documents for GAH (a company providing integrated services supporting modern, sustainable agriculture in Rwanda). The system enables executives (CEO, CFO), operational staff (Procurement Officer, Accountant, IT Officer), oversight personnel (Internal Auditor), and external stakeholders (Investors) to collaborate seamlessly through a unified platform that combines document filing with secure communication capabilities.

## Glossary

- **System**: The E-FileConnect web application
- **User**: Any authenticated person interacting with the System
- **Document**: A digital file uploaded to the System for management, approval, or review
- **Case**: A project or legal matter that groups related Documents and Communications
- **Communication**: A secure message exchange between Users within a Case context
- **Role**: A user classification that determines access permissions (ADMIN, CEO, CFO, PROCUREMENT, ACCOUNTANT, AUDITOR, IT, INVESTOR)
- **Dashboard**: The main interface displaying user-specific information and pending actions
- **2FA**: Two-Factor Authentication using OTP or email verification
- **JWT**: JSON Web Token used for secure authentication
- **Audit Trail**: A chronological record of system activities for compliance tracking

## Requirements

### Requirement 1: User Authentication and Security

**User Story:** As a User, I want to securely log into the System with two-factor authentication, so that my account and sensitive documents are protected from unauthorized access.

#### Acceptance Criteria

1. WHEN a User submits valid credentials, THE System SHALL authenticate the User and issue a JWT token with a 24-hour expiration period
2. WHEN a User enables 2FA, THE System SHALL send a one-time password to the User's registered email address within 30 seconds
3. WHEN a User enters an incorrect OTP three consecutive times, THE System SHALL lock the account for 15 minutes
4. THE System SHALL encrypt all password data using bcrypt hashing with a minimum cost factor of 10
5. WHEN a JWT token expires, THE System SHALL require the User to re-authenticate before accessing protected resources

### Requirement 2: Role-Based Access Control

**User Story:** As an Administrator, I want to assign specific roles to Users, so that each person has appropriate access permissions based on their organizational responsibilities.

#### Acceptance Criteria

1. THE System SHALL support eight distinct roles: ADMIN, CEO, CFO, PROCUREMENT, ACCOUNTANT, AUDITOR, IT, and INVESTOR
2. WHEN a User is assigned a role, THE System SHALL enforce role-specific permissions for all subsequent actions
3. WHEN an AUDITOR User requests access to financial documents, THE System SHALL grant read access to all financial and compliance records
4. WHEN an INVESTOR User attempts to modify a Document, THE System SHALL deny the request and return an authorization error
5. THE System SHALL store the role assignment in the User table as an ENUM field

### Requirement 3: User Management

**User Story:** As an Administrator, I want to create, update, and deactivate User accounts, so that I can manage system access for all organizational personnel and external stakeholders.

#### Acceptance Criteria

1. WHEN an Administrator creates a new User account, THE System SHALL require name, email, password, and role fields
2. WHEN an Administrator assigns a User to a Department, THE System SHALL validate that the Department exists in the database
3. THE System SHALL enforce unique email addresses across all User accounts
4. WHEN an Administrator deactivates a User account, THE System SHALL prevent that User from authenticating while preserving historical audit records
5. WHEN a User updates their profile information, THE System SHALL log the change with timestamp and modifier identification

### Requirement 4: Document Upload and Validation

**User Story:** As a User with document submission permissions, I want to upload files with automatic validation, so that only compliant documents enter the System.

#### Acceptance Criteria

1. WHEN a User uploads a Document, THE System SHALL validate that the file size does not exceed 10 megabytes
2. WHEN a User uploads a Document, THE System SHALL verify that the file type is PDF, DOCX, XLSX, or PNG
3. WHEN a Document passes validation, THE System SHALL store the file path, upload timestamp, and uploader identification in the database
4. WHEN a Document fails validation, THE System SHALL return a descriptive error message within 2 seconds
5. THE System SHALL associate each uploaded Document with a Case identifier

### Requirement 5: Document Approval Workflow

**User Story:** As a CEO or CFO, I want to review and approve submitted documents, so that only authorized materials proceed to the next workflow stage.

#### Acceptance Criteria

1. WHEN a Document requires approval, THE System SHALL display it in the approver's Dashboard with pending status
2. WHEN a CEO User approves a high-level Document, THE System SHALL update the Document status to "APPROVED" and record the approval timestamp
3. WHEN a CFO User rejects a financial Document, THE System SHALL update the status to "REJECTED" and require a rejection reason of at least 10 characters
4. THE System SHALL send an email notification to the Document uploader within 5 minutes of approval or rejection
5. WHEN a Document is approved, THE System SHALL make it visible to Users with appropriate role permissions

### Requirement 6: Document Tracking and Status

**User Story:** As a User who submitted a document, I want to track its current status in real-time, so that I know when action is required or when processing is complete.

#### Acceptance Criteria

1. THE System SHALL display Document status as one of: PENDING, UNDER_REVIEW, APPROVED, REJECTED, or ARCHIVED
2. WHEN a Document status changes, THE System SHALL update the Dashboard view within 3 seconds
3. WHEN a User views a Document, THE System SHALL display the complete history of status changes with timestamps
4. THE System SHALL provide a search function that filters Documents by status, type, and date range
5. WHEN a User requests Document history, THE System SHALL return results within 2 seconds for datasets up to 10,000 records

### Requirement 7: Case Management

**User Story:** As a CEO or Project Manager, I want to create and manage Cases that group related documents and communications, so that I can organize work by project or legal matter.

#### Acceptance Criteria

1. WHEN a User creates a Case, THE System SHALL require a title of at least 5 characters and a description of at least 20 characters
2. THE System SHALL assign a unique case identifier to each new Case
3. WHEN a User assigns a Case to another User, THE System SHALL validate that the assignee exists and has appropriate role permissions
4. WHEN a User views a Case, THE System SHALL display all associated Documents and Communications in chronological order
5. THE System SHALL support Case status values: OPEN, IN_PROGRESS, CLOSED, and ARCHIVED

### Requirement 8: Secure Communication

**User Story:** As a User involved in a Case, I want to send secure messages to other Case participants, so that I can discuss matters without using external communication channels.

#### Acceptance Criteria

1. WHEN a User sends a Communication, THE System SHALL associate it with a specific Case identifier
2. THE System SHALL record the sender identification, recipient identification, timestamp, and message content for each Communication
3. WHEN a User receives a Communication, THE System SHALL display an unread notification badge on the Dashboard
4. THE System SHALL encrypt Communication content at rest using AES-256 encryption
5. WHEN an INVESTOR User attempts to send a Communication, THE System SHALL restrict recipients to Users with ADMIN, CEO, or CFO roles

### Requirement 9: Dashboard and Notifications

**User Story:** As a User, I want a personalized Dashboard that shows my pending tasks and recent activities, so that I can prioritize my work efficiently.

#### Acceptance Criteria

1. WHEN a User logs in, THE System SHALL display a Dashboard with pending Documents, assigned Cases, and unread Communications
2. THE System SHALL group Dashboard items by priority: high (overdue), medium (due within 3 days), and low (due after 3 days)
3. WHEN a new Document requires the User's approval, THE System SHALL add it to the Dashboard within 5 seconds
4. THE System SHALL display the count of pending items for each category on the Dashboard
5. WHEN a User clicks a Dashboard item, THE System SHALL navigate to the detailed view within 1 second

### Requirement 10: Audit Trail and Compliance

**User Story:** As an Internal Auditor, I want to access comprehensive audit logs of all system activities, so that I can ensure compliance and investigate any irregularities.

#### Acceptance Criteria

1. THE System SHALL log all User authentication attempts with timestamp, IP address, and success/failure status
2. THE System SHALL log all Document uploads, approvals, rejections, and deletions with User identification and timestamp
3. WHEN an AUDITOR User requests audit logs, THE System SHALL provide filtering by date range, User, and activity type
4. THE System SHALL retain audit logs for a minimum of 7 years
5. WHEN an audit log entry is created, THE System SHALL ensure it cannot be modified or deleted by any User role

### Requirement 11: Department Management

**User Story:** As an Administrator, I want to organize Users into Departments with designated heads, so that the organizational structure is reflected in the System.

#### Acceptance Criteria

1. WHEN an Administrator creates a Department, THE System SHALL require a unique department name of at least 3 characters
2. THE System SHALL allow assignment of one User as the Department head
3. WHEN a User is assigned to a Department, THE System SHALL update the User's department association in the database
4. THE System SHALL display Department membership on User profile pages
5. WHEN an Administrator deletes a Department, THE System SHALL require reassignment of all associated Users before deletion

### Requirement 12: Digital Receipts and Acknowledgments

**User Story:** As a User who submits documents, I want to receive automatic digital receipts, so that I have proof of submission for my records.

#### Acceptance Criteria

1. WHEN a User successfully uploads a Document, THE System SHALL generate a digital receipt with a unique receipt number
2. THE System SHALL include the Document title, upload timestamp, Case identifier, and receipt number in the digital receipt
3. THE System SHALL send the digital receipt to the User's registered email address within 2 minutes
4. THE System SHALL provide a download option for the digital receipt in PDF format
5. WHEN a User requests a receipt for a previously uploaded Document, THE System SHALL regenerate and deliver it within 30 seconds

### Requirement 13: Search and Filtering

**User Story:** As a User, I want to search for documents and cases using keywords and filters, so that I can quickly locate relevant information.

#### Acceptance Criteria

1. THE System SHALL provide a search function that accepts keywords of at least 3 characters
2. WHEN a User performs a search, THE System SHALL return results matching Document titles, Case titles, or descriptions
3. THE System SHALL support filtering search results by Document type, status, date range, and assigned User
4. THE System SHALL return search results within 3 seconds for datasets up to 50,000 records
5. WHEN a User has insufficient permissions to view a search result, THE System SHALL exclude it from the results list

### Requirement 14: Responsive User Interface

**User Story:** As a User accessing the System from various devices, I want a responsive interface that adapts to different screen sizes, so that I can work efficiently on desktop, tablet, or mobile devices.

#### Acceptance Criteria

1. THE System SHALL render all interface elements correctly on screen widths from 320 pixels to 2560 pixels
2. WHEN a User accesses the System on a mobile device, THE System SHALL display a touch-optimized navigation menu
3. THE System SHALL maintain readability with font sizes between 14 and 18 pixels on mobile devices
4. WHEN a User rotates a mobile device, THE System SHALL adjust the layout within 1 second
5. THE System SHALL ensure all interactive elements have a minimum touch target size of 44 by 44 pixels on mobile devices

### Requirement 15: Data Export and Reporting

**User Story:** As a CFO or CEO, I want to export document and case data in standard formats, so that I can perform external analysis and generate reports.

#### Acceptance Criteria

1. WHEN a User with CEO or CFO role requests a data export, THE System SHALL generate files in CSV or PDF format
2. THE System SHALL include all visible columns and applied filters in the exported data
3. WHEN a User exports more than 1,000 records, THE System SHALL process the request asynchronously and send a download link via email
4. THE System SHALL complete exports of up to 10,000 records within 60 seconds
5. WHEN an INVESTOR User attempts to export data, THE System SHALL restrict the export to investment reports and company updates only
