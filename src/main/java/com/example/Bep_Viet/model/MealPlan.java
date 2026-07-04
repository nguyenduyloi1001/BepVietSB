package com.example.Bep_Viet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meal_plans")
@Builder
public class MealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    private String name;

    @Column(name = "week_start_date",nullable = false)
    private LocalDate weekStartDate;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL,orphanRemoval = true) // tao không quản lý, thằng kia quản lý.
    private List<MealPlanItem> items;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


}