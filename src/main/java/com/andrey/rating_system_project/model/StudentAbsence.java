package com.andrey.rating_system_project.model;

import com.andrey.rating_system_project.model.enums.AbsenceReasonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "student_absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAbsence {

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
    @JoinColumn(name = "teacher_user_id", nullable = false)
    private User teacher;

    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @Column(name = "hours", nullable = false)
    private Integer hours;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false)
    private AbsenceReasonType reasonType;
}