package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "syllabus_versions", schema = "core_service") // ⭐ Sửa đúng tên bảng
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Syllabus {
    @Id
    private UUID id;

    // Mapping đúng cột snapshot trong DB
    @Column(name = "snap_subject_code")
    private String code;

    @Column(name = "snap_subject_name_vi")
    private String nameVi;

    @Column(name = "snap_subject_name_en")
    private String nameEn;

    // Mapping thông qua ID (vì DB lưu ID)
    @Column(name = "academic_term_id")
    private UUID academicTermId;
    
    // Dùng @Formula để lấy tên Học kỳ mà không cần Join
    @Formula("(select t.name from core_service.academic_terms t where t.id = academic_term_id)")
    private String term;

    @Column(name = "snap_credit_count")
    private Integer credits;

    // Dùng @Formula để lấy tên Khoa/Ngành từ Subject -> Department -> Faculty
    @Formula("(select f.name from core_service.faculties f " +
            "join core_service.departments d on f.id = d.faculty_id " +
            "join core_service.subjects s on d.id = s.department_id " +
            "where s.id = subject_id)")
    private String faculty;

    // Lấy tên chương trình (nếu có)
    @Formula("(select c.name from core_service.curriculums c " +
            "join core_service.subjects s on c.id = s.curriculum_id " +
            "where s.id = subject_id)")
    private String program;

    // ⭐ Dùng @Formula để lấy tên Giảng viên từ bảng Users
    @Formula("(select u.full_name from core_service.users u where u.id = created_by)")
    private String lecturerName;

    @Formula("(select u.email from core_service.users u where u.id = created_by)")
    private String lecturerEmail;

    // Lấy mô tả từ bảng Subjects (vì V13 update vào đó)
    @Formula("(select s.description from core_service.subjects s where s.id = subject_id)")
    private String description;

    // Trong DB, teaching_methods có thể nằm trong JSONB 'content', 
    // nhưng để đơn giản ta lấy tạm 1 string mẫu hoặc null nếu chưa có cột riêng
    @Transient 
    private String teachingMethods;

    @Column(name = "theory_hours") 
    private Integer theoryHours;

    @Column(name = "practice_hours") 
    private Integer practiceHours;

    @Column(name = "self_study_hours") 
    private Integer selfStudyHours;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "status")
    private String status;

    // Relationships (Giữ nguyên nếu entity SyllabusCLO đã map đúng)
    // Lưu ý: SyllabusCLO phải map @JoinColumn(name="syllabus_version_id")
    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY)
    private List<SyllabusCLO> clos;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY)
    private List<AssessmentMatrix> assessmentMatrix;

    // ⭐ SỬA QUAN TRỌNG: Trong DB V11, đây là cột TEXT, không phải bảng riêng
    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "student_tasks", columnDefinition = "TEXT")
    private String studentTasks;

    // Keywords là mảng text trong Postgres
    @Column(name = "keywords", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> keywords;

    // Các trường sách chưa có trong V11, tạm để Transient hoặc xóa đi
    @Transient
    private List<String> textbooks;

    @Transient
    private List<String> references;
}