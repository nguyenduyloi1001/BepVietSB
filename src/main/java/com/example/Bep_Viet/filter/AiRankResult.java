package com.example.Bep_Viet.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AiRankResult {
    private List<RankItem> suggestions;

    @Data
    public static class RankItem {
        @JsonProperty("recipe_id")
        private Long recipeId;

        @JsonProperty("match_score")
        private Integer matchScore;

        @JsonProperty("missing_ingredients")
        private List<String> missingIngredients;

        private String reason;
    }
}