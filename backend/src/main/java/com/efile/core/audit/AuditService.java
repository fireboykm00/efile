package com.efile.core.audit;

import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(
        AuditLogRepository auditLogRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public void logAction(String action, String entityType, Long entityId, Map<String, Object> details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(getClientIpAddress());
        auditLog.setTimestamp(Instant.now());

        // Set user if authenticated
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String email = authentication.getName();
                userRepository.findByEmailIgnoreCase(email).ifPresent(auditLog::setUser);
            }
        } catch (Exception e) {
            // Continue without user if there's an issue
        }

        // Convert details to JSON string
        try {
            if (details != null && !details.isEmpty()) {
                auditLog.setDetails(objectMapper.writeValueAsString(details));
            }
        } catch (Exception e) {
            auditLog.setDetails("{}");
        }

        auditLogRepository.save(auditLog);
    }

    public void logAction(String action, String entityType, Long entityId) {
        logAction(action, entityType, entityId, new HashMap<>());
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Instant startDate, Instant endDate, Long userId, String actionType, Pageable pageable) {
        if (startDate != null && endDate != null && userId != null && actionType != null) {
            return auditLogRepository.findByTimestampBetweenAndUserIdAndAction(startDate, endDate, userId, actionType, pageable);
        } else if (startDate != null && endDate != null && userId != null) {
            return auditLogRepository.findByTimestampBetweenAndUserId(startDate, endDate, userId, pageable);
        } else if (startDate != null && endDate != null && actionType != null) {
            return auditLogRepository.findByTimestampBetweenAndAction(startDate, endDate, actionType, pageable);
        } else if (startDate != null && endDate != null) {
            return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else if (userId != null) {
            return auditLogRepository.findByUserId(userId, pageable);
        } else if (actionType != null) {
            return auditLogRepository.findByAction(actionType, pageable);
        } else {
            return auditLogRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Return unknown if unable to get IP
        }
        return "unknown";
    }
}
