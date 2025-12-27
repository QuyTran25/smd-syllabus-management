package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@Table(name = "syllabi", schema = "core_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Syllabus {
    @Id
    private UUID id;

    private String code;

    @Column(name = "name_vi") 
    private String nameVi;

    @Column(name = "name_en") 
    private String nameEn;

    private String term;
    private Integer credits;
    private String faculty;
    private String program;

    @Column(name = "lecturer_name") 
    private String lecturerName;

    @Column(name = "lecturer_email") 
    private String lecturerEmail;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "teaching_methods") 
    private String teachingMethods;

    @Column(name = "theory_hours") 
    private Integer theoryHours;

    @Column(name = "practice_hours") 
    private Integer practiceHours;

    @Column(name = "self_study_hours") 
    private Integer selfStudyHours;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    private String status;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY)
    private List<SyllabusCLO> clos;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY)
    private List<AssessmentMatrix> assessmentMatrix;

    @ElementCollection
    @CollectionTable(name = "syllabus_objectives", schema = "core_service", joinColumns = @JoinColumn(name = "syllabus_id"))
    @Column(name = "objectives")
    private List<String> objectives;

    @ElementCollection
    @CollectionTable(name = "syllabus_tasks", schema = "core_service", joinColumns = @JoinColumn(name = "syllabus_id"))
    @Column(name = "student_tasks")
    private List<String> studentTasks;

    @ElementCollection
    @CollectionTable(name = "syllabus_textbooks", schema = "core_service", joinColumns = @JoinColumn(name = "syllabus_id"))
    @Column(name = "textbooks")
    private List<String> textbooks;

    @ElementCollection
    @CollectionTable(name = "syllabus_references", schema = "core_service", joinColumns = @JoinColumn(name = "syllabus_id"))
    @Column(name = "references")
    private List<String> references;
}