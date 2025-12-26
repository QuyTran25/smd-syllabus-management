package vn.edu.smd.core.module.syllabus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.syllabus.dto.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.core.security.UserPrincipal;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final SyllabusVersionRepository syllabusVersionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicTermRepository academicTermRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings) {
        if (statusStrings != null && !statusStrings.isEmpty()) {
            String[] statusArray = statusStrings.toArray(new String[0]);
            return syllabusVersionRepository.findByStatusInWithPage(statusArray, pageable)
                    .map(this::mapToResponse);
        }
        return syllabusVersionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SyllabusResponse getSyllabusById(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        return mapToResponse(syllabus);
    }

    @Transactional
    public SyllabusResponse createSyllabus(SyllabusRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        AcademicTerm academicTerm = null;
        if (request.getAcademicTermId() != null) {
            academicTerm = academicTermRepository.findById(request.getAcademicTermId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", request.getAcademicTermId()));
        }

        User currentUser = getCurrentUser();

        SyllabusVersion syllabus = new SyllabusVersion();
        syllabus.setSubject(subject);
        syllabus.setAcademicTerm(academicTerm);
        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setStatus(SyllabusStatus.DRAFT);
        syllabus.setReviewDeadline(request.getReviewDeadline());
        syllabus.setEffectiveDate(request.getEffectiveDate());
        syllabus.setKeywords(request.getKeywords());
        syllabus.setContent(request.getContent());

        // Set snapshots
        syllabus.setSnapSubjectCode(subject.getCode());
        syllabus.setSnapSubjectNameVi(subject.getCurrentNameVi());
        syllabus.setSnapSubjectNameEn(subject.getCurrentNameEn());
        syllabus.setSnapCreditCount(subject.getDefaultCredits());

        syllabus.setCreatedBy(currentUser);
        syllabus.setUpdatedBy(currentUser);

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(savedSyllabus);
    }

    @Transactional
    public SyllabusResponse updateSyllabus(UUID id, SyllabusRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be updated");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        AcademicTerm academicTerm = null;
        if (request.getAcademicTermId() != null) {
            academicTerm = academicTermRepository.findById(request.getAcademicTermId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", request.getAcademicTermId()));
        }

        syllabus.setSubject(subject);
        syllabus.setAcademicTerm(academicTerm);
        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setReviewDeadline(request.getReviewDeadline());
        syllabus.setEffectiveDate(request.getEffectiveDate());
        syllabus.setKeywords(request.getKeywords());
        syllabus.setContent(request.getContent());

        // Update snapshots
        syllabus.setSnapSubjectCode(subject.getCode());
        syllabus.setSnapSubjectNameVi(subject.getCurrentNameVi());
        syllabus.setSnapSubjectNameEn(subject.getCurrentNameEn());
        syllabus.setSnapCreditCount(subject.getDefaultCredits());

        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public void deleteSyllabus(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be deleted");
        }

        syllabusVersionRepository.deleteById(id);
    }

    @Transactional
    public SyllabusResponse submitSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be submitted");
        }

        syllabus.setStatus(SyllabusStatus.PENDING_HOD);
        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public SyllabusResponse approveSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // Simple approval logic - move to next status
        SyllabusStatus currentStatus = syllabus.getStatus();
        SyllabusStatus nextStatus;

        switch (currentStatus) {
            case PENDING_HOD:
                nextStatus = SyllabusStatus.PENDING_AA;
                break;
            case PENDING_AA:
                nextStatus = SyllabusStatus.PENDING_PRINCIPAL;
                break;
            case PENDING_PRINCIPAL:
                nextStatus = SyllabusStatus.APPROVED;
                break;
            case APPROVED:
                nextStatus = SyllabusStatus.PUBLISHED;
                break;
            default:
                throw new BadRequestException("Cannot approve syllabus in status: " + currentStatus);
        }

        syllabus.setStatus(nextStatus);
        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public SyllabusResponse rejectSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() == SyllabusStatus.DRAFT || syllabus.getStatus() == SyllabusStatus.REJECTED) {
            throw new BadRequestException("Cannot reject syllabus in status: " + syllabus.getStatus());
        }

        syllabus.setStatus(SyllabusStatus.REJECTED);
        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public SyllabusResponse cloneSyllabus(UUID id) {
        SyllabusVersion originalSyllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        User currentUser = getCurrentUser();

        SyllabusVersion newSyllabus = new SyllabusVersion();
        newSyllabus.setSubject(originalSyllabus.getSubject());
        newSyllabus.setAcademicTerm(originalSyllabus.getAcademicTerm());
        newSyllabus.setVersionNo(generateNextVersionNo(originalSyllabus.getVersionNo()));
        newSyllabus.setStatus(SyllabusStatus.DRAFT);
        newSyllabus.setPreviousVersion(originalSyllabus);
        newSyllabus.setReviewDeadline(originalSyllabus.getReviewDeadline());
        newSyllabus.setEffectiveDate(originalSyllabus.getEffectiveDate());
        newSyllabus.setKeywords(originalSyllabus.getKeywords());
        newSyllabus.setContent(originalSyllabus.getContent());

        // Copy snapshots
        newSyllabus.setSnapSubjectCode(originalSyllabus.getSnapSubjectCode());
        newSyllabus.setSnapSubjectNameVi(originalSyllabus.getSnapSubjectNameVi());
        newSyllabus.setSnapSubjectNameEn(originalSyllabus.getSnapSubjectNameEn());
        newSyllabus.setSnapCreditCount(originalSyllabus.getSnapCreditCount());

        newSyllabus.setCreatedBy(currentUser);
        newSyllabus.setUpdatedBy(currentUser);

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(newSyllabus);
        return mapToResponse(savedSyllabus);
    }

    public List<SyllabusResponse> getSyllabusVersions(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        List<SyllabusVersion> versions = new ArrayList<>();
        SyllabusVersion current = syllabus;

        // Get all versions by following previousVersion links
        while (current != null) {
            versions.add(current);
            current = current.getPreviousVersion();
        }

        Collections.reverse(versions);
        return versions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public SyllabusCompareResponse compareSyllabi(UUID id1, UUID id2) {
        SyllabusVersion syllabus1 = syllabusVersionRepository.findById(id1)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id1));
        SyllabusVersion syllabus2 = syllabusVersionRepository.findById(id2)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id2));

        SyllabusCompareResponse response = new SyllabusCompareResponse();
        response.setSyllabusA(mapToResponse(syllabus1));
        response.setSyllabusB(mapToResponse(syllabus2));
        response.setDifferences(calculateDifferences(syllabus1, syllabus2));

        return response;
    }

    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        return syllabusVersionRepository.findBySubject(subject).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public byte[] exportSyllabusToPdf(UUID id) {
        syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // TODO: Implement PDF export logic
        throw new BadRequestException("PDF export not implemented yet");
    }

    private SyllabusResponse mapToResponse(SyllabusVersion syllabus) {
        SyllabusResponse response = new SyllabusResponse();
        response.setId(syllabus.getId());
        response.setSubjectId(syllabus.getSubject().getId());
        response.setSubjectCode(syllabus.getSnapSubjectCode());
        response.setSubjectNameVi(syllabus.getSnapSubjectNameVi());
        response.setSubjectNameEn(syllabus.getSnapSubjectNameEn());
        response.setCreditCount(syllabus.getSnapCreditCount());

        if (syllabus.getAcademicTerm() != null) {
            response.setAcademicTermId(syllabus.getAcademicTerm().getId());
            response.setAcademicTermCode(syllabus.getAcademicTerm().getCode());
        }

        response.setVersionNo(syllabus.getVersionNo());
        response.setStatus(syllabus.getStatus().name());
        
        if (syllabus.getPreviousVersion() != null) {
            response.setPreviousVersionId(syllabus.getPreviousVersion().getId());
        }

        response.setReviewDeadline(syllabus.getReviewDeadline());
        response.setEffectiveDate(syllabus.getEffectiveDate());
        response.setKeywords(syllabus.getKeywords());
        response.setContent(syllabus.getContent());

        // Owner and department info
        if (syllabus.getCreatedBy() != null) {
            response.setCreatedBy(syllabus.getCreatedBy().getId());
            response.setOwnerName(syllabus.getCreatedBy().getFullName());
        }
        if (syllabus.getUpdatedBy() != null) {
            response.setUpdatedBy(syllabus.getUpdatedBy().getId());
        }
        
        // Department from subject
        if (syllabus.getSubject() != null && syllabus.getSubject().getDepartment() != null) {
            response.setDepartment(syllabus.getSubject().getDepartment().getName());
        }
        
        // Semester from academic term
        if (syllabus.getAcademicTerm() != null) {
            response.setSemester(syllabus.getAcademicTerm().getName());
        }
        
        // Approval workflow tracking
        response.setSubmittedAt(syllabus.getSubmittedAt());
        response.setHodApprovedAt(syllabus.getHodApprovedAt());
        if (syllabus.getHodApprovedBy() != null) {
            response.setHodApprovedByName(syllabus.getHodApprovedBy().getFullName());
        }
        
        response.setAaApprovedAt(syllabus.getAaApprovedAt());
        if (syllabus.getAaApprovedBy() != null) {
            response.setAaApprovedByName(syllabus.getAaApprovedBy().getFullName());
        }
        
        response.setPrincipalApprovedAt(syllabus.getPrincipalApprovedAt());
        if (syllabus.getPrincipalApprovedBy() != null) {
            response.setPrincipalApprovedByName(syllabus.getPrincipalApprovedBy().getFullName());
        }

        response.setCreatedAt(syllabus.getCreatedAt());
        response.setUpdatedAt(syllabus.getUpdatedAt());

        return response;
    }

    private List<SyllabusCompareResponse.FieldDifference> calculateDifferences(SyllabusVersion s1, SyllabusVersion s2) {
        List<SyllabusCompareResponse.FieldDifference> differences = new ArrayList<>();

        addDifference(differences, "versionNo", s1.getVersionNo(), s2.getVersionNo());
        addDifference(differences, "status", s1.getStatus(), s2.getStatus());
        addDifference(differences, "creditCount", s1.getSnapCreditCount(), s2.getSnapCreditCount());
        addDifference(differences, "effectiveDate", s1.getEffectiveDate(), s2.getEffectiveDate());

        return differences;
    }

    private void addDifference(List<SyllabusCompareResponse.FieldDifference> differences, String fieldName, Object value1, Object value2) {
        SyllabusCompareResponse.FieldDifference diff = new SyllabusCompareResponse.FieldDifference();
        diff.setFieldName(fieldName);
        diff.setValueA(value1);
        diff.setValueB(value2);
        diff.setDifferent(!Objects.equals(value1, value2));
        differences.add(diff);
    }

    private String generateNextVersionNo(String currentVersion) {
        // Simple version increment logic: v1.0 -> v1.1, v1.9 -> v2.0
        try {
            String[] parts = currentVersion.replace("v", "").split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            minor++;
            if (minor >= 10) {
                major++;
                minor = 0;
            }
            return String.format("v%d.%d", major, minor);
        } catch (Exception e) {
            return currentVersion + "_new";
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
