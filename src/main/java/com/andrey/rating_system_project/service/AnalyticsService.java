package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.analytics.*;
import com.andrey.rating_system_project.model.Achievement;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.SystemLog;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final StudentGradeRepository studentGradeRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final SystemLogRepository systemLogRepository;
    private final StudentInfoRepository studentInfoRepository;

    public AnalyticsService(StudentGradeRepository studentGradeRepository, AchievementRepository achievementRepository,
                            UserRepository userRepository, SystemLogRepository systemLogRepository,
                            StudentInfoRepository studentInfoRepository) {
        this.studentGradeRepository = studentGradeRepository;
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.systemLogRepository = systemLogRepository;
        this.studentInfoRepository = studentInfoRepository;
    }

    public AnalyticsResponseDto getAnalytics(AnalyticsRequestDto request) {
        AnalyticsResponseDto response = new AnalyticsResponseDto();
        Map<String, WidgetDataDto> widgetDataMap = new HashMap<>();

        if (request.getFilters().containsKey("studentId")) {
            List<StudentRankingDto> rankingList = getStudentRankingListForContext(request.getFilters());
            for (String widgetId : request.getWidgetIds()) {
                WidgetDataDto data = calculateStudentWidgetData(widgetId, request.getFilters(), rankingList);
                widgetDataMap.put(widgetId, data);
            }
        } else {
            for (String widgetId : request.getWidgetIds()) {
                WidgetDataDto data = calculateWidgetData(widgetId, request.getFilters());
                widgetDataMap.put(widgetId, data);
            }
        }

        response.setWidgets(widgetDataMap);
        return response;
    }

    private WidgetDataDto calculateWidgetData(String widgetId, Map<String, Object> filters) {
        if (filters.containsKey("rectorateId")) return calculateRectorateWidgetData(widgetId, filters);
        if (filters.containsKey("adminId")) return calculateAdminWidgetData(widgetId, filters);
        if (filters.containsKey("teacherId")) return calculateTeacherWidgetData(widgetId, filters);
        return calculateDeanStaffWidgetData(widgetId, filters);
    }

    private WidgetDataDto calculateRectorateWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "facultyPerformanceComparison":
                return new WidgetDataDto("Сравнение факультетов", "BAR_CHART", studentGradeRepository.getFacultyPerformanceComparison());
            case "educationFormDistribution":
                return new WidgetDataDto("Формы обучения", "PIE_CHART", studentInfoRepository.countByEducationForm());
            case "extracurricularActivityOverview":
                return new WidgetDataDto("Внеучебная активность", "STACKED_BAR_CHART", achievementRepository.getFacultyExtracurricularActivity());
            default: return new WidgetDataDto("Неизвестный виджет для ректората", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateAdminWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "roleStatistics": return calculateRoleStatistics();
            case "userStatusOverview": return calculateUserStatusOverview();
            case "latestActions": return calculateLatestActions();
            case "registrationDynamics": return calculateRegistrationDynamics();
            default: return new WidgetDataDto("Неизвестный виджет для администратора", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateTeacherWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "myStudentPerformance": return calculateMyStudentPerformance(filters);
            case "myLatestAchievements": return calculateMyLatestAchievements(filters);
            case "myGroupComparison": return calculateMyGroupComparison(filters);
            default: return new WidgetDataDto("Неизвестный виджет для преподавателя", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateDeanStaffWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "performanceDistribution": return calculatePerformanceDistribution(filters);
            case "averageScoreDynamics": return calculateAverageScoreDynamics(filters);
            case "keyMetrics": return calculateKeyMetrics(filters);
            case "groupComparison": return calculateGroupComparison(filters);
            case "contributionAnalysis": return calculateContributionAnalysis(filters);
            case "studentRankingList": return calculateStudentRankingList(filters);
            default: return new WidgetDataDto("Неизвестный виджет для деканата", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateStudentWidgetData(String widgetId, Map<String, Object> filters, List<StudentRankingDto> rankingList) {
        Integer studentId = (Integer) filters.get("studentId");
        StudentRankingDto currentUserRanking = rankingList.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(new StudentRankingDto(studentId, "N/A", "N/A", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        switch (widgetId) {
            case "myRank":
                long rank = rankingList.indexOf(currentUserRanking) + 1;
                long total = rankingList.size();
                return new WidgetDataDto("Мое место в рейтинге", "RANK_CARD", new StudentRankDto(rank > 0 ? rank : -1, total));
            case "myScores":
                MyScoresDto scoresDto = new MyScoresDto(currentUserRanking.getTotalScore(), currentUserRanking.getAcademicScore());
                return new WidgetDataDto("Мои ключевые показатели", "KPI_CARDS", scoresDto);
            case "studentRankingList":
                return calculateStudentRankingList(filters);
            case "myScoreBreakdown":
                return calculateMyScoreBreakdown(filters);
            case "myRankDynamics":
                return calculateMyRankDynamics(filters);
            default: return new WidgetDataDto("Неизвестный виджет для студента", "TEXT", "Ошибка");
        }
    }

    private CommonFilters extractCommonFilters(Map<String, Object> filters) {
        Integer facultyId = (Integer) filters.get("facultyId");
        Integer formationYear = (Integer) filters.get("formationYear");
        Integer groupId = (Integer) filters.get("groupId");
        List<String> assessmentTypes = (List<String>) filters.get("assessmentTypes");
        if (assessmentTypes != null && assessmentTypes.isEmpty()) assessmentTypes = null;
        String educationForm = (String) filters.get("educationForm");
        return new CommonFilters(facultyId, formationYear, groupId, assessmentTypes, educationForm);
    }

    private WidgetDataDto calculateStudentRankingList(Map<String, Object> filters) {
        Integer studentId = (Integer) filters.get("studentId");
        String context = (String) filters.getOrDefault("comparisonContext", "faculty");

        Integer groupId = null;
        Integer formationYear = null;
        Integer facultyId = null;
        Integer targetGroupId = null;

        if (studentId != null) {
            User student = userRepository.findById(studentId).orElse(null);
            if (student != null && student.getStudentInfo() != null && student.getStudentInfo().getGroup() != null) {
                targetGroupId = student.getStudentInfo().getGroup().getId();
                switch (context) {
                    case "group": groupId = targetGroupId; break;
                    case "specialty_course": formationYear = student.getStudentInfo().getGroup().getFormationYear(); break;
                    case "faculty": facultyId = student.getStudentInfo().getGroup().getFaculty().getId(); break;
                }
            }
        } else {
            CommonFilters f = extractCommonFilters(filters);
            groupId = f.groupId();
            formationYear = f.formationYear();
            facultyId = f.facultyId();
            targetGroupId = groupId;
        }

        List<Long> availableSemesters = targetGroupId != null ?
                studentGradeRepository.getAvailableSemesters(targetGroupId) : new ArrayList<>();

        Long selectedSemester = null;
        if (filters.containsKey("rankingSemester") && filters.get("rankingSemester") != null && !filters.get("rankingSemester").toString().isEmpty()) {
            try {
                selectedSemester = Long.parseLong(filters.get("rankingSemester").toString());
            } catch (NumberFormatException e) { /* ignore */ }
        }

        List<StudentRankingDto> rankingList;
        if (selectedSemester != null) {
            rankingList = studentGradeRepository.getStudentRankingListBySemester(facultyId, formationYear, groupId, selectedSemester);
        } else {
            rankingList = studentGradeRepository.getStudentRankingList(facultyId, formationYear, groupId, null, null);
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("data", rankingList);
        resultData.put("availableSemesters", availableSemesters);
        resultData.put("selectedSemester", selectedSemester);

        return new WidgetDataDto("Рейтинг студентов", "TABLE_COMPLEX", resultData);
    }

    private List<StudentRankingDto> getStudentRankingListForContext(Map<String, Object> filters) {
        WidgetDataDto widgetData = calculateStudentRankingList(filters);
        Map<String, Object> map = (Map<String, Object>) widgetData.getData();
        return (List<StudentRankingDto>) map.get("data");
    }

    // ... Стандартные методы для других ролей ...
    private WidgetDataDto calculatePerformanceDistribution(Map<String, Object> filters) {
        CommonFilters f = extractCommonFilters(filters);
        List<DistributionItemDto> distribution = studentGradeRepository.getPerformanceDistribution(
                f.facultyId(), f.formationYear(), f.groupId(), f.assessmentTypes(), f.educationForm());
        return new WidgetDataDto("Распределение по успеваемости", "BAR_CHART", distribution);
    }
    private WidgetDataDto calculateAverageScoreDynamics(Map<String, Object> filters) {
        CommonFilters f = extractCommonFilters(filters);
        List<DynamicsPointDto> dynamics = studentGradeRepository.getAverageScoreDynamics(
                f.facultyId(), f.formationYear(), f.groupId(), f.assessmentTypes(), f.educationForm());
        return new WidgetDataDto("Динамика среднего балла", "LINE_CHART", dynamics);
    }
    private WidgetDataDto calculateKeyMetrics(Map<String, Object> filters) {
        CommonFilters f = extractCommonFilters(filters);
        KeyMetricsDto metrics = studentGradeRepository.getKeyMetrics(
                f.facultyId(), f.formationYear(), f.groupId(), f.assessmentTypes(), f.educationForm());
        return new WidgetDataDto("Ключевые показатели", "KPI_CARDS", metrics);
    }
    private WidgetDataDto calculateGroupComparison(Map<String, Object> filters) {
        CommonFilters f = extractCommonFilters(filters);
        List<ComparisonItemDto> comparisonData = studentGradeRepository.getGroupComparison(
                f.facultyId(), f.formationYear(), f.assessmentTypes(), f.educationForm());
        return new WidgetDataDto("Сравнение успеваемости групп", "COLUMN_CHART", comparisonData);
    }
    private WidgetDataDto calculateContributionAnalysis(Map<String, Object> filters) {
        CommonFilters f = extractCommonFilters(filters);

        // Получаем настоящие достижения из БД (SCIENCE, SOCIAL, SPORTS, CULTURE)
        List<ContributionItemDto> contributionData = new ArrayList<>(
                achievementRepository.getAchievementsContribution(f.facultyId(), f.formationYear(), f.groupId())
        );

        // Блок, который добавлял "ACADEMIC", УДАЛЕН.

        return new WidgetDataDto("Вклад в общий рейтинг", "PIE_CHART", contributionData);
    }
    private WidgetDataDto calculateRoleStatistics() {
        List<StatItemDto> stats = userRepository.countUsersByRole();
        return new WidgetDataDto("Статистика по ролям", "PIE_CHART", stats);
    }
    private WidgetDataDto calculateUserStatusOverview() {
        List<StatItemDto> stats = userRepository.countUsersByStatus();
        return new WidgetDataDto("Статус пользователей", "BAR_CHART", stats);
    }
    private WidgetDataDto calculateLatestActions() {
        List<SystemLog> logs = systemLogRepository.findTop10ByOrderByCreatedAtDesc();
        List<SystemLogDto> logDtos = logs.stream().map(SystemLogDto::new).collect(Collectors.toList());
        return new WidgetDataDto("Последние действия", "LOG_LIST", logDtos);
    }
    private WidgetDataDto calculateRegistrationDynamics() {
        // Получаем "сырые" данные (массив объектов) из базы
        List<Object[]> rawData = userRepository.countRegistrationsLast7DaysRaw();

        // Преобразуем их в DTO
        List<StatItemDto> stats = rawData.stream()
                .map(row -> new StatItemDto(
                        (String) row[0],                // label (дата)
                        ((Number) row[1]).longValue()   // count (количество). Number позволяет безопасно брать и Integer и BigInteger
                ))
                .collect(Collectors.toList());

        return new WidgetDataDto("Динамика регистраций (7 дней)", "LINE_CHART", stats);
    }
    private WidgetDataDto calculateMyStudentPerformance(Map<String, Object> filters) {
        Integer teacherId = (Integer) filters.get("teacherId");
        if (teacherId == null) throw new IllegalArgumentException("teacherId is required");
        return new WidgetDataDto("Успеваемость моих студентов", "TABLE", studentGradeRepository.getStudentPerformanceForTeacher(teacherId));
    }
    private WidgetDataDto calculateMyLatestAchievements(Map<String, Object> filters) {
        Integer teacherId = (Integer) filters.get("teacherId");
        if (teacherId == null) throw new IllegalArgumentException("teacherId is required");
        List<Achievement> achievements = achievementRepository.findTop5ByAddedByIdOrderByCreatedAtDesc(teacherId);
        List<AchievementDto> achievementDtos = achievements.stream().map(AchievementDto::new).collect(Collectors.toList());
        return new WidgetDataDto("Последние добавленные достижения", "ACHIEVEMENT_LIST", achievementDtos);
    }
    private WidgetDataDto calculateMyGroupComparison(Map<String, Object> filters) {
        Integer teacherId = (Integer) filters.get("teacherId");
        if (teacherId == null) throw new IllegalArgumentException("teacherId is required");
        return new WidgetDataDto("Сравнение успеваемости групп", "BAR_CHART", studentGradeRepository.getGroupComparisonForTeacher(teacherId));
    }

    // --- ИЗМЕНЕННЫЙ МЕТОД ДЛЯ ДЕТАЛИЗАЦИИ БАЛЛОВ (ТОЛЬКО ДОСТИЖЕНИЯ) ---
    private WidgetDataDto calculateMyScoreBreakdown(Map<String, Object> filters) {
        Integer studentId = (Integer) filters.get("studentId");
        List<Object[]> rawData = studentGradeRepository.getDetailedBreakdownBySemester(studentId);

        List<Long> semesters = rawData.stream()
                .map(row -> ((Number) row[0]).longValue())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Long selectedSemester = null;
        if (filters.containsKey("semester") && filters.get("semester") != null && !filters.get("semester").toString().isEmpty()) {
            try {
                selectedSemester = Long.parseLong(filters.get("semester").toString());
            } catch (NumberFormatException e) { /* ignore */ }
        }

        List<ContributionItemDto> items;
        if (selectedSemester != null) {
            // Фильтруем по семестру
            Long sem = selectedSemester;
            items = rawData.stream()
                    .filter(row -> ((Number) row[0]).longValue() == sem)
                    // ИЗМЕНЕНИЕ: Берем ТОЛЬКО категории достижений, исключаем ACADEMIC
                    .filter(row -> !"ACADEMIC".equals((String) row[1]))
                    .map(row -> new ContributionItemDto((String) row[1], (BigDecimal) row[2]))
                    .collect(Collectors.toList());
        } else {
            // СУММИРУЕМ ВСЕ (Накопительный итог)
            Map<String, BigDecimal> accumulated = rawData.stream()
                    .filter(row -> !"ACADEMIC".equals((String) row[1])) // ИЗМЕНЕНИЕ: Исключаем ACADEMIC
                    .collect(Collectors.groupingBy(
                            row -> (String) row[1], // Категория
                            Collectors.reducing(BigDecimal.ZERO, row -> (BigDecimal) row[2], BigDecimal::add)
                    ));
            items = accumulated.entrySet().stream()
                    .map(e -> new ContributionItemDto(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("availableSemesters", semesters);
        resultData.put("selectedSemester", selectedSemester);
        resultData.put("breakdown", items);

        return new WidgetDataDto("Детализация баллов", "BAR_CHART_COMPLEX", resultData);
    }

    private WidgetDataDto calculateMyRankDynamics(Map<String, Object> filters) {
        Integer studentId = (Integer) filters.get("studentId");
        String absenceType = (String) filters.get("absenceType");
        List<String> lines = (List<String>) filters.get("lines");
        Object compareWithIdObj = filters.get("compareWithStudentId");
        Integer compareWithStudentId = null;
        if (compareWithIdObj != null) {
            if (compareWithIdObj instanceof Integer) compareWithStudentId = (Integer) compareWithIdObj;
            else if (compareWithIdObj instanceof String) {
                try { compareWithStudentId = Integer.parseInt((String) compareWithIdObj); } catch (NumberFormatException e) {}
            }
        }
        if (lines == null || lines.isEmpty()) return new WidgetDataDto("Динамика моего рейтинга", "MULTI_LINE_CHART", new HashMap<>());
        Map<String, List<DynamicsPointDto>> resultLines = new HashMap<>(calculateDynamicsForStudent(studentId, absenceType, lines, ""));
        if (compareWithStudentId != null) {
            Map<String, List<DynamicsPointDto>> compareLines = calculateDynamicsForStudent(compareWithStudentId, absenceType, lines, "_compare");
            resultLines.putAll(compareLines);
        }
        return new WidgetDataDto("Динамика моего рейтинга", "MULTI_LINE_CHART", resultLines);
    }

    private Map<String, List<DynamicsPointDto>> calculateDynamicsForStudent(Integer studentId, String absenceType, List<String> lines, String keySuffix) {
        List<DynamicsDetailDto> details = studentGradeRepository.getStudentDynamicsDetails(studentId, absenceType);
        Map<String, List<DynamicsPointDto>> result = new HashMap<>();
        BigDecimal cumulativeTotal = BigDecimal.ZERO;
        BigDecimal cumulativeAchievements = BigDecimal.ZERO;
        BigDecimal cumulativeUnexcusedHours = BigDecimal.ZERO;
        BigDecimal cumulativeExcusedHours = BigDecimal.ZERO;

        for (DynamicsDetailDto detail : details) {
            BigDecimal academicScore = detail.getAcademicScoreInSemester() != null ? detail.getAcademicScoreInSemester() : BigDecimal.ZERO;
            BigDecimal achievements = detail.getAchievementsInSemester() != null ? detail.getAchievementsInSemester() : BigDecimal.ZERO;
            BigDecimal penalty = detail.getAbsencePenaltyInSemester() != null ? detail.getAbsencePenaltyInSemester() : BigDecimal.ZERO;
            BigDecimal unexcusedHours = detail.getUnexcusedHoursInSemester() != null ? detail.getUnexcusedHoursInSemester() : BigDecimal.ZERO;
            BigDecimal excusedHours = detail.getExcusedHoursInSemester() != null ? detail.getExcusedHoursInSemester() : BigDecimal.ZERO;

            BigDecimal totalInSemester = academicScore.add(achievements).add(penalty);
            cumulativeTotal = cumulativeTotal.add(totalInSemester);
            cumulativeAchievements = cumulativeAchievements.add(achievements);
            cumulativeUnexcusedHours = cumulativeUnexcusedHours.add(unexcusedHours);
            cumulativeExcusedHours = cumulativeExcusedHours.add(excusedHours);

            if (lines.contains("cumulativeTotal")) result.computeIfAbsent("cumulativeTotal" + keySuffix, k -> new ArrayList<>()).add(new DynamicsPointDto(detail.getSemester(), cumulativeTotal));
            if (lines.contains("semesterTotal")) result.computeIfAbsent("semesterTotal" + keySuffix, k -> new ArrayList<>()).add(new DynamicsPointDto(detail.getSemester(), totalInSemester));
            if (lines.contains("academic")) result.computeIfAbsent("academic" + keySuffix, k -> new ArrayList<>()).add(new DynamicsPointDto(detail.getSemester(), academicScore));
            if (lines.contains("achievements")) result.computeIfAbsent("achievements" + keySuffix, k -> new ArrayList<>()).add(new DynamicsPointDto(detail.getSemester(), cumulativeAchievements));
            if (lines.contains("excused")) result.computeIfAbsent("excused" + keySuffix, k -> new ArrayList<>()).add(new DynamicsPointDto(detail.getSemester(), cumulativeExcusedHours));
            if (lines.contains("unexcused")) result.computeIfAbsent("unexcused" + keySuffix, k -> new ArrayList<>()).add(new DynamicsPointDto(detail.getSemester(), cumulativeUnexcusedHours));
        }
        return result;
    }

    private record CommonFilters(Integer facultyId, Integer formationYear, Integer groupId, List<String> assessmentTypes, String educationForm) {}
}