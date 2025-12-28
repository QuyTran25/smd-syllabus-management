package vn.edu.smd.core.module.syllabus.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.repository.SubjectRepository;
import java.util.List;

@RestController("syllabusSubjectController")
@RequestMapping("/api/syllabus/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }
}