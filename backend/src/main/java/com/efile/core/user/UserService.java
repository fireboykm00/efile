package com.efile.core.user;

import com.efile.core.department.Department;
import com.efile.core.department.DepartmentRepository;
import com.efile.core.user.dto.CreateUserRequest;
import com.efile.core.user.dto.UpdateProfileRequest;
import com.efile.core.user.dto.UpdateUserRequest;
import com.efile.core.user.dto.UserResponse;
import com.efile.core.user.dto.UserSummary;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.valueOf(request.role().toUpperCase()));
        user.setDepartment(resolveDepartment(request.departmentId()));
        user.setActive(request.active() == null || request.active());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.email())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.email());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            user.setRole(UserRole.valueOf(request.role().toUpperCase()));
        }
        if (request.departmentId() != null) {
            user.setDepartment(resolveDepartment(request.departmentId()));
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public UserResponse get(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    public List<UserResponse> getAll(Optional<UserRole> role) {
        List<User> users = role.map(userRepository::findByRole).orElseGet(userRepository::findAll);
        return users.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UserSummary> getByDepartment(Long departmentId) {
        return userRepository
            .findByDepartmentId(departmentId)
            .stream()
            .map(user -> new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole().name()))
            .collect(Collectors.toList());
    }

    public UserResponse getCurrent() {
        User user = getCurrentUserEntity();
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrent(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof com.efile.core.security.UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user");
        }
        return userRepository.findById(principal.getId()).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId).orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    private UserResponse toResponse(User user) {
        Department department = user.getDepartment();
        UserResponse.DepartmentSummary departmentSummary = department == null
            ? null
            : new UserResponse.DepartmentSummary(department.getId(), department.getName());
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.isActive(), departmentSummary);
    }
}
