package vn.edu.smd.core.module.faculty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Department;
import vn.edu.smd.core.entity.Faculty;
import vn.edu.smd.core.module.department.dto.DepartmentResponse;
import vn.edu.smd.core.module.faculty.dto.FacultyRequest;
import vn.edu.smd.core.module.faculty.dto.FacultyResponse;
import vn.edu.smd.core.module.faculty.dto.FacultyStatisticsResponse;
import vn.edu.smd.core.repository.DepartmentRepository;
import vn.edu.smd.core.repository.FacultyRepository;
import vn.edu.smd.core.repository.SubjectRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;

    public Page<FacultyResponse> getAllFaculties(Pageable pageable) {
        return facultyRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<FacultyResponse> getAllFaculties() {
        return facultyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FacultyResponse getFacultyById(UUID id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
        return mapToResponse(faculty);
    }

    public FacultyResponse getFacultyByCode(String code) {
        Faculty faculty = facultyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "code", code));
        return mapToResponse(faculty);
    }

    @Transactional
    public FacultyResponse createFaculty(FacultyRequest request) {
        if (facultyRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Faculty code already exists");
        }

        Faculty faculty = Faculty.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();

        Faculty savedFaculty = facultyRepository.save(faculty);
        return mapToResponse(savedFaculty);
    }

    @Transactional
    public FacultyResponse updateFaculty(UUID id, FacultyRequest request) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));

        if (!faculty.getCode().equals(request.getCode()) 
                && facultyRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Faculty code already exists");
        }

        faculty.setCode(request.getCode());
        faculty.setName(request.getName());

        Faculty updatedFaculty = facultyRepository.save(faculty);
        return mapToResponse(updatedFaculty);
    }

    @Transactional
    public void deleteFaculty(UUID id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty", "id", id);
        }
        facultyRepository.deleteById(id);
    }

    public List<DepartmentResponse> getDepartmentsOfFaculty(UUID id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty", "id", id);
        }
        return departmentRepository.findByFacultyId(id).stream()
                .map(this::mapToDepartmentResponse)
                .collect(Collectors.toList());
    }

    public FacultyStatisticsResponse getFacultyStatistics(UUID id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty", "id", id);
        }
        
        List<Department> departments = departmentRepository.findByFacultyId(id);
        long departmentCount = departments.size();
        
        long subjectCount = departments.stream()
                .flatMap(dept -> subjectRepository.findByDepartmentId(dept.getId()).stream())
                .count();
        
        FacultyStatisticsResponse statistics = new FacultyStatisticsResponse();
        statistics.setDepartmentCount(departmentCount);
        statistics.setSubjectCount(subjectCount);
        return statistics;
    }

    private FacultyResponse mapToResponse(Faculty faculty) {
        FacultyResponse response = new FacultyResponse();
        response.setId(faculty.getId());
        response.setCode(faculty.getCode());
        response.setName(faculty.getName());
        response.setCreatedAt(faculty.getCreatedAt());
        response.setUpdatedAt(faculty.getUpdatedAt());
        return response;
    }

    private DepartmentResponse mapToDepartmentResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setFacultyId(department.getFaculty().getId());
        response.setFacultyCode(department.getFaculty().getCode());
        response.setFacultyName(department.getFaculty().getName());
        response.setCode(department.getCode());
        response.setName(department.getName());
        
        // Tìm HOD (Head of Department) - user có role HOD thuộc department này
        // Tạm thời để null, sẽ implement sau khi có UserRepository
        response.setHeadOfDepartmentName(null);
        
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }
}
