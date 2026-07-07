package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.IngredientRequest;
import com.example.Bep_Viet.response.IngredientResponse;
import com.example.Bep_Viet.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.createIngredient(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> getIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<IngredientResponse> getIngredientBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ingredientService.getIngredientBySlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAllIngredient(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(ingredientService.getAllIngredient(keyword, categoryId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}