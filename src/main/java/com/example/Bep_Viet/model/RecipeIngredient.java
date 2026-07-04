package com.example.Bep_Viet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id",nullable = false)
    private Ingredient ingredient;

    @Column(precision = 10,scale = 2)//pre la tong so chu so , scale so chieu sao thap phan
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit; // don vi g / kg

    @Column(length = 500)
    private String note;
}