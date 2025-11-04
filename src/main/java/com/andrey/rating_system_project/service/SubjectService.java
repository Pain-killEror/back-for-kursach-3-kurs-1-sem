package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.model.Subject;
import com.andrey.rating_system_project.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }
}