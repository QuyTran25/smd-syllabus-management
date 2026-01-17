package vn.edu.smd.core.module.admin.service;

import vn.edu.smd.core.module.admin.dto.AcademicTermDto;
import java.util.List;
import java.util.UUID;

public interface AcademicTermService {
    List<AcademicTermDto> getAllTerms();
    AcademicTermDto createTerm(AcademicTermDto dto);
    void deleteTerm(UUID id);
    void setActiveTerm(UUID id);
}