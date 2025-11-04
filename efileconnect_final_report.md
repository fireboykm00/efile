## University of Lay Adventists of Kigali (UNILAK)  
**Faculty of Computing and Information Technology**  
**Module:** MSIT6120 – Advanced Programming Concepts and Emerging Technologies  

### FINAL PROJECT REPORT  
**Project Title:** E-FileConnect – Enhanced E-Filing Web Application  

**Submitted by:** [Your Name]  
**Date:** November 2025  

---

### **Abstract**

E-FileConnect is a secure, browser-based web application designed to digitize and streamline the submission, management, and official communication of legal documents. The system enables attorneys, clients, clerks, judges, and key company stakeholders such as **CEO, CFO, Procurement Officer, Accountant, Internal Auditor, IT Officer, and external Investors** to operate seamlessly. It is designed as part of a case study for a company called **GAH**, which provides integrated services supporting modern, sustainable agriculture in Rwanda. Through world-class infrastructure, expert support, and inclusive partnerships, GAH delivers end-to-end solutions that empower investors and uplift communities.

By integrating communication with document filing, E-FileConnect reduces processing delays, ensures compliance with electronic service rules, and provides real-time notifications. The system was implemented using **Spring Boot**, **React**, and **MySQL**. It delivers an accessible, paperless solution aimed at improving transparency, accessibility, and operational efficiency in both legal and corporate documentation workflows.

---

### **Introduction**

Traditional document filing and communication systems in both legal and corporate sectors cause inefficiencies, lost documents, and delays in decision-making. E-FileConnect addresses these challenges by providing a unified, secure, and digital platform for users to file documents, manage workflows, and communicate with authorized personnel.  

The system combines modern web technologies with secure role-based access, audit trails, and integrated notifications, offering a complete document and communication workflow solution suitable for organizations like GAH that manage legal, financial, and operational records.

---

### **Objectives**

The main objectives of this system are:

1. To enable digital submission and management of legal and corporate documents.  
2. To facilitate secure communication between internal teams and external investors.  
3. To ensure simple role-based access and accountability using a single role field.  
4. To reduce manual paperwork and document processing time.  
5. To improve transparency and traceability through audit trails and real-time tracking.

---

### **System Description**

E-FileConnect is structured around four main modules:  

1. **User and Role Management:** Handles authentication and role-based access control for users such as CEO, CFO, Accountant, Procurement, IT, Auditor, and external investors.  
2. **Document Management:** Enables uploading, validation, approval, and digital signing of key files and reports.  
3. **Official Communication:** Provides secure, timestamped message exchanges and notifications between stakeholders.  
4. **Case/Project Management:** Allows tracking and review of corporate or legal cases within GAH’s operational departments.

Users interact through a web interface that communicates with RESTful APIs for all database operations.  

---

### **System Design**

#### **Entity Relationship Diagram (ERD)**  

```
User (user_id PK, name, email, password_hash, role ENUM('ADMIN','CEO','CFO','PROCUREMENT','ACCOUNTANT','AUDITOR','IT','INVESTOR'), created_at)
Case (case_id PK, title, description, status, assigned_to FK (User.user_id), created_at)
Document (doc_id PK, title, type, file_path, upload_date, case_id FK, uploaded_by FK)
Communication (comm_id PK, type, content, timestamp, sender_id FK, recipient_id FK, case_id FK)
Department (dept_id PK, name, head_id FK (User.user_id))
```

**Relationships:**
- One User belongs to one Department.  
- One Department can have many Users.  
- One Case involves multiple Documents and Users.  
- Each Document is uploaded by one User.  
- Communication links Users within a Case context.  

---

### **User Roles (Simplified RBAC Model)**

The system uses a **simple role-based model** with a single `role` field in the `User` table. Roles are represented as an ENUM field for consistency and easy role checks in backend logic.

#### **UserRole Enum Example**
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

#### **Role Summary**

| **Role** | **Description** | **Key Actions** |
|-----------|-----------------|-----------------|
| **Admin** | Manages users and monitors system activity. | Create users, manage roles, view logs. |
| **CEO** | Oversees company projects and document workflows. | Approve high-level documents, view reports. |
| **CFO** | Manages financial records and reports. | Approve budgets, track invoices. |
| **Procurement** | Handles supplier and tender documents. | Upload procurement files, approve bids. |
| **Accountant** | Manages accounting documents. | File financial statements, verify transactions. |
| **Auditor** | Reviews compliance and audit reports. | Access all financial and compliance records. |
| **IT** | Manages technical system configurations. | Maintain users, perform security checks. |
| **Investor** | External party reviewing reports and updates. | View investment reports and company updates. |

---

### **Key Features and User Benefits**

| *Feature*             | *Description*                                    | *User Benefit*     |
| ----------------------- | -------------------------------------------------- | -------------------- |
| *Secure Login (2FA)*  | Uses OTP or email verification                     | Enhances security    |
| *Intuitive Dashboard* | Displays pending, submitted, and approved files    | Simplifies workflow  |
| *Smart Forms*         | Auto-validates inputs before submission            | Reduces errors       |
| *Tracking System*     | Real-time document status                          | Builds trust         |
| *Digital Receipts*    | Automatic acknowledgment on submission             | Proof of filing      |
| *Admin Tools*         | Manage users, review submissions, generate reports | Increases efficiency |

---

### **Implementation**

**Backend:**  
Developed with **Spring Boot**, exposing RESTful APIs for CRUD operations on users, cases, documents, and communications. Authentication is managed with JWT, and data persistence is handled through **MySQL** using JPA and Hibernate ORM.

**Frontend:**  
Built using **React.js** for an interactive and responsive user interface. Axios handles API communication, and Bootstrap ensures responsive layouts.

**Database:**  
Implemented in **MySQL**, ensuring referential integrity through foreign keys. Tables include `users`, `cases`, `documents`, `communications`, and `departments`.

---

### **Testing**

Testing was conducted using Postman for backend API validation and manual browser testing for the frontend.  
Key tested features include:
- User registration and login with 2FA  
- Department-based role assignment  
- Document upload and approval workflows  
- Secure case communication  
- Role-based access using `UserRole` enum  

All modules performed as expected, and input validations were correctly enforced.

---

### **Challenges and Solutions**

| **Challenge** | **Solution** |
|----------------|--------------|
| Role diversity across departments | Unified all user types into a single `UserRole` enum |
| File upload and storage validation | Implemented file type and size restrictions using Spring Boot’s MultipartFile handling |
| Managing external investor access | Added secure view-only endpoints with authentication tokens |
| Data security and audit logging | Applied JWT authentication, encryption, and activity tracking |

---

### **Conclusion and Recommendations**

E-FileConnect successfully provides a modern, digital solution that bridges legal and corporate document management. It improves coordination among executives, auditors, and investors by offering a unified, secure communication and filing platform.  

Future enhancements could include:
- AI-assisted document categorization  
- Advanced analytics dashboards  
- Mobile app integration for executives and investors  
- Integration with external ERP systems  

E-FileConnect demonstrates how technology can transform administrative efficiency and transparency for organizations like GAH in Rwanda.

---

### **References**

1. Spring Boot Documentation – https://spring.io/projects/spring-boot  
2. React.js Official Documentation – https://react.dev  
3. Hibernate ORM Guide – https://hibernate.org/orm/documentation/  
4. MySQL Developer Guide – https://dev.mysql.com/doc/  
5. OWASP Security Guidelines – https://owasp.org