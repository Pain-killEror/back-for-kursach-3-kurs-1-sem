package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.model.Specialty;
import com.andrey.rating_system_project.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public List<Specialty> findAll() {
        return specialtyRepository.findAll();
    }

    // НОВЫЙ МЕТОД: Получение специальностей по ID факультета
    public List<Specialty> findSpecialtiesByFacultyId(Integer facultyId) {
        return specialtyRepository.findByFacultyId(facultyId);
    }
}