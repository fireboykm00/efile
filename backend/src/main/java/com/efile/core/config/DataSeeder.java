package com.efile.core.config;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.casemanagement.CaseStatus;
import com.efile.core.casemanagement.CasePriority;
import com.efile.core.casemanagement.CaseCategory;
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
                departmentRepository.save(dept != null ? dept : createDepartment("General"));
            }
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            Department executive = departmentRepository.findByName("Executive").orElse(null);
            Department finance = departmentRepository.findByName("Finance").orElse(null);
            Department it = departmentRepository.findByName("IT").orElse(null);

            // Create admin user
            User admin = createUser("System Admin", "admin@efile.com", "admin123", UserRole.ADMIN, it != null ? it : createDepartment("IT"));
            userRepository.save(admin);

            // Create CEO
            User ceo = createUser("CEO User", "ceo@efile.com", "ceo123", UserRole.CEO, executive != null ? executive : createDepartment("Executive"));
            userRepository.save(ceo);

            // Create CFO
            User cfo = createUser("CFO User", "cfo@efile.com", "cfo123", UserRole.CFO, finance != null ? finance : createDepartment("Finance"));
            userRepository.save(cfo);

            // Create other users
            User auditor = createUser("Auditor User", "auditor@efile.com", "audit123", UserRole.AUDITOR, executive != null ? executive : createDepartment("Executive"));
            userRepository.save(auditor);

            User procurement = createUser("Procurement User", "procurement@efile.com", "proc123", UserRole.PROCUREMENT, finance != null ? finance : createDepartment("Finance"));
            userRepository.save(procurement);

            User accountant = createUser("Accountant User", "accountant@efile.com", "acc123", UserRole.ACCOUNTANT, finance != null ? finance : createDepartment("Finance"));
            userRepository.save(accountant);

            User itUser = createUser("IT User", "it@efile.com", "it123", UserRole.IT, it != null ? it : createDepartment("IT"));
            userRepository.save(itUser);

            User investor = createUser("Investor User", "investor@efile.com", "inv123", UserRole.INVESTOR, executive != null ? executive : createDepartment("Executive"));
            userRepository.save(investor);

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

    @NonNull
    private Department createDepartment(String name) {
        Department dept = new Department();
        dept.setName(name);
        return dept;
    }

    @NonNull
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
            User cfo = userRepository.findByEmailIgnoreCase("cfo@efile.com").orElse(null);
            User auditor = userRepository.findByEmailIgnoreCase("auditor@efile.com").orElse(null);
            User procurement = userRepository.findByEmailIgnoreCase("procurement@efile.com").orElse(null);
            User accountant = userRepository.findByEmailIgnoreCase("accountant@efile.com").orElse(null);
            User it = userRepository.findByEmailIgnoreCase("it@efile.com").orElse(null);
            User investor = userRepository.findByEmailIgnoreCase("investor@efile.com").orElse(null);

            if (admin != null && ceo != null && cfo != null) {
                // Case 1: High Priority Legal Contract
                Case case1 = new Case();
                case1.setTitle("Q1 2024 Vendor Contract Review");
                case1.setDescription("Comprehensive review and approval of Q1 2024 vendor contracts for procurement department including terms, conditions, and compliance requirements");
                case1.setStatus(CaseStatus.OPEN);
                case1.setPriority(CasePriority.HIGH);
                case1.setCategory(CaseCategory.LEGAL);
                case1.setTags("[\"contracts\", \"procurement\", \"Q1-2024\", \"legal-review\"]");
                case1.setBudget(75000.0);
                case1.setLocation("Headquarters - Legal Department");
                case1.setDepartment("Legal");
                case1.setCreatedBy(admin);
                case1.setAssignedTo(ceo);
                caseRepository.save(case1);

                // Case 2: Urgent Financial Budget
                Case case2 = new Case();
                case2.setTitle("Annual Budget Allocation 2024");
                case2.setDescription("Critical annual budget allocation and strategic planning for Finance department operations including quarterly forecasts and investment strategies");
                case2.setStatus(CaseStatus.ACTIVE);
                case2.setPriority(CasePriority.URGENT);
                case2.setCategory(CaseCategory.FINANCIAL);
                case2.setTags("[\"budget\", \"finance\", \"annual-planning\", \"2024\"]");
                case2.setBudget(2500000.0);
                case2.setLocation("Finance Office - Main Building");
                case2.setDepartment("Finance");
                case2.setCreatedBy(admin);
                case2.setAssignedTo(cfo);
                caseRepository.save(case2);

                // Case 3: Medium Priority IT Infrastructure
                Case case3 = new Case();
                case3.setTitle("IT Infrastructure Upgrade Project");
                case3.setDescription("System-wide infrastructure upgrade including servers, network equipment, and security enhancements for improved performance and reliability");
                case3.setStatus(CaseStatus.UNDER_REVIEW);
                case3.setPriority(CasePriority.MEDIUM);
                case3.setCategory(CaseCategory.OPERATIONS);
                case3.setTags("[\"IT\", \"infrastructure\", \"upgrade\", \"security\"]");
                case3.setBudget(150000.0);
                case3.setLocation("Data Center - Building A");
                case3.setDepartment("IT");
                case3.setCreatedBy(admin);
                case3.setAssignedTo(it);
                caseRepository.save(case3);

                // Case 4: Low Priority HR Policy
                Case case4 = new Case();
                case4.setTitle("Employee Handbook Update 2024");
                case4.setDescription("Annual review and update of employee handbook to reflect new policies, compliance requirements, and company culture initiatives");
                case4.setStatus(CaseStatus.ACTIVE);
                case4.setPriority(CasePriority.LOW);
                case4.setCategory(CaseCategory.HR);
                case4.setTags("[\"HR\", \"policies\", \"handbook\", \"compliance\"]");
                case4.setBudget(5000.0);
                case4.setLocation("HR Department - 3rd Floor");
                case4.setDepartment("Human Resources");
                case4.setCreatedBy(admin);
                case4.setAssignedTo(procurement);
                caseRepository.save(case4);

                // Case 5: High Priority Compliance Audit
                Case case5 = new Case();
                case5.setTitle("Q2 2024 Compliance Audit");
                case5.setDescription("Comprehensive compliance audit covering financial regulations, data privacy, and operational procedures for Q2 2024");
                case5.setStatus(CaseStatus.OPEN);
                case5.setPriority(CasePriority.HIGH);
                case5.setCategory(CaseCategory.COMPLIANCE);
                case5.setTags("[\"compliance\", \"audit\", \"Q2-2024\", \"regulations\"]");
                case5.setBudget(100000.0);
                case5.setLocation("Compliance Office - Suite 200");
                case5.setDepartment("Compliance");
                case5.setCreatedBy(admin);
                case5.setAssignedTo(auditor);
                caseRepository.save(case5);

                // Case 6: Medium Priority Strategic Planning
                Case case6 = new Case();
                case6.setTitle("Strategic Partnership Initiative");
                case6.setDescription("Evaluation and planning of strategic partnerships for business expansion and market penetration in new regions");
                case6.setStatus(CaseStatus.UNDER_REVIEW);
                case6.setPriority(CasePriority.MEDIUM);
                case6.setCategory(CaseCategory.STRATEGIC);
                case6.setTags("[\"strategy\", \"partnerships\", \"expansion\", \"growth\"]");
                case6.setBudget(300000.0);
                case6.setLocation("Executive Boardroom");
                case6.setDepartment("Executive");
                case6.setCreatedBy(admin);
                case6.setAssignedTo(investor);
                caseRepository.save(case6);

                // Case 7: General Administrative Task
                Case case7 = new Case();
                case7.setTitle("Office Supplies Procurement");
                case7.setDescription("Quarterly procurement of office supplies and equipment for all departments including stationery, furniture, and technology items");
                case7.setStatus(CaseStatus.ACTIVE);
                case7.setPriority(CasePriority.LOW);
                case7.setCategory(CaseCategory.GENERAL);
                case7.setTags("[\"procurement\", \"supplies\", \"administrative\"]");
                case7.setBudget(25000.0);
                case7.setLocation("Administrative Office");
                case7.setDepartment("Administration");
                case7.setCreatedBy(admin);
                case7.setAssignedTo(accountant);
                caseRepository.save(case7);

                // Case 8: Completed Case for Testing
                Case case8 = new Case();
                case8.setTitle("Security System Installation");
                case8.setDescription("Installation of new security system including cameras, access control, and monitoring equipment");
                case8.setStatus(CaseStatus.COMPLETED);
                case8.setPriority(CasePriority.MEDIUM);
                case8.setCategory(CaseCategory.OPERATIONS);
                case8.setTags("[\"security\", \"installation\", \"completed\"]");
                case8.setBudget(85000.0);
                case8.setLocation("All Facilities");
                case8.setDepartment("Security");
                case8.setCreatedBy(admin);
                case8.setAssignedTo(it);
                caseRepository.save(case8);

                System.out.println("✅ Seeded 8 comprehensive test cases with enhanced fields");
            }
        }
    }

    private void seedDocuments() {
        if (documentRepository.count() == 0) {
            User admin = userRepository.findByEmailIgnoreCase("admin@efile.com").orElse(null);
            List<Case> cases = caseRepository.findAll();

            if (admin != null && !cases.isEmpty()) {
                // Create documents for different cases and statuses
                Document doc1 = new Document();
                doc1.setTitle("Contract_Q1_2024.pdf");
                doc1.setType(DocumentType.LEGAL_DOCUMENT);
                doc1.setFilePath("/uploads/contracts/contract_q1_2024.pdf");
                doc1.setFileSize(1024000L);
                doc1.setStatus(DocumentStatus.DRAFT);
                doc1.setCaseRef(cases.get(0));
                doc1.setUploadedBy(admin);
                doc1.setReceiptNumber("REC-2024-001");
                documentRepository.save(doc1);

                Document doc2 = new Document();
                doc2.setTitle("Budget_Proposal.xlsx");
                doc2.setType(DocumentType.FINANCIAL_REPORT);
                doc2.setFilePath("/uploads/reports/budget_proposal.xlsx");
                doc2.setFileSize(512000L);
                doc2.setStatus(DocumentStatus.UNDER_REVIEW);
                doc2.setCaseRef(cases.get(0));
                doc2.setUploadedBy(admin);
                doc2.setReceiptNumber("REC-2024-002");
                documentRepository.save(doc2);

                // Add more documents
                Document doc3 = new Document();
                doc3.setTitle("HR_Policy_Manual.pdf");
                doc3.setType(DocumentType.GENERAL);
                doc3.setFilePath("/uploads/hr/hr_policy_manual.pdf");
                doc3.setFileSize(2048000L);
                doc3.setStatus(DocumentStatus.APPROVED);
                doc3.setCaseRef(cases.size() > 1 ? cases.get(1) : cases.get(0));
                doc3.setUploadedBy(admin);
                doc3.setReceiptNumber("REC-2024-003");
                documentRepository.save(doc3);

                Document doc4 = new Document();
                doc4.setTitle("Compliance_Report_Q2.pdf");
                doc4.setType(DocumentType.AUDIT_REPORT);
                doc4.setFilePath("/uploads/compliance/compliance_q2.pdf");
                doc4.setFileSize(1536000L);
                doc4.setStatus(DocumentStatus.SUBMITTED);
                doc4.setCaseRef(cases.size() > 2 ? cases.get(2) : cases.get(0));
                doc4.setUploadedBy(admin);
                doc4.setReceiptNumber("REC-2024-004");
                documentRepository.save(doc4);

                Document doc5 = new Document();
                doc5.setTitle("Procurement_Contract.pdf");
                doc5.setType(DocumentType.LEGAL_DOCUMENT);
                doc5.setFilePath("/uploads/procurement/procurement_contract.pdf");
                doc5.setFileSize(3072000L);
                doc5.setStatus(DocumentStatus.APPROVED);
                doc5.setCaseRef(cases.size() > 3 ? cases.get(3) : cases.get(0));
                doc5.setUploadedBy(admin);
                doc5.setReceiptNumber("REC-2024-005");
                documentRepository.save(doc5);

                Document doc6 = new Document();
                doc6.setTitle("Financial_Audit_2024.pdf");
                doc6.setType(DocumentType.AUDIT_REPORT);
                doc6.setFilePath("/uploads/audit/financial_audit_2024.pdf");
                doc6.setFileSize(4096000L);
                doc6.setStatus(DocumentStatus.UNDER_REVIEW);
                doc6.setCaseRef(cases.size() > 4 ? cases.get(4) : cases.get(0));
                doc6.setUploadedBy(admin);
                doc6.setReceiptNumber("REC-2024-006");
                documentRepository.save(doc6);

                Document doc7 = new Document();
                doc7.setTitle("Operations_Manual.docx");
                doc7.setType(DocumentType.GENERAL);
                doc7.setFilePath("/uploads/operations/operations_manual.docx");
                doc7.setFileSize(768000L);
                doc7.setStatus(DocumentStatus.DRAFT);
                doc7.setCaseRef(cases.size() > 5 ? cases.get(5) : cases.get(0));
                doc7.setUploadedBy(admin);
                doc7.setReceiptNumber("REC-2024-007");
                documentRepository.save(doc7);

                Document doc8 = new Document();
                doc8.setTitle("Tax_Return_2023.pdf");
                doc8.setType(DocumentType.FINANCIAL_REPORT);
                doc8.setFilePath("/uploads/tax/tax_return_2023.pdf");
                doc8.setFileSize(2560000L);
                doc8.setStatus(DocumentStatus.APPROVED);
                doc8.setCaseRef(cases.size() > 6 ? cases.get(6) : cases.get(0));
                doc8.setUploadedBy(admin);
                doc8.setReceiptNumber("REC-2024-008");
                documentRepository.save(doc8);

                System.out.println("✅ Seeded 8 test documents");
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
