package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.RatingRequest;
import com.example.Bep_Viet.response.RatingResponse;

import java.util.List;

public interface RatingService {
    RatingResponse rate(RatingRequest request, Long userId);
    List<RatingResponse> getByRecipeId(Long recipeId);
    Double getAverageStars(Long recipeId);
    long countByRecipeId(Long recipeId);
    void delete(Long recipeId,Long userId);
}
