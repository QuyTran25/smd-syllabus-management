package vn.edu.smd.core.module.auditlog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.AuditLog;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.auditlog.dto.AuditLogRequest; // Import file DTO vừa tạo
import vn.edu.smd.core.module.auditlog.dto.AuditLogResponse;
import vn.edu.smd.core.repository.AuditLogRepository;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.AuditStatus; // Import Enum của bạn

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // =================================================================
    // 🔥 HÀM MỚI: XỬ LÝ GHI LOG TỪ FRONTEND (AN TOÀN TUYỆT ĐỐI)
    // =================================================================
    @Transactional
    public void createLog(AuditLogRequest request) {
        try {
            AuditLog log = new AuditLog();

            // 1. Map thông tin cơ bản
            log.setEntityName(request.getEntityType()); // VD: Syllabus
            log.setAction(request.getAction());         // VD: PUBLISH
            log.setCreatedAt(LocalDateTime.now());

            // 2. Xử lý Description (Cắt ngắn nếu quá dài để tránh lỗi DB)
            String desc = request.getDescription();
            if (desc != null && desc.length() > 2000) { // Giả sử cột TEXT chịu được nhiều, nhưng cứ an toàn
                desc = desc.substring(0, 1997) + "...";
            }
            log.setDescription(desc);

            // 3. Xử lý Entity ID (Chuyển String -> UUID an toàn)
            if (request.getEntityId() != null && !request.getEntityId().isEmpty() && !request.getEntityId().equals("N/A")) {
                try {
                    log.setEntityId(UUID.fromString(request.getEntityId()));
                } catch (IllegalArgumentException e) {
                    // Nếu React gửi ID rác không phải UUID, set null để không lỗi
                    System.err.println("⚠️ AuditLog: Invalid UUID format '" + request.getEntityId() + "'. Setting null.");
                    log.setEntityId(null);
                }
            }

            // 4. Xử lý Status (String -> Enum an toàn)
            try {
                if (request.getStatus() != null) {
                    log.setStatus(AuditStatus.valueOf(request.getStatus().toUpperCase()));
                } else {
                    log.setStatus(AuditStatus.SUCCESS);
                }
            } catch (Exception e) {
                // Nếu gửi sai Status, mặc định là SUCCESS
                System.err.println("⚠️ AuditLog: Invalid Status '" + request.getStatus() + "'. Defaulting to SUCCESS.");
                log.setStatus(AuditStatus.SUCCESS);
            }

            // 5. Lấy Actor (Người thực hiện)
            try {
                UUID currentUserId = getCurrentUserId();
                log.setActorId(currentUserId);
            } catch (Exception e) {
                log.setActorId(null); // Nếu lỗi lấy user, cho phép null
            }

            // 6. Lưu vào DB
            auditLogRepository.save(log);

        } catch (Exception e) {
            // Catch tất cả lỗi còn lại để API không bao giờ trả về 500
            System.err.println("❌ CRITICAL ERROR SAVING AUDIT LOG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================================================================
    // CÁC HÀM GET/SEARCH CŨ (GIỮ NGUYÊN LOGIC CỦA BẠN)
    // =================================================================

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
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
        
        if (start > logs.size()) {
             return new PageImpl<>(List.of(), pageable, logs.size());
        }

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
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
        
        if (start > filteredLogs.size()) {
             return new PageImpl<>(List.of(), pageable, filteredLogs.size());
        }

        List<AuditLogResponse> pageContent = filteredLogs.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pageContent, pageable, filteredLogs.size());
    }

    // --- HELPER METHODS ---

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        // Logic lấy User ID: Ưu tiên tìm theo username/email từ DB cho chính xác
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
             user = userRepository.findByEmail(username);
        }
        return user.map(User::getId).orElse(null);
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
                // Log lỗi nhẹ nhàng
                response.setActorName("Lỗi tải user");
                response.setActorEmail("error@system");
                response.setActorRole("ERROR");
            }
        } else {
            response.setActorName("Hệ thống / Ẩn danh");
            response.setActorEmail("system@smd.edu.vn");
            response.setActorRole("SYSTEM");
        }
        
        return response;
    }
}