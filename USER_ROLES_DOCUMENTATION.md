# E-FileConnect User Roles Documentation

## Overview

E-FileConnect is a secure document management system designed for GAH (agricultural company in Rwanda). The system supports multiple user roles with specific permissions and responsibilities. This document outlines how each role interacts with the system.

## User Roles and Permissions

### 1. **ADMIN** (System Administrator)
**Primary Responsibilities:**
- Complete system administration and user management
- Create, update, and deactivate user accounts
- Manage department structures and assignments
- Oversee system security and access controls
- Generate system-wide reports and analytics

**System Access:**
- Full access to all system features
- User Management tab with create/edit/delete user capabilities
- Department Management with full CRUD operations
- All reports and analytics
- System configuration access

**Typical Users:** IT administrators, system managers

---

### 2. **CEO** (Chief Executive Officer)
**Primary Responsibilities:**
- Oversee company-wide document workflows
- Review high-level reports and analytics
- Approve critical business documents
- Monitor executive dashboard metrics
- Strategic decision-making based on system data

**System Access:**
- Dashboard with executive overview (growth metrics, processing times, efficiency)
- Document approval/rejection capabilities
- Access to all document types
- Reports on company performance
- Case management for strategic initiatives

**Typical Workflow:**
1. Login and review executive dashboard
2. Monitor document processing metrics
3. Review pending approvals for critical documents
4. Access comprehensive reports
5. Create strategic cases for company initiatives

---

### 3. **CFO** (Chief Financial Officer)
**Primary Responsibilities:**
- Manage financial documents and reports
- Oversee budget and procurement processes
- Review financial compliance documents
- Monitor financial KPIs and metrics
- Approve financial transactions and reports

**System Access:**
- Full access to FINANCIAL_REPORT and procurement documents
- Executive dashboard with financial metrics
- Budget and financial case management
- Financial compliance reporting
- Procurement document workflows

**Typical Workflow:**
1. Review financial dashboard metrics
2. Process financial document approvals
3. Monitor budget-related cases
4. Generate financial reports
5. Oversee procurement document flows

---

### 4. **PROCUREMENT** (Procurement Officer)
**Primary Responsibilities:**
- Manage supplier contracts and tenders
- Process procurement documentation
- Handle vendor compliance documents
- Track procurement workflows
- Coordinate with finance for approvals

**System Access:**
- Upload and manage PROCUREMENT_BID documents
- Create procurement-related cases
- Track document approval status
- Access procurement reports
- Limited financial document access

**Typical Workflow:**
1. Upload procurement documents (RFPs, contracts)
2. Create cases for procurement processes
3. Monitor approval workflows
4. Track supplier compliance documents
5. Generate procurement reports

---

### 5. **ACCOUNTANT** (Accounting Staff)
**Primary Responsibilities:**
- Process accounting documents and transactions
- Handle financial record management
- Process expense reports and invoices
- Maintain accounting compliance
- Support financial reporting processes

**System Access:**
- Upload accounting documents
- Process financial transactions
- Access accounting-related cases
- Limited financial reporting
- Invoice and expense management

**Typical Workflow:**
1. Upload accounting documents (invoices, receipts)
2. Process expense reports
3. Handle transaction approvals
4. Maintain accounting records
5. Support audit preparations

---

### 6. **AUDITOR** (Internal/External Auditor)
**Primary Responsibilities:**
- Review compliance and audit documents
- Assess financial and operational controls
- Generate audit reports
- Monitor system compliance
- Review document approval processes

**System Access:**
- Access to AUDIT_REPORT documents
- Read-only access to most document types
- Generate compliance reports
- Audit trail reviews
- System monitoring capabilities

**Typical Workflow:**
1. Review audit documents and reports
2. Assess document approval processes
3. Generate compliance reports
4. Monitor system usage and controls
5. Review audit trails

---

### 7. **IT** (IT Officer)
**Primary Responsibilities:**
- Manage technical system configurations
- Handle IT-related documentation
- Support system maintenance
- Monitor system performance
- Assist with technical issues

**System Access:**
- Department management access
- System monitoring capabilities
- Technical documentation management
- Limited user management
- System configuration access

**Typical Workflow:**
1. Monitor system performance
2. Manage IT documentation
3. Assist with department configurations
4. Handle technical support cases
5. Review system logs and reports

---

### 8. **INVESTOR** (External Investor)
**Primary Responsibilities:**
- Review investment-related reports
- Monitor company performance
- Access financial summaries
- Review strategic documents
- Track investment progress

**System Access:**
- Read-only access to approved documents
- Limited dashboard access
- Investment report access
- Company performance metrics
- Strategic document reviews

**Typical Workflow:**
1. Review investor dashboard
2. Access approved financial reports
3. Monitor company performance metrics
4. Review strategic documents
5. Track investment-related cases

## Document Types by Role

| Document Type | ADMIN | CEO | CFO | PROC | ACCT | AUDIT | IT | INV |
|---------------|-------|-----|-----|------|------|-------|----|-----|
| FINANCIAL_REPORT | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ |
| PROCUREMENT_BID | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| LEGAL_DOCUMENT | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| AUDIT_REPORT | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| INVESTMENT_REPORT | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ |
| GENERAL | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |

## Case Management by Role

| Case Type | ADMIN | CEO | CFO | PROC | ACCT | AUDIT | IT | INV |
|-----------|-------|-----|-----|------|------|-------|----|-----|
| Executive Cases | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Financial Cases | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ |
| Procurement Cases | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Audit Cases | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| IT Cases | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| General Cases | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |

## Navigation and UI Access

### Sidebar Navigation by Role

**ADMIN:**
- Dashboard
- Documents
- Cases
- Reports
- User Management

**CEO/CFO:**
- Dashboard (with executive overview)
- Documents
- Cases
- Reports

**PROCUREMENT/ACCOUNTANT/AUDITOR/IT:**
- Dashboard
- Documents
- Cases
- Reports (limited)

**INVESTOR:**
- Dashboard (limited)
- Documents (read-only)
- Reports (limited)

## Security and Access Control

- **JWT Authentication:** All users authenticate via email/password
- **Role-based Access:** Permissions enforced at API and UI levels
- **Department Isolation:** Users can only see department-relevant content
- **Audit Trails:** All actions are logged for compliance
- **Session Management:** Automatic logout on inactivity

## Getting Started for New Users

1. **Account Creation:** Admin creates account with appropriate role
2. **First Login:** Use provided email and temporary password
3. **Password Change:** Required to change password on first login
4. **Profile Setup:** Complete profile information
5. **Training:** Review role-specific documentation
6. **Access Testing:** Verify correct permissions and access

## Support and Help

- **ADMIN:** Contact system administrator for account issues
- **All Users:** Use in-app help or contact department head
- **Technical Issues:** Contact IT department
- **Process Questions:** Contact department supervisor

## System Requirements

- Modern web browser (Chrome, Firefox, Safari, Edge)
- Stable internet connection
- JavaScript enabled
- Cookies enabled for session management

---

*This documentation should be updated as new roles or permissions are added to the system.*