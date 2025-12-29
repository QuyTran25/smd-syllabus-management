package vn.edu.smd.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "v_syllabus_full", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
public class SyllabusFullView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "subject_name_vi")
    private String subjectNameVi;

    @Column(name = "status")
    private String status;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "owner_full_name")
    private String ownerName;

    // add other fields as needed later
}
