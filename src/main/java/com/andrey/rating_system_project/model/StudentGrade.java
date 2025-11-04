package com.andrey.rating_system_project.model;

import com.andrey.rating_system_project.model.enums.AssessmentType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "student_grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id")
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false)
    private AssessmentType assessmentType;

    @Column(name = "mark", nullable = false)
    private Integer mark;

    @Column(name = "pre_exam_average", precision = 3, scale = 1)
    private BigDecimal preExamAverage;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;
}
