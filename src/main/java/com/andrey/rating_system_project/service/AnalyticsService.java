package com.andrey.rating_system_project.service;

import com.andrey.rating_system_project.dto.analytics.*;
import com.andrey.rating_system_project.model.Achievement;
import com.andrey.rating_system_project.model.StudentGrade;
import com.andrey.rating_system_project.model.SystemLog;
import com.andrey.rating_system_project.model.User;
import com.andrey.rating_system_project.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (filters.containsKey("rectorateId")) {
            return calculateRectorateWidgetData(widgetId, filters);
        }
        if (filters.containsKey("adminId")) {
            return calculateAdminWidgetData(widgetId, filters);
        }
        if (filters.containsKey("teacherId")) {
            return calculateTeacherWidgetData(widgetId, filters);
        }
        return calculateDeanStaffWidgetData(widgetId, filters);
    }


    // --- ДИСПЕТЧЕРЫ ПО РОЛЯМ ---

    private WidgetDataDto calculateRectorateWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "facultyPerformanceComparison":
                return new WidgetDataDto("Сравнение факультетов", "BAR_CHART", studentGradeRepository.getFacultyPerformanceComparison());
            case "enrollmentDynamics":
                return new WidgetDataDto("Динамика набора", "COMBO_CHART", studentGradeRepository.getEnrollmentDynamics());
            case "educationFormDistribution":
                return new WidgetDataDto("Формы обучения", "PIE_CHART", studentInfoRepository.countByEducationForm());
            case "extracurricularActivityOverview":
                return new WidgetDataDto("Внеучебная активность", "STACKED_BAR_CHART", achievementRepository.getFacultyExtracurricularActivity());
            default:
                return new WidgetDataDto("Неизвестный виджет для ректората", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateAdminWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "roleStatistics":
                return calculateRoleStatistics();
            case "userStatusOverview":
                return calculateUserStatusOverview();
            case "latestActions":
                return calculateLatestActions();
            case "registrationDynamics":
                return calculateRegistrationDynamics();
            default:
                return new WidgetDataDto("Неизвестный виджет для администратора", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateTeacherWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "myStudentPerformance":
                return calculateMyStudentPerformance(filters);
            case "myLatestAchievements":
                return calculateMyLatestAchievements(filters);
            case "myGroupComparison":
                return calculateMyGroupComparison(filters);
            default:
                return new WidgetDataDto("Неизвестный виджет для преподавателя", "TEXT", "Ошибка");
        }
    }

    private WidgetDataDto calculateDeanStaffWidgetData(String widgetId, Map<String, Object> filters) {
        switch (widgetId) {
            case "performanceDistribution":
                return calculatePerformanceDistribution(filters);
            case "averageScoreDynamics":
                return calculateAverageScoreDynamics(filters);
            case "keyMetrics":
                return calculateKeyMetrics(filters);
            case "groupComparison":
                return calculateGroupComparison(filters);
            case "contributionAnalysis":
                return calculateContributionAnalysis(filters);
            case "studentRankingList":
                return calculateStudentRankingList(filters);
            default:
                return new WidgetDataDto("Неизвестный виджет для деканата", "TEXT", "Ошибка");
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
                return new WidgetDataDto("Рейтинг", "TABLE", rankingList);
            case "myScoreBreakdown":
                return calculateMyScoreBreakdown(filters);
            case "myRankDynamics":
                return calculateMyRankDynamics(filters);
            default:
                return new WidgetDataDto("Неизвестный виджет для студента", "TEXT", "Ошибка");
        }
    }

    // --- МЕТОДЫ РАСЧЕТА (ОБЩИЕ И ДЛЯ ДЕКАНАТА) ---

    private CommonFilters extractCommonFilters(Map<String, Object> filters) {
        Integer facultyId = (Integer) filters.get("facultyId");
        Integer formationYear = (Integer) filters.get("formationYear");
        Integer groupId = (Integer) filters.get("groupId");
        List<String> assessmentTypes = (List<String>) filters.get("assessmentTypes");
        if (assessmentTypes != null && assessmentTypes.isEmpty()) {
            assessmentTypes = null;
        }
        String educationForm = (String) filters.get("educationForm");
        return new CommonFilters(facultyId, formationYear, groupId, assessmentTypes, educationForm);
    }

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
        List<ContributionItemDto> contributionData = new ArrayList<>(
                achievementRepository.getAchievementsContribution(f.facultyId(), f.formationYear(), f.groupId()));
        BigDecimal academicPoints = studentGradeRepository.getTotalAcademicPoints(
                f.facultyId(), f.formationYear(), f.groupId(), f.assessmentTypes(), f.educationForm());
        if (academicPoints != null) {
            contributionData.add(new ContributionItemDto("ACADEMIC", academicPoints));
        }
        return new WidgetDataDto("Вклад в общий рейтинг", "PIE_CHART", contributionData);
    }

    private WidgetDataDto calculateStudentRankingList(Map<String, Object> filters) {
        CommonFilters f = extractCommonFilters(filters);
        List<StudentRankingDto> rankingList = studentGradeRepository.getStudentRankingList(
                f.facultyId(), f.formationYear(), f.groupId(), f.assessmentTypes(), f.educationForm());
        return new WidgetDataDto("Итоговый рейтинг студентов", "TABLE", rankingList);
    }

    // --- МЕТОДЫ РАСЧЕТА (ДЛЯ АДМИНИСТРАТОРА) ---

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
        // Преобразуем в DTO
        List<SystemLogDto> logDtos = logs.stream()
                .map(SystemLogDto::new)
                .collect(Collectors.toList());
        return new WidgetDataDto("Последние действия", "LOG_LIST", logDtos);
    }

    private WidgetDataDto calculateRegistrationDynamics() {
        List<StatItemDto> stats = userRepository.countRegistrationsLast7Days();
        return new WidgetDataDto("Динамика регистраций (7 дней)", "LINE_CHART", stats);
    }

    // --- МЕТОДЫ РАСЧЕТА (ДЛЯ ПРЕПОДАВАТЕЛЯ) ---

    private WidgetDataDto calculateMyStudentPerformance(Map<String, Object> filters) {
        Integer teacherId = (Integer) filters.get("teacherId");
        if (teacherId == null) {
            throw new IllegalArgumentException("teacherId is required for this widget");
        }
        List<StudentPerformanceDto> performanceData = studentGradeRepository.getStudentPerformanceForTeacher(teacherId);
        return new WidgetDataDto("Успеваемость моих студентов", "TABLE", performanceData);
    }

    // ИЗМЕНЕНИЯ ЗДЕСЬ
    private WidgetDataDto calculateMyLatestAchievements(Map<String, Object> filters) {
        Integer teacherId = (Integer) filters.get("teacherId");
        if (teacherId == null) {
            throw new IllegalArgumentException("teacherId is required for this widget");
        }
        List<Achievement> achievements = achievementRepository.findTop5ByAddedByIdOrderByCreatedAtDesc(teacherId);
        // Преобразуем список сущностей в список DTO
        List<AchievementDto> achievementDtos = achievements.stream()
                .map(AchievementDto::new)
                .collect(Collectors.toList());
        return new WidgetDataDto("Последние добавленные достижения", "ACHIEVEMENT_LIST", achievementDtos);
    }

    private WidgetDataDto calculateMyGroupComparison(Map<String, Object> filters) {
        Integer teacherId = (Integer) filters.get("teacherId");
        if (teacherId == null) {
            throw new IllegalArgumentException("teacherId is required for this widget");
        }
        List<ComparisonItemDto> comparisonData = studentGradeRepository.getGroupComparisonForTeacher(teacherId);
        return new WidgetDataDto("Сравнение успеваемости групп", "BAR_CHART", comparisonData);
    }


    // --- МЕТОДЫ РАСЧЕТА (ДЛЯ СТУДЕНТА) ---

    private List<StudentRankingDto> getStudentRankingListForContext(Map<String, Object> filters) {
        Integer studentId = (Integer) filters.get("studentId");
        String context = (String) filters.getOrDefault("comparisonContext", "faculty");

        User student = userRepository.findById(studentId).orElse(null);
        if (student == null || student.getStudentInfo() == null || student.getStudentInfo().getGroup() == null) {
            return List.of();
        }

        Integer groupId = null;
        Integer formationYear = null;
        Integer facultyId = null;

        switch (context) {
            case "group":
                groupId = student.getStudentInfo().getGroup().getId();
                break;
            case "specialty_course":
                formationYear = student.getStudentInfo().getGroup().getFormationYear();
                break;
            case "faculty":
                facultyId = student.getStudentInfo().getGroup().getFaculty().getId();
                break;
        }
        return studentGradeRepository.getStudentRankingList(facultyId, formationYear, groupId, null, null);
    }

    private WidgetDataDto calculateMyScoreBreakdown(Map<String, Object> filters) {
        Integer studentId = (Integer) filters.get("studentId");
        List<ContributionItemDto> breakdownData = new ArrayList<>(
                achievementRepository.getStudentAchievementsContribution(studentId));
        BigDecimal academicScore = studentGradeRepository.getStudentAverageAcademicScore(studentId);
        if (academicScore != null) {
            breakdownData.add(new ContributionItemDto("ACADEMIC", academicScore));
        }
        return new WidgetDataDto("Детализация моих баллов", "PIE_CHART", breakdownData);
    }

    private WidgetDataDto calculateMyRankDynamics(Map<String, Object> filters) {
        Integer studentId = (Integer) filters.get("studentId");
        String absenceType = (String) filters.get("absenceType");
        List<String> lines = (List<String>) filters.get("lines");

        if (lines == null || lines.isEmpty()) {
            return new WidgetDataDto("Динамика моего рейтинга", "MULTI_LINE_CHART", new HashMap<>());
        }

        List<DynamicsDetailDto> details = studentGradeRepository.getStudentDynamicsDetails(studentId, absenceType);
        Map<String, List<DynamicsPointDto>> resultLines = new HashMap<>();

        BigDecimal cumulativeTotal = BigDecimal.ZERO;
        BigDecimal cumulativeAchievements = BigDecimal.ZERO;
        BigDecimal cumulativeAbsences = BigDecimal.ZERO;

        for (DynamicsDetailDto detail : details) {
            BigDecimal academicScore = detail.getAcademicScoreInSemester() != null ? detail.getAcademicScoreInSemester() : BigDecimal.ZERO;
            BigDecimal achievements = detail.getAchievementsInSemester() != null ? detail.getAchievementsInSemester() : BigDecimal.ZERO;
            BigDecimal absences = detail.getAbsencePenaltyInSemester() != null ? detail.getAbsencePenaltyInSemester() : BigDecimal.ZERO;

            BigDecimal totalInSemester = academicScore.add(achievements).add(absences);

            cumulativeTotal = cumulativeTotal.add(totalInSemester);
            cumulativeAchievements = cumulativeAchievements.add(achievements);
            cumulativeAbsences = cumulativeAbsences.add(absences);

            if (lines.contains("cumulativeTotal")) {
                resultLines.computeIfAbsent("cumulativeTotal", k -> new ArrayList<>())
                        .add(new DynamicsPointDto(detail.getSemester(), cumulativeTotal));
            }
            if (lines.contains("semesterTotal")) {
                resultLines.computeIfAbsent("semesterTotal", k -> new ArrayList<>())
                        .add(new DynamicsPointDto(detail.getSemester(), totalInSemester));
            }
            if (lines.contains("academic")) {
                resultLines.computeIfAbsent("academic", k -> new ArrayList<>())
                        .add(new DynamicsPointDto(detail.getSemester(), academicScore));
            }
            if (lines.contains("achievements")) {
                resultLines.computeIfAbsent("achievements", k -> new ArrayList<>())
                        .add(new DynamicsPointDto(detail.getSemester(), cumulativeAchievements));
            }
            if (lines.contains("absences")) {
                resultLines.computeIfAbsent("absences", k -> new ArrayList<>())
                        .add(new DynamicsPointDto(detail.getSemester(), cumulativeAbsences));
            }
        }
        return new WidgetDataDto("Динамика моего рейтинга", "MULTI_LINE_CHART", resultLines);
    }

    private record CommonFilters(Integer facultyId, Integer formationYear, Integer groupId, List<String> assessmentTypes, String educationForm) {}
}