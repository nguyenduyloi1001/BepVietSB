package com.example.Bep_Viet.model;


import com.example.Bep_Viet.enums.MealTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plan_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meal_plan_id", "day_of_week", "meal_time"}) //1 bua chi co 1 mon an
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week",nullable = false)
    private com.example.Bep_Viet.enums.DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time",nullable = false)
    private MealTime mealTime;

    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}