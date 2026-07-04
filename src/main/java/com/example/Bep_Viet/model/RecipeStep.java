package com.example.Bep_Viet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
public class RecipeStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;

    @Column(name = "step_number",nullable = false)
    private Integer stepNumber;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String instruction;//mo ta cac buoc

    @Column(name = "image_url",length = 500)
    private String imageUrl;

    @Column(name = "video_url",length = 500)
    private String videoUrl;

    @Column(name = "time_minutes")
    private Integer timerMinutes;
}
