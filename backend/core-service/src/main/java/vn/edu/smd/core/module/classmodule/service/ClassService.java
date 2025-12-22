package vn.edu.smd.core.module.classmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.ClassEntity;
import vn.edu.smd.core.entity.Semester;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.classmodule.dto.ClassRequest;
import vn.edu.smd.core.module.classmodule.dto.ClassResponse;
import vn.edu.smd.core.repository.ClassRepository;
import vn.edu.smd.core.repository.SemesterRepository;
import vn.edu.smd.core.repository.SubjectRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassService {

    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;

    public Page<ClassResponse> getAllClasses(Pageable pageable) {
        return classRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public ClassResponse getClassById(UUID id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));
        return mapToResponse(classEntity);
    }

    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        if (classRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Class with code " + request.getCode() + " already exists");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        ClassEntity classEntity = ClassEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .subject(subject)
                .maxStudents(request.getMaxStudents())
                .schedule(request.getSchedule())
                .room(request.getRoom())
                .status(request.getStatus() != null ? request.getStatus() : "OPEN")
                .build();

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
            classEntity.setSemester(semester);
        }

        if (request.getLecturerId() != null) {
            User lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
            classEntity.setLecturer(lecturer);
        }

        classEntity = classRepository.save(classEntity);
        return mapToResponse(classEntity);
    }

    @Transactional
    public ClassResponse updateClass(UUID id, ClassRequest request) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        if (!classEntity.getCode().equals(request.getCode()) && 
            classRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Class with code " + request.getCode() + " already exists");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        classEntity.setCode(request.getCode());
        classEntity.setName(request.getName());
        classEntity.setSubject(subject);
        classEntity.setMaxStudents(request.getMaxStudents());
        classEntity.setSchedule(request.getSchedule());
        classEntity.setRoom(request.getRoom());
        classEntity.setStatus(request.getStatus());

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
            classEntity.setSemester(semester);
        }

        if (request.getLecturerId() != null) {
            User lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
            classEntity.setLecturer(lecturer);
        }

        classEntity = classRepository.save(classEntity);
        return mapToResponse(classEntity);
    }

    @Transactional
    public void deleteClass(UUID id) {
        if (!classRepository.existsById(id)) {
            throw new ResourceNotFoundException("Class not found with id: " + id);
        }
        classRepository.deleteById(id);
    }

    public List<UUID> getStudentsInClass(UUID classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        return classEntity.getStudents().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addStudentToClass(UUID classId, UUID studentId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (classEntity.getMaxStudents() != null && 
            classEntity.getStudents().size() >= classEntity.getMaxStudents()) {
            throw new BadRequestException("Class is full");
        }

        classEntity.getStudents().add(student);
        classRepository.save(classEntity);
    }

    @Transactional
    public void removeStudentFromClass(UUID classId, UUID studentId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        classEntity.getStudents().remove(student);
        classRepository.save(classEntity);
    }

    private ClassResponse mapToResponse(ClassEntity classEntity) {
        return ClassResponse.builder()
                .id(classEntity.getId())
                .code(classEntity.getCode())
                .name(classEntity.getName())
                .subjectId(classEntity.getSubject().getId())
                .subjectCode(classEntity.getSubject().getCode())
                .subjectName(classEntity.getSubject().getName())
                .semesterId(classEntity.getSemester() != null ? classEntity.getSemester().getId() : null)
                .semesterName(classEntity.getSemester() != null ? classEntity.getSemester().getName() : null)
                .lecturerId(classEntity.getLecturer() != null ? classEntity.getLecturer().getId() : null)
                .lecturerName(classEntity.getLecturer() != null ? classEntity.getLecturer().getEmail() : null)
                .maxStudents(classEntity.getMaxStudents())
                .currentStudents(classEntity.getStudents().size())
                .schedule(classEntity.getSchedule())
                .room(classEntity.getRoom())
                .status(classEntity.getStatus())
                .createdAt(classEntity.getCreatedAt())
                .updatedAt(classEntity.getUpdatedAt())
                .build();
    }
}
