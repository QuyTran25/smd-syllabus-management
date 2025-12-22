package vn.edu.smd.core.module.department.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Department;
import vn.edu.smd.core.entity.Faculty;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.department.dto.DepartmentRequest;
import vn.edu.smd.core.module.department.dto.DepartmentResponse;
import vn.edu.smd.core.module.subject.dto.SubjectResponse;
import vn.edu.smd.core.module.user.dto.UserResponse;
import vn.edu.smd.core.repository.DepartmentRepository;
import vn.edu.smd.core.repository.FacultyRepository;
import vn.edu.smd.core.repository.SubjectRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(department);
    }

    public DepartmentResponse getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));
        return mapToResponse(department);
    }

    public List<DepartmentResponse> getDepartmentsByFaculty(UUID facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty", "id", facultyId);
        }
        return departmentRepository.findByFacultyId(facultyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));

        if (departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Department code already exists");
        }

        Department department = Department.builder()
                .faculty(faculty)
                .code(request.getCode())
                .name(request.getName())
                .build();

        Department savedDepartment = departmentRepository.save(department);
        return mapToResponse(savedDepartment);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));

        if (!department.getCode().equals(request.getCode()) 
                && departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Department code already exists");
        }

        department.setFaculty(faculty);
        department.setCode(request.getCode());
        department.setName(request.getName());

        Department updatedDepartment = departmentRepository.save(department);
        return mapToResponse(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        departmentRepository.deleteById(id);
    }

    public List<SubjectResponse> getSubjectsOfDepartment(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        return subjectRepository.findByDepartmentId(id).stream()
                .map(this::mapToSubjectResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getLecturersOfDepartment(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        return userRepository.findByDepartmentId(id).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private DepartmentResponse mapToResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setFacultyId(department.getFaculty().getId());
        response.setFacultyCode(department.getFaculty().getCode());
        response.setFacultyName(department.getFaculty().getName());
        response.setCode(department.getCode());
        response.setName(department.getName());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }

    private SubjectResponse mapToSubjectResponse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setId(subject.getId());
        response.setCode(subject.getCode());
        response.setDepartmentId(subject.getDepartment().getId());
        response.setDepartmentCode(subject.getDepartment().getCode());
        response.setDepartmentName(subject.getDepartment().getName());
        if (subject.getCurriculum() != null) {
            response.setCurriculumId(subject.getCurriculum().getId());
            response.setCurriculumCode(subject.getCurriculum().getCode());
            response.setCurriculumName(subject.getCurriculum().getName());
        }
        response.setCurrentNameVi(subject.getCurrentNameVi());
        response.setCurrentNameEn(subject.getCurrentNameEn());
        response.setDefaultCredits(subject.getDefaultCredits());
        response.setIsActive(subject.getIsActive());
        response.setSubjectType(subject.getSubjectType());
        response.setComponent(subject.getComponent());
        response.setDefaultTheoryHours(subject.getDefaultTheoryHours());
        response.setDefaultPracticeHours(subject.getDefaultPracticeHours());
        response.setDefaultSelfStudyHours(subject.getDefaultSelfStudyHours());
        response.setDescription(subject.getDescription());
        response.setRecommendedTerm(subject.getRecommendedTerm());
        response.setCreatedAt(subject.getCreatedAt());
        response.setUpdatedAt(subject.getUpdatedAt());
        return response;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus().name());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
