package vn.edu.smd.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.repository.NotificationRepository;
import vn.edu.smd.shared.enums.NotificationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Revision Notification Service
 * Service for creating and sending notifications for revision workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevisionNotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Send notification when admin enables edit for revision
     */
    @Transactional
    public void notifyLecturerRevisionRequested(
            RevisionSession session,
            User lecturer,
            int feedbackCount
    ) {
        SyllabusVersion syllabus = session.getSyllabusVersion();
        
        String title = String.format("[Yêu cầu chỉnh sửa] Đề cương %s - %s",
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
        
        String message = String.format(
                "Admin đã phát hiện %d lỗi cần chỉnh sửa trong đề cương của bạn. " +
                "Vui lòng xem chi tiết và cập nhật.",
                feedbackCount
        );
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("syllabusId", syllabus.getId().toString());
        payload.put("syllabusCode", syllabus.getSnapSubjectCode());
        payload.put("revisionSessionId", session.getId().toString());
        payload.put("feedbackCount", feedbackCount);
        payload.put("actionUrl", "/lecturer/syllabi/" + syllabus.getId() + "/edit");
        payload.put("actionLabel", "Chỉnh sửa ngay");
        payload.put("priority", "HIGH");
        
        Notification notification = Notification.builder()
                .user(lecturer)
                .title(title)
                .message(message)
                .type(NotificationType.ERROR_REPORT.name())
                .payload(payload)
                .isRead(false)
                .relatedEntityType("SYLLABUS_VERSION")
                .relatedEntityId(syllabus.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Sent revision request notification to lecturer: {} for syllabus: {}",
                lecturer.getFullName(), syllabus.getSnapSubjectCode());
    }

    /**
     * Send notification to HoD when lecturer submits revision
     */
    @Transactional
    public void notifyHodRevisionSubmitted(
            RevisionSession session,
            User hod,
            User lecturer,
            int feedbackCount
    ) {
        SyllabusVersion syllabus = session.getSyllabusVersion();
        
        String title = String.format("[Chờ duyệt] Đề cương đã chỉnh sửa: %s - %s",
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
        
        String message = String.format(
                "Giảng viên %s đã hoàn thành chỉnh sửa đề cương dựa trên %d phản hồi. " +
                "Vui lòng xem xét và phê duyệt.",
                lecturer.getFullName(),
                feedbackCount
        );
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("syllabusId", syllabus.getId().toString());
        payload.put("syllabusCode", syllabus.getSnapSubjectCode());
        payload.put("revisionSessionId", session.getId().toString());
        payload.put("lecturerId", lecturer.getId().toString());
        payload.put("lecturerName", lecturer.getFullName());
        payload.put("feedbackCount", feedbackCount);
        payload.put("actionUrl", "/hod/approvals/" + syllabus.getId());
        payload.put("actionLabel", "Xem và duyệt");
        payload.put("priority", "HIGH");
        
        Notification notification = Notification.builder()
                .user(hod)
                .title(title)
                .message(message)
                .type(NotificationType.APPROVAL.name())
                .payload(payload)
                .isRead(false)
                .relatedEntityType("SYLLABUS_VERSION")
                .relatedEntityId(syllabus.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Sent revision approval notification to HoD: {} for syllabus: {}",
                hod.getFullName(), syllabus.getSnapSubjectCode());
    }

    /**
     * Send notification to admin when HoD approves revision
     */
    @Transactional
    public void notifyAdminRevisionApproved(
            RevisionSession session,
            User admin,
            User hod,
            int feedbackCount
    ) {
        SyllabusVersion syllabus = session.getSyllabusVersion();
        
        String title = String.format("[Chờ xuất bản lại] Đề cương %s đã được TBM duyệt",
                syllabus.getSnapSubjectCode());
        
        String message = String.format(
                "Trưởng bộ môn %s đã phê duyệt phiên bản chỉnh sửa. " +
                "Vui lòng xuất bản lại để sinh viên xem.",
                hod.getFullName()
        );
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("syllabusId", syllabus.getId().toString());
        payload.put("syllabusCode", syllabus.getSnapSubjectCode());
        payload.put("revisionSessionId", session.getId().toString());
        payload.put("approvedBy", hod.getFullName());
        payload.put("feedbackCount", feedbackCount);
        payload.put("actionUrl", "/admin/syllabi/" + syllabus.getId() + "/republish");
        payload.put("actionLabel", "Xuất bản ngay");
        payload.put("priority", "MEDIUM");
        
        Notification notification = Notification.builder()
                .user(admin)
                .title(title)
                .message(message)
                .type(NotificationType.PUBLICATION.name())
                .payload(payload)
                .isRead(false)
                .relatedEntityType("SYLLABUS_VERSION")
                .relatedEntityId(syllabus.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Sent republish notification to admin: {} for syllabus: {}",
                admin.getFullName(), syllabus.getSnapSubjectCode());
    }

    /**
     * Send notification to students when syllabus is republished
     */
    @Transactional
    public void notifyStudentsSyllabusUpdated(
            SyllabusVersion syllabus,
            List<User> students,
            String changesSummary,
            int feedbackResolvedCount
    ) {
        String title = String.format("[Cập nhật] Đề cương %s - %s đã được cập nhật",
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
        
        String message = String.format(
                "Đề cương môn học đã được cập nhật dựa trên phản hồi của sinh viên. " +
                "Các thay đổi chính: %s",
                changesSummary
        );
        
        for (User student : students) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("syllabusCode", syllabus.getSnapSubjectCode());
            payload.put("newVersionNo", syllabus.getVersionNo());
            payload.put("changesSummary", changesSummary);
            payload.put("feedbackResolvedCount", feedbackResolvedCount);
            payload.put("actionUrl", "/student/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem ngay");
            payload.put("priority", "MEDIUM");
            
            Notification notification = Notification.builder()
                    .user(student)
                    .title(title)
                    .message(message)
                    .type(NotificationType.PUBLICATION.name())
                    .payload(payload)
                    .isRead(false)
                    .relatedEntityType("SYLLABUS_VERSION")
                    .relatedEntityId(syllabus.getId())
                    .build();
            
            notificationRepository.save(notification);
        }
        
        log.info("Sent syllabus update notifications to {} students for syllabus: {}",
                students.size(), syllabus.getSnapSubjectCode());
    }

    /**
     * Send notification to lecturer when HoD rejects revision
     */
    @Transactional
    public void notifyLecturerRevisionRejected(
            RevisionSession session,
            User lecturer,
            User hod,
            String rejectionReason
    ) {
        SyllabusVersion syllabus = session.getSyllabusVersion();
        
        String title = String.format("[Từ chối] Đề cương %s cần chỉnh sửa lại",
                syllabus.getSnapSubjectCode());
        
        String message = String.format(
                "Trưởng bộ môn %s đã từ chối phiên bản chỉnh sửa. Lý do: %s. " +
                "Vui lòng xem xét và cập nhật lại.",
                hod.getFullName(),
                rejectionReason
        );
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("syllabusId", syllabus.getId().toString());
        payload.put("syllabusCode", syllabus.getSnapSubjectCode());
        payload.put("revisionSessionId", session.getId().toString());
        payload.put("rejectedBy", hod.getFullName());
        payload.put("rejectionReason", rejectionReason);
        payload.put("actionUrl", "/lecturer/syllabi/" + syllabus.getId() + "/edit");
        payload.put("actionLabel", "Chỉnh sửa lại");
        payload.put("priority", "HIGH");
        
        Notification notification = Notification.builder()
                .user(lecturer)
                .title(title)
                .message(message)
                .type(NotificationType.APPROVAL.name())
                .payload(payload)
                .isRead(false)
                .relatedEntityType("SYLLABUS_VERSION")
                .relatedEntityId(syllabus.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Sent rejection notification to lecturer: {} for syllabus: {}",
                lecturer.getFullName(), syllabus.getSnapSubjectCode());
    }
}
