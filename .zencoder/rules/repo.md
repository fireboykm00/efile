---
description: Repository Information Overview
alwaysApply: true
---

# Repository Information Overview

## Repository Summary
E-FileConnect is a secure, browser-based web application for digitizing and streamlining legal document submission, management, and official communication. Built as a case study for GAH company, it supports role-based access for attorneys, clients, clerks, judges, and corporate stakeholders with features including document management, 2FA authentication, digital signing, and real-time tracking.

## Repository Structure
- **backend/**: Spring Boot 3 REST API service (Java 21, Maven)
- **frontend/**: React 19 single-page application (TypeScript, Vite)
- **.kiro/**: Kiro specifications and configuration
- **README.md**: Project documentation with setup instructions

### Main Repository Components
- **Backend Service**: Core Spring Boot application handling authentication, document management, and business logic
- **Frontend Application**: React-based UI with role-based dashboards for different user types
- **Database**: MySQL integration for persistent data storage
- **API Gateway**: Spring WebFlux reactive endpoints

---

## Projects

### Backend Service
**Configuration File**: `backend/pom.xml`

#### Language & Runtime
**Language**: Java
**Version**: 21
**Build System**: Maven
**Package Manager**: Maven (mvnw executable wrapper included)

#### Dependencies
**Main Dependencies**:
- Spring Boot 3.5.7
- Spring Data JPA with Hibernate ORM
- Spring WebFlux (reactive web services)
- MySQL Connector J
- Lombok (boilerplate reduction)

**Development Dependencies**:
- Spring Boot Test
- Reactor Test

#### Build & Installation
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

#### Main Files & Resources
**Entry Point**: `backend/src/main/java/com/efile/core/CoreApplication.java`
**Configuration**: `backend/src/main/resources/application.properties`
**Package Structure**: `com.efile.core.*` (controllers, services, entities, repositories)

#### Testing
**Framework**: JUnit 5 (Spring Boot Test)
**Test Location**: `backend/src/test/java/com/efile/core/`
**Configuration**: Reactor Test for reactive testing
**Run Command**:
```bash
cd backend
./mvnw test
```

---

### Frontend Application
**Configuration File**: `frontend/package.json`

#### Language & Runtime
**Language**: TypeScript
**Version**: 5.9.3
**Build Tool**: Vite 7.1.7
**Runtime**: Node.js (latest LTS recommended)
**Package Manager**: npm (bun.lock indicates Bun compatibility)

#### Dependencies
**Main Dependencies**:
- React 19.1.1
- React DOM 19.1.1
- React Hook Form 7.66.0 (form state management)
- Zod 4.1.12 (schema validation)
- Tailwind CSS 4.1.16 (styling)
- @radix-ui/* (accessible component library)
- Lucide React (icon library)
- Axios (HTTP client - from README)

**Development Dependencies**:
- TypeScript ESLint
- Vite React plugin
- Tailwind CSS Vite plugin

#### Build & Installation
```bash
cd frontend
npm install
npm run dev          # Development server
npm run build        # Production build
npm run lint         # ESLint checks
```

#### Main Files & Resources
**Entry Point**: `frontend/src/main.tsx`
**Application Root**: `frontend/src/App.tsx`
**Configuration**: `frontend/vite.config.ts`, `frontend/tsconfig.json`
**Styles**: `frontend/src/index.css`
**Component Structure**: `frontend/src/components/ui/` (Radix UI components), `frontend/src/components/` (custom components)

#### Development Server
- Runs on `http://localhost:5173`
- Hot module replacement enabled
- TypeScript strict mode

#### Docker
**Dockerfile**: Not found in repository
**Build Artifact**: Static bundle output to `dist/` directory for deployment

---

## Integration
**Backend API**: Runs on `http://localhost:8080`
**Frontend UI**: Runs on `http://localhost:5173` in development
**Communication**: Axios HTTP client for API calls
**Database**: MySQL connection configured in backend properties

## Key Features Implementation
- **Authentication**: 2FA support with OTP/email verification
- **Authorization**: Role-based access control (Admin, CEO, CFO, Procurement, Accountant, Auditor, IT, Investor)
- **Document Management**: Upload, validation, approval, and digital signing workflows
- **Real-time Tracking**: Document status monitoring
- **Secure Communication**: Timestamped messaging between stakeholders

