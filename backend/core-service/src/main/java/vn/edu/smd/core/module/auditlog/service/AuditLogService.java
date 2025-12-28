package vn.edu.smd.core.module.auditlog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.AuditLog;
import vn.edu.smd.core.module.auditlog.dto.AuditLogResponse;
import vn.edu.smd.core.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::mapToResponse);
    }

    public AuditLogResponse getAuditLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return mapToResponse(auditLog);
    }

    public List<AuditLogResponse> getAuditLogsByEntity(String entityName, UUID entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getAuditLogsByActor(UUID actorId) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<AuditLogResponse> getAuditLogsByUser(UUID userId, Pageable pageable) {
        List<AuditLog> logs = auditLogRepository.findByActorIdOrderByCreatedAtDesc(userId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), logs.size());
        
        List<AuditLogResponse> pageContent = logs.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pageContent, pageable, logs.size());
    }

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
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
        
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
        return response;
    }
}
