package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.RecipeIngredientRequest;
import com.example.Bep_Viet.response.RecipeIngredientResponse;
import com.example.Bep_Viet.service.RecipeIngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipe-ingredients")
@RequiredArgsConstructor
public class RecipeIngredientController {
    private final RecipeIngredientService recipeIngredientService;

    @PostMapping("/recipe/{recipeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecipeIngredientResponse> add(
            @PathVariable Long recipeId,
            @Valid @RequestBody RecipeIngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeIngredientService.add(recipeId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecipeIngredientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecipeIngredientRequest request) {
        return ResponseEntity.ok(recipeIngredientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeIngredientService.delete(id);
        return ResponseEntity.noContent().build();
    }


}