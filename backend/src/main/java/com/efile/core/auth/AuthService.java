package com.efile.core.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.efile.core.auth.dto.LoginRequest;
import com.efile.core.auth.dto.RefreshTokenRequest;
import com.efile.core.auth.dto.RegisterUserRequest;
import com.efile.core.auth.dto.TokenResponse;
import com.efile.core.auth.dto.UserSummaryResponse;
import com.efile.core.department.Department;
import com.efile.core.department.DepartmentRepository;
import com.efile.core.security.JwtService;
import com.efile.core.security.UserDetailsServiceImpl;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        DepartmentRepository departmentRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        UserDetailsServiceImpl userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isActive()) {
            throw new BadCredentialsException("Account is inactive");
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        );
        authenticationManager.authenticate(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        return new TokenResponse(accessToken, refreshToken, jwtService.extractExpiration(accessToken));
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(request.refreshToken(), userDetails) || !jwtService.isRefreshToken(request.refreshToken())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        return new TokenResponse(accessToken, refreshToken, jwtService.extractExpiration(accessToken));
    }

    @Transactional
    public UserSummaryResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository
                .findById(request.departmentId())
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

    public void logout() {
    }
}
