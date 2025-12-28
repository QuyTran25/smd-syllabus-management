package vn.edu.smd.core.module.syllabus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.smd.core.entity.AcademicTerm;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.repository.AcademicTermRepository;
import vn.edu.smd.core.repository.SubjectRepository;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicDataController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AcademicTermRepository academicTermRepository;

    @GetMapping("/subjects")
    public List<Subject> getSubjects() {
        return subjectRepository.findAll();
    }

    @GetMapping("/semesters")
    public List<AcademicTerm> getSemesters() {
        return academicTermRepository.findAll();
    }
}
