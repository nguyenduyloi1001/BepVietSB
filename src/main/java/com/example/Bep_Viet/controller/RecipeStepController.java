package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.RecipeStepRequest;
import com.example.Bep_Viet.response.RecipeStepResponse;
import com.example.Bep_Viet.service.RecipeStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-steps")
@RequiredArgsConstructor
public class RecipeStepController {
    private final RecipeStepService recipeStepService;

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<RecipeStepResponse>> getByRecipeId(@PathVariable Long recipeId) {
        return ResponseEntity.ok(recipeStepService.getByRecipeId(recipeId));
    }

    @PostMapping("/recipe/{recipeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecipeStepResponse> add(
            @PathVariable Long recipeId,
            @Valid @RequestBody RecipeStepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeStepService.add(recipeId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecipeStepResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecipeStepRequest request) {
        return ResponseEntity.ok(recipeStepService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeStepService.delete(id);
        return ResponseEntity.noContent().build();
    }
}