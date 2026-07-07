package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.IngredientRequest;
import com.example.Bep_Viet.response.IngredientResponse;

import java.util.List;

public interface IngredientService {
    IngredientResponse createIngredient(IngredientRequest request);

    IngredientResponse getIngredientById(Long id);

    IngredientResponse getIngredientBySlug(String slug);

    List<IngredientResponse> getAllIngredient(String keyword, Long categoryId);

    IngredientResponse updateIngredient(Long id , IngredientRequest request);

    void delete(Long id);

}
