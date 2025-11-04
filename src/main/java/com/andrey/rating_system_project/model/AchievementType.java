package com.andrey.rating_system_project.model;


import com.andrey.rating_system_project.model.enums.AchievementCategory;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "achievement_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AchievementCategory category;

    @Column(name = "default_points", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultPoints;
}
