package com.efile.core.department;

import com.efile.core.department.dto.DepartmentRequest;
import com.efile.core.department.dto.DepartmentResponse;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.dto.UserSummary;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Department name already exists");
        }
        Department department = new Department();
        department.setName(request.name());
        department.setHead(resolveHead(request.headId()));
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Department not found"));
        if (!department.getName().equalsIgnoreCase(request.name()) && departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Department name already exists");
        }
        department.setName(request.name());
        department.setHead(resolveHead(request.headId()));
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Department not found"));
        boolean hasUsers = !userRepository.findByDepartmentId(id).isEmpty();
        if (hasUsers) {
            throw new IllegalStateException("Department has assigned users");
        }
        departmentRepository.delete(department);
    }

    public DepartmentResponse get(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Department not found"));
        return toResponse(department);
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository
            .findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<UserSummary> getUsers(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Department not found"));
        return userRepository
            .findByDepartmentId(department.getId())
            .stream()
            .map(user -> new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole().name()))
            .collect(Collectors.toList());
    }

    private User resolveHead(Long headId) {
        if (headId == null) {
            return null;
        }
        return userRepository.findById(headId).orElseThrow(() -> new IllegalArgumentException("Department head not found"));
    }

    private DepartmentResponse toResponse(Department department) {
        User head = department.getHead();
        UserSummary headSummary = head == null ? null : new UserSummary(head.getId(), head.getName(), head.getEmail(), head.getRole().name());
        return new DepartmentResponse(department.getId(), department.getName(), headSummary, department.getCreatedAt(), department.getUpdatedAt());
    }
}
