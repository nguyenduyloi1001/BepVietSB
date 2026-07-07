package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.RecipeRequest;
import com.example.Bep_Viet.response.RecipeResponse;
import com.example.Bep_Viet.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    private final UserRepository userRepository;
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecipeResponse> createRecipe(
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeService.createRecipe(request, userId));
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecipeResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(recipeService.getByUserId(userId));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<RecipeResponse> getRecipeBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(recipeService.getBySlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAllRecipe() {
        return ResponseEntity.ok(recipeService.getAllRecipeSorted());
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long dishTypeId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long difficultyId) {
        return ResponseEntity.ok(recipeService.search(keyword, dishTypeId, regionId, difficultyId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request, userId));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'CHEF', 'BLOGGER')")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {


        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        recipeService.delete(id, userId, isAdmin);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.reject(id));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RecipeResponse>> getByStatus(@RequestParam RecipeStatus status) {
        return ResponseEntity.ok(recipeService.getByStatus(status));
    }
}