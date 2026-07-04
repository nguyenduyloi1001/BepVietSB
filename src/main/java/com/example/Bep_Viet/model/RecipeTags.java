package com.example.Bep_Viet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_tags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(RecipeTagId.class)
public class RecipeTags {

    @Id
    @Column(name = "recipe_id")
    private Long recipeId;

    @Id
    @Column(name = "tag_id")
    private Long tagId;
}