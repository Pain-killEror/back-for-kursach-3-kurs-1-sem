    package com.andrey.rating_system_project.model;

    import com.andrey.rating_system_project.model.enums.EducationForm;
    import jakarta.persistence.*;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;

    @Entity
    @Table(name = "students_info")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class StudentInfo {

        @Id
        @Column(name = "user_id")
        private Integer id;

        @OneToOne(fetch = FetchType.LAZY)
        @MapsId
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "group_id")
        private Group group;

        @Column(name = "record_book_number", unique = true, length = 50)
        private String recordBookNumber;

        @Enumerated(EnumType.STRING)
        @Column(name = "education_form")
        private EducationForm educationForm;
    }
