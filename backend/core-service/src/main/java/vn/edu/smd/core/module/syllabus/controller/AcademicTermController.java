package vn.edu.smd.core.module.syllabus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.smd.core.entity.AcademicTerm;
import vn.edu.smd.core.repository.AcademicTermRepository;

import java.util.List;

@RestController
@RequestMapping("/api/academic-terms")
public class AcademicTermController {

    @Autowired
    private AcademicTermRepository academicTermRepository;

    @GetMapping
    public List<AcademicTerm> getAllTerms() {
        // Trả về danh sách học kỳ
        return academicTermRepository.findAll();
    }
}