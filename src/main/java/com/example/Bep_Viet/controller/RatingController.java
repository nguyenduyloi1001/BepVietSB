package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.RatingRequest;
import com.example.Bep_Viet.response.RatingResponse;
import com.example.Bep_Viet.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RatingResponse> rate(
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ratingService.rate(request, userId));
    }

    // Public - ai cũng xem được
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<RatingResponse>> getByRecipe(@PathVariable Long recipeId) {
        return ResponseEntity.ok(ratingService.getByRecipeId(recipeId));
    }

    @GetMapping("/recipe/{recipeId}/average")
    public ResponseEntity<Double> getAverage(@PathVariable Long recipeId) {
        return ResponseEntity.ok(ratingService.getAverageStars(recipeId));
    }

    @GetMapping("/recipe/{recipeId}/count")
    public ResponseEntity<Long> getCount(@PathVariable Long recipeId) {
        return ResponseEntity.ok(ratingService.countByRecipeId(recipeId));
    }

    // Xoá rating của chính mình
    @DeleteMapping("/recipe/{recipeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long userId) {
        ratingService.delete(recipeId, userId);
        return ResponseEntity.noContent().build();
    }
}