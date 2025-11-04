# E-FileConnect

E-FileConnect is a secure, browser-based web application designed to digitize and streamline the submission, management, and official communication of legal documents. The system enables attorneys, clients, clerks, judges, and key company stakeholders such as **CEO, CFO, Procurement Officer, Accountant, Internal Auditor, IT Officer, and external Investors** to operate seamlessly. It is designed as part of a case study for a company called **GAH**, which provides integrated services supporting modern, sustainable agriculture in Rwanda. Through world-class infrastructure, expert support, and inclusive partnerships, GAH delivers end-to-end solutions that empower investors and uplift communities.

## Features

- **User and Role Management**: Handles authentication and role-based access control for users such as CEO, CFO, Accountant, Procurement, IT, Auditor, and external investors.
- **Document Management**: Enables uploading, validation, approval, and digital signing of key files and reports.
- **Official Communication**: Provides secure, timestamped message exchanges and notifications between stakeholders.
- **Case/Project Management**: Allows tracking and review of corporate or legal cases within GAH's operational departments.
- **Secure Login with 2FA**: Uses OTP or email verification for enhanced security.
- **Intuitive Dashboard**: Displays pending, submitted, and approved files to simplify workflow.
- **Smart Forms**: Auto-validates inputs before submission to reduce errors.
- **Tracking System**: Real-time document status for building trust.
- **Digital Receipts**: Automatic acknowledgment on submission as proof of filing.
- **Admin Tools**: Manage users, review submissions, generate reports for increased efficiency.

## Tech Stack

### Backend
- **Spring Boot** 3.5.7
- **Java** 21
- **Spring Data JPA** with Hibernate ORM
- **Spring WebFlux** for reactive web services
- **MySQL** database
- **Lombok** for reducing boilerplate code

### Frontend
- **React** 19.1.1 with TypeScript
- **Vite** for build tooling
- **Tailwind CSS** for styling
- **Radix UI** components
- **React Hook Form** with Zod validation
- **Axios** for API communication

## Installation

### Prerequisites
- Java 21
- Node.js (latest LTS)
- MySQL database
- Maven

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Configure your MySQL database in `src/main/resources/application.properties`

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

## Usage

1. Access the application at `http://localhost:5173` (frontend)
2. Backend API runs on `http://localhost:8080`
3. Register or login with appropriate role-based access
4. Upload documents, manage cases, and communicate securely

## User Roles

The system uses a simplified role-based access control with the following roles:

- **Admin**: Manages users and monitors system activity
- **CEO**: Oversees company projects and document workflows
- **CFO**: Manages financial records and reports
- **Procurement**: Handles supplier and tender documents
- **Accountant**: Manages accounting documents
- **Auditor**: Reviews compliance and audit reports
- **IT**: Manages technical system configurations
- **Investor**: External party reviewing reports and updates

## Testing

- Backend APIs tested with Postman
- Frontend tested manually in browser
- Key features include user registration/login with 2FA, document workflows, and role-based access

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and linting
5. Submit a pull request

## License

This project is developed as part of a university case study for GAH company.
