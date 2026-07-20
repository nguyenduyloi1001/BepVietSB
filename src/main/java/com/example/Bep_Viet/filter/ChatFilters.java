package com.example.Bep_Viet.filter;

import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ChatFilters {

    private List<String> ingredients;

    @JsonProperty("excluded_ingredients")
    private List<String> excludedIngredients; // dung de loai bo user k muon ăn

    private String diet;

    @JsonProperty("cooking_time_max")
    private Integer cookingTimeMax;

    // "top_rated" | "most_liked" | null
    @JsonProperty("sort_by")
    private String sortBy;

    // "bac" | "trung" | "nam" | null
    private String region;
}