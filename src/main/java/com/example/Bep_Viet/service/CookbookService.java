package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.CookBookRequest;
import com.example.Bep_Viet.response.CookBookResponse;

import java.util.List;

public interface CookbookService {
    CookBookResponse create(CookBookRequest request, Long userId);
    CookBookResponse getById(Long id);
    List<CookBookResponse> getByUserId(Long userId);
    CookBookResponse update(Long id, CookBookRequest request, Long currentUserId);
    void delete(Long id, Long currentUserId);
    CookBookResponse addRecipe(Long cookbookId, Long recipeId, Long currentUserId);
    CookBookResponse removeRecipe(Long cookbookId, Long recipeId, Long currentUserId);
}