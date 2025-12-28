package vn.edu.smd.core.module.syllabus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.smd.core.entity.Department;
import vn.edu.smd.core.repository.DepartmentRepository;

import java.util.List;

@RestController("syllabusDepartmentController")
@RequestMapping("/api/syllabus/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public List<Department> getAllDepartments() {
        // Trả về danh sách khoa để đổ vào Dropdown
        return departmentRepository.findAll();
    }
}