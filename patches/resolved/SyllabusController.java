package vn.edu.smd.core.module.syllabus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import vn.edu.smd.core.entity.AcademicTerm;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.AcademicTermRepository;
import vn.edu.smd.core.repository.SubjectRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/syllabus")
public class SyllabusController {

    @Autowired
    private SyllabusVersionRepository syllabusVersionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademicTermRepository academicTermRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createSyllabus(@RequestBody Map<String, Object> payload) {
        try {
            SyllabusVersion syllabus = new SyllabusVersion();

            // 1. Map Subject & Snapshot Data
            String subjectIdStr = (String) payload.get("subjectId");
            if (subjectIdStr != null) {
                Subject subject = subjectRepository.findById(UUID.fromString(subjectIdStr))
                        .orElseThrow(() -> new RuntimeException("Subject not found"));
                syllabus.setSubject(subject);
                
                syllabus.setSnapSubjectCode(subject.getCode());
                syllabus.setSnapSubjectNameVi(subject.getCurrentNameVi());
                syllabus.setSnapSubjectNameEn(subject.getCurrentNameEn());
                syllabus.setSnapCreditCount(subject.getDefaultCredits()); 
            }

            // 2. Map Academic Term
            String semesterId = (String) payload.get("semesterId");
            if (semesterId != null) {
                AcademicTerm term = academicTermRepository.findById(UUID.fromString(semesterId)).orElse(null);
                syllabus.setAcademicTerm(term);
            }

            // 3. Map Creator
            User currentUser = getCurrentUser();
            syllabus.setCreatedBy(currentUser);

            // 4. Map Basic Fields
            syllabus.setVersionNo("1.0.0");
            syllabus.setDescription((String) payload.get("description"));
            syllabus.setObjectives((String) payload.get("objectives"));
            syllabus.setStudentTasks((String) payload.get("studentDuties"));

            // 5. Xử lý Time Allocation
            Object timeAllocObj = payload.get("timeAllocation");
            if (timeAllocObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> timeAlloc = (Map<String, Object>) timeAllocObj;
                
                syllabus.setTheoryHours(parseIntSafely(timeAlloc.get("theory")));
                syllabus.setPracticeHours(parseIntSafely(timeAlloc.get("practice")));
                syllabus.setSelfStudyHours(parseIntSafely(timeAlloc.get("selfStudy")));
            }

            // 6. Map Status
            String statusStr = (String) payload.get("status");
            try {
                syllabus.setStatus(SyllabusStatus.valueOf(statusStr));
            } catch (Exception e) {
                syllabus.setStatus(SyllabusStatus.DRAFT);
            }

            // 7. Lưu Content JSON & Cờ
            syllabus.setContent(payload);
            syllabus.setIsEditEnabled(true);
            syllabus.setIsDeleted(false);
            
            syllabus.setCreatedAt(LocalDateTime.now());
            syllabus.setUpdatedAt(LocalDateTime.now());

            // Lưu DB
            syllabusVersionRepository.save(syllabus);

            return ResponseEntity.ok().body("Lưu thành công Syllabus ID: " + syllabus.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi Backend: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = null;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                email = (String) principal;
            }

            if (email != null) {
                // FIX: Dùng biến finalEmail để truyền vào lambda
                String finalEmail = email;
                return userRepository.findByEmail(finalEmail)
                        .orElseThrow(() -> new RuntimeException("User not found: " + finalEmail));
            }
        } catch (Exception e) {
            // Ignore
        }

        return userRepository.findByEmail("lecturer@smd.edu.vn")
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("DB trống! Hãy chạy lệnh SQL tạo User trước.")));
    }

    private Integer parseIntSafely(Object obj) {
        if (obj == null) return 0;
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

/*
Origin/main variant (kept as reference):

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.syllabus.dto.*;
import vn.edu.smd.core.module.syllabus.service.SyllabusService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Syllabus Management", description = "Syllabus version management APIs")
@RestController
@RequestMapping("/api/syllabi")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;

    @Operation(summary = "Get all syllabi", description = "Get list of syllabi with pagination and filtering")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SyllabusResponse>>> getAllSyllabi(
            Pageable pageable,
            @RequestParam(required = false) List<String> status) {
        Page<SyllabusResponse> syllabi = syllabusService.getAllSyllabi(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(syllabi)));
    }

    // ... (rest of origin/main controller)
}
*/
