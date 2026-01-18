package vn.edu.smd.core.module.auditlog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.AuditLog;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.auditlog.dto.AuditLogResponse;
import vn.edu.smd.core.repository.AuditLogRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator; // 🔥 Import Comparator
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        // Repository sẽ tự động xử lý sort dựa trên Pageable từ Controller
        return auditLogRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return mapToResponse(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntity(String entityName, UUID entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByActor(UUID actorId) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByUser(UUID userId, Pageable pageable) {
        List<AuditLog> logs = auditLogRepository.findByActorIdOrderByCreatedAtDesc(userId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), logs.size());
        
        List<AuditLogResponse> pageContent = logs.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pageContent, pageable, logs.size());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchAuditLogs(String entityName, String action, 
                                                   String startDate, String endDate, 
                                                   Pageable pageable) {
        List<AuditLog> allLogs = auditLogRepository.findAll();
        
        // Filter by criteria
        List<AuditLog> filteredLogs = allLogs.stream()
                .filter(log -> entityName == null || entityName.isEmpty() || log.getEntityName().equals(entityName))
                .filter(log -> action == null || action.isEmpty() || log.getAction().equals(action))
                .filter(log -> {
                    if (startDate != null && !startDate.isEmpty()) {
                        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
                        return log.getCreatedAt().isAfter(start) || log.getCreatedAt().isEqual(start);
                    }
                    return true;
                })
                .filter(log -> {
                    if (endDate != null && !endDate.isEmpty()) {
                        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
                        return log.getCreatedAt().isBefore(end) || log.getCreatedAt().isEqual(end);
                    }
                    return true;
                })
                // 🔥 FIX: Sắp xếp giảm dần theo thời gian (Mới nhất lên đầu)
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
        
        // Handle case where offset is beyond list size
        if (start > filteredLogs.size()) {
             return new PageImpl<>(List.of(), pageable, filteredLogs.size());
        }

        List<AuditLogResponse> pageContent = filteredLogs.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pageContent, pageable, filteredLogs.size());
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(auditLog.getId());
        response.setEntityName(auditLog.getEntityName());
        response.setEntityId(auditLog.getEntityId());
        response.setAction(auditLog.getAction());
        response.setActorId(auditLog.getActorId());
        response.setDescription(auditLog.getDescription());
        response.setStatus(auditLog.getStatus());
        response.setOldValue(auditLog.getOldValue());
        response.setNewValue(auditLog.getNewValue());
        response.setIpAddress(auditLog.getIpAddress());
        response.setUserAgent(auditLog.getUserAgent());
        response.setCreatedAt(auditLog.getCreatedAt());
        
        if (auditLog.getActorId() != null) {
            try {
                Optional<User> actorOpt = userRepository.findByIdWithRoles(auditLog.getActorId());
                if (actorOpt.isPresent()) {
                    User actor = actorOpt.get();
                    response.setActorName(actor.getFullName());
                    response.setActorEmail(actor.getEmail());
                    
                    if (actor.getUserRoles() != null && !actor.getUserRoles().isEmpty()) {
                        var firstRole = actor.getUserRoles().iterator().next().getRole();
                        if (firstRole != null) {
                            response.setActorRole(firstRole.getCode());
                        } else {
                            response.setActorRole("UNKNOWN");
                        }
                    } else {
                        response.setActorRole("NO_ROLE");
                    }
                } else {
                    response.setActorName("Người dùng đã xóa");
                    response.setActorEmail("unknown@deleted.user");
                    response.setActorRole("DELETED");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to fetch user info for audit log: " + e.getMessage());
                response.setActorName("Lỗi tải user");
                response.setActorEmail("error@system");
                response.setActorRole("ERROR");
            }
        } else {
            response.setActorName("Hệ thống");
            response.setActorEmail("system@smd.edu.vn");
            response.setActorRole("SYSTEM");
        }
        
        return response;
    }
}