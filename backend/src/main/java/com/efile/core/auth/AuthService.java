package com.efile.core.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.efile.core.auth.dto.LoginRequest;
import com.efile.core.auth.dto.LoginResponse;
import com.efile.core.auth.dto.RegisterUserRequest;
import com.efile.core.auth.dto.UserSummaryResponse;
import com.efile.core.department.Department;
import com.efile.core.department.DepartmentRepository;
import com.efile.core.security.JwtService;
import com.efile.core.security.UserPrincipal;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        DepartmentRepository departmentRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            logger.info("Login attempt for email: {}", request.email());

            User user = userRepository
                .findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", request.email());
                    return new BadCredentialsException("Invalid credentials");
                });

            if (!user.isActive()) {
                logger.warn("Account inactive: {}", request.email());
                throw new BadCredentialsException("Account is inactive");
            }

            // Authenticate user
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
            authenticationManager.authenticate(authentication);

            logger.info("User authenticated successfully: {}", request.email());

            // Generate JWT token using UserPrincipal
            UserPrincipal userPrincipal = UserPrincipal.from(user);
            String token = jwtService.generateAccessToken(userPrincipal);

            return new LoginResponse(
                true,
                "Login successful",
                LoginResponse.UserData.from(user),
                token
            );
        } catch (BadCredentialsException e) {
            logger.warn("Bad credentials: {}", e.getMessage());
            return new LoginResponse(false, "Invalid credentials", null, null);
        } catch (Exception e) {
            logger.error("Login failed", e);
            return new LoginResponse(false, "Login failed: " + e.getMessage(), null, null);
        }
    }

    public void logout() {
        // JWT tokens are stateless, so logout is handled on the client side
        // by removing the token from localStorage
    }

    @Transactional
    public UserSummaryResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository
                .findById(request.departmentId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }
        
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.valueOf(request.role().toUpperCase()));
        user.setDepartment(department);
        user.setActive(true);
        
        User saved = userRepository.save(user);
        return new UserSummaryResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }
}
