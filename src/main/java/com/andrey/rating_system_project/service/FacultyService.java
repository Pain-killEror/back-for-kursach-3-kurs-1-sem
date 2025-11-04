package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.model.Faculty;
import com.andrey.rating_system_project.repository.FacultyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<Faculty> findAll() {
        return facultyRepository.findAll();
    }
}
