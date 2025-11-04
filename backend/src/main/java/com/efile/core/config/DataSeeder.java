package com.efile.core.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.efile.core.department.Department;
import com.efile.core.department.DepartmentRepository;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, 
                     DepartmentRepository departmentRepository,
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedDepartments();
        seedUsers();
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
}
