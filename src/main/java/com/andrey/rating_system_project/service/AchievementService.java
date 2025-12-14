package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.AchievementCreateDto;
import com.andrey.rating_system_project.exception.ResourceNotFoundException;
import com.andrey.rating_system_project.model.Achievement;
import com.andrey.rating_system_project.model.AchievementType;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.AchievementRepository;
import com.andrey.rating_system_project.repository.AchievementTypeRepository;
import com.andrey.rating_system_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementTypeRepository achievementTypeRepository;
    private final UserRepository userRepository;

    public List<AchievementType> getAllTypes() {
        return achievementTypeRepository.findAll();
    }

    public void addAchievement(AchievementCreateDto createDto) {
        // Находим, кто добавляет достижение (текущий декан/админ)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User addedBy = userRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        User student = userRepository.findById(createDto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        AchievementType type = achievementTypeRepository.findById(createDto.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Achievement type not found"));

        Achievement newAchievement = new Achievement();
        newAchievement.setStudent(student);
        newAchievement.setType(type);
        newAchievement.setPointsAwarded(type.getDefaultPoints());
        newAchievement.setAddedBy(addedBy);
        newAchievement.setAchievementDate(LocalDate.now());
        newAchievement.setDescription("Добавлено сотрудником деканата");

        achievementRepository.save(newAchievement);
    }
}