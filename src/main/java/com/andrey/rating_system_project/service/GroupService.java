package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.GroupWithCountDto;
import com.andrey.rating_system_project.exception.ResourceNotFoundException;
import com.andrey.rating_system_project.model.Faculty;
import com.andrey.rating_system_project.model.Group;
import com.andrey.rating_system_project.model.Specialty;
import com.andrey.rating_system_project.repository.FacultyRepository;
import com.andrey.rating_system_project.repository.GroupRepository;
import com.andrey.rating_system_project.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final FacultyRepository facultyRepository;
    private final SpecialtyRepository specialtyRepository;

    public GroupService(GroupRepository groupRepository, FacultyRepository facultyRepository, SpecialtyRepository specialtyRepository) {
        this.groupRepository = groupRepository;
        this.facultyRepository = facultyRepository;
        this.specialtyRepository = specialtyRepository;
    }

    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    public List<Group> findGroupsByFacultyId(Integer facultyId) {
        return groupRepository.findByFacultyId(facultyId);
    }

    /**
     * Ищет группы по параметрам и проверяет их заполненность.
     */
    public List<GroupWithCountDto> findAvailableGroups(Integer facultyId, Integer specialtyId, Integer course) {
        int formationYear = calculateFormationYear(course);

        // Ищем группы в БД
        List<Group> groups = groupRepository.findByFacultyAndSpecialtyAndYear(facultyId, specialtyId, formationYear);

        // Преобразуем в DTO с подсчетом студентов
        return groups.stream()
                .map(group -> {
                    Long count = groupRepository.countStudentsInGroup(group.getId());
                    return new GroupWithCountDto(group, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * Автоматически создает следующую группу в потоке.
     */
    @Transactional
    public Group createAutoGroup(Integer facultyId, Integer specialtyId, Integer course) {
        int formationYear = calculateFormationYear(course);

        // 1. Ищем существующие группы этого потока
        List<Group> existingGroups = groupRepository.findByFacultyAndSpecialtyAndYear(facultyId, specialtyId, formationYear);

        // 2. Генерируем имя новой группы
        String newName;
        if (existingGroups.isEmpty()) {
            // Если групп нет, создаем первую.
            // Логика генерации первого номера может быть сложной, но для примера берем:
            // "ID_Специальности" + "Курс" + "1" (например: 55 + 1 + 1 = 5511)
            // Но лучше полагаться на то, что в БД уже есть данные, как вы сказали.
            // Если данных нет совсем, делаем простую заглушку:
            newName = String.valueOf(specialtyId * 100 + course * 10 + 1);
        } else {
            // Ищем максимальный номер группы
            // Предполагаем, что названия групп числовые (как на скриншотах: 5521, 5522)
            Integer maxName = existingGroups.stream()
                    .map(g -> {
                        try {
                            return Integer.parseInt(g.getName());
                        } catch (NumberFormatException e) {
                            return 0; // Если имя не число, игнорируем
                        }
                    })
                    .max(Comparator.naturalOrder())
                    .orElse(0);

            if (maxName > 0) {
                newName = String.valueOf(maxName + 1);
            } else {
                // Если имена не числовые, просто добавляем суффикс
                newName = existingGroups.get(0).getName() + "-NEW";
            }
        }

        // 3. Создаем и сохраняем группу
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

        Group newGroup = new Group();
        newGroup.setName(newName);
        newGroup.setFormationYear(formationYear);
        newGroup.setFaculty(faculty);
        newGroup.setSpecialty(specialty);

        return groupRepository.save(newGroup);
    }

    // Вспомогательный метод расчета года
    private int calculateFormationYear(Integer course) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        // Учебный год начинается в сентябре.
        // Если сейчас Сентябрь-Декабрь 2025, то 1 курс поступил в 2025.
        // Если сейчас Январь-Август 2026, то 1 курс поступил в 2025.
        int academicYearStart = (currentMonth >= 9) ? currentYear : currentYear - 1;
        return academicYearStart - (course - 1);
    }
}