package com.efile.core.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.casemanagement.CaseStatus;
import com.efile.core.communication.Communication;
import com.efile.core.communication.CommunicationRepository;
import com.efile.core.communication.CommunicationType;
import com.efile.core.department.Department;
import com.efile.core.department.DepartmentRepository;
import com.efile.core.document.Document;
import com.efile.core.document.DocumentRepository;
import com.efile.core.document.DocumentStatus;
import com.efile.core.document.DocumentType;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final CommunicationRepository communicationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                     DepartmentRepository departmentRepository,
                     CaseRepository caseRepository,
                     DocumentRepository documentRepository,
                     CommunicationRepository communicationRepository,
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
        this.communicationRepository = communicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedDepartments();
        seedUsers();
        seedCases();
        seedDocuments();
        seedCommunications();
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            Department[] departments = {
                createDepartment("Executive"),
                createDepartment("Finance"),
                createDepartment("Procurement"),
                createDepartment("Accounting"),
                createDepartment("Audit"),
                createDepartment("IT"),
                createDepartment("Legal")
            };

            for (Department dept : departments) {
                departmentRepository.save(dept);
            }
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            Department executive = departmentRepository.findByName("Executive").orElse(null);
            Department finance = departmentRepository.findByName("Finance").orElse(null);
            Department it = departmentRepository.findByName("IT").orElse(null);

            // Create admin user
            User admin = createUser("System Admin", "admin@efile.com", "admin123", UserRole.ADMIN, it);
            userRepository.save(admin);

            // Create CEO
            User ceo = createUser("John CEO", "ceo@efile.com", "ceo123", UserRole.CEO, executive);
            userRepository.save(ceo);

            // Create CFO
            User cfo = createUser("Jane CFO", "cfo@efile.com", "cfo123", UserRole.CFO, finance);
            userRepository.save(cfo);

            // Assign department heads
            if (executive != null) {
                executive.setHead(ceo);
                departmentRepository.save(executive);
            }
            if (finance != null) {
                finance.setHead(cfo);
                departmentRepository.save(finance);
            }

            System.out.println("✅ Seeded initial users:");
            System.out.println("   Admin: admin@efile.com / admin123");
            System.out.println("   CEO: ceo@efile.com / ceo123 (Executive Head)");
            System.out.println("   CFO: cfo@efile.com / cfo123 (Finance Head)");
        }
    }

    private Department createDepartment(String name) {
        Department dept = new Department();
        dept.setName(name);
        return dept;
    }

    private User createUser(String name, String email, String password, UserRole role, Department department) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(true);
        return user;
    }

    private void seedCases() {
        if (caseRepository.count() == 0) {
            User admin = userRepository.findByEmailIgnoreCase("admin@efile.com").orElse(null);
            User ceo = userRepository.findByEmailIgnoreCase("ceo@efile.com").orElse(null);

            if (admin != null && ceo != null) {
                Case case1 = new Case();
                case1.setTitle("Contract Review - Q1 2024");
                case1.setDescription("Review and approval of Q1 2024 vendor contracts");
                case1.setStatus(CaseStatus.OPEN);
                case1.setCreatedBy(admin);
                case1.setAssignedTo(ceo);
                caseRepository.save(case1);

                Case case2 = new Case();
                case2.setTitle("Budget Allocation - Finance");
                case2.setDescription("Annual budget allocation for Finance department");
                case2.setStatus(CaseStatus.ACTIVE);
                case2.setCreatedBy(admin);
                case2.setAssignedTo(ceo);
                caseRepository.save(case2);

                System.out.println("✅ Seeded 2 test cases");
            }
        }
    }

    private void seedDocuments() {
        if (documentRepository.count() == 0) {
            User admin = userRepository.findByEmailIgnoreCase("admin@efile.com").orElse(null);
            Case case1 = caseRepository.findAll().stream().findFirst().orElse(null);

            if (admin != null && case1 != null) {
                Document doc1 = new Document();
                doc1.setTitle("Contract_Q1_2024.pdf");
                doc1.setType(DocumentType.LEGAL_DOCUMENT);
                doc1.setFilePath("/uploads/contracts/contract_q1_2024.pdf");
                doc1.setFileSize(1024000L);
                doc1.setStatus(DocumentStatus.DRAFT);
                doc1.setCaseRef(case1);
                doc1.setUploadedBy(admin);
                doc1.setReceiptNumber("REC-2024-001");
                documentRepository.save(doc1);

                Document doc2 = new Document();
                doc2.setTitle("Budget_Proposal.xlsx");
                doc2.setType(DocumentType.FINANCIAL_REPORT);
                doc2.setFilePath("/uploads/reports/budget_proposal.xlsx");
                doc2.setFileSize(512000L);
                doc2.setStatus(DocumentStatus.UNDER_REVIEW);
                doc2.setCaseRef(case1);
                doc2.setUploadedBy(admin);
                doc2.setReceiptNumber("REC-2024-002");
                documentRepository.save(doc2);

                System.out.println("✅ Seeded 2 test documents");
            }
        }
    }

    private void seedCommunications() {
        if (communicationRepository.count() == 0) {
            User admin = userRepository.findByEmailIgnoreCase("admin@efile.com").orElse(null);
            User ceo = userRepository.findByEmailIgnoreCase("ceo@efile.com").orElse(null);
            Case case1 = caseRepository.findAll().stream().findFirst().orElse(null);

            if (admin != null && ceo != null && case1 != null) {
                Communication comm1 = new Communication();
                comm1.setType(CommunicationType.MESSAGE);
                comm1.setContent("Please review the contract and provide feedback");
                comm1.setRead(false);
                comm1.setSender(admin);
                comm1.setRecipient(ceo);
                comm1.setCaseRef(case1);
                communicationRepository.save(comm1);

                Communication comm2 = new Communication();
                comm2.setType(CommunicationType.NOTIFICATION);
                comm2.setContent("New document uploaded: Budget_Proposal.xlsx");
                comm2.setRead(false);
                comm2.setSender(admin);
                comm2.setRecipient(ceo);
                comm2.setCaseRef(case1);
                communicationRepository.save(comm2);

                System.out.println("✅ Seeded 2 test communications");
            }
        }
    }
}
