package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ChatResponse {

    private Long sessionId;
    private String replyText;
    private List<SuggestionItem> suggestions;

    @Data
    @Builder
    public static class SuggestionItem {
        private Long recipeId;
        private String slug;
        private String title;
        private String thumbnail;
        private BigDecimal avgRating;
        private Integer cookingTime;
        private Integer matchScore;
        private List<String> missingIngredients;
        private String aiReason;
    }
}