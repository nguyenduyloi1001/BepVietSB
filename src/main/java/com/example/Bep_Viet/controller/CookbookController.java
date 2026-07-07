package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.CookBookRequest;
import com.example.Bep_Viet.response.CookBookResponse;
import com.example.Bep_Viet.service.CookbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cookbooks")
@RequiredArgsConstructor
public class CookbookController {

    private final CookbookService cookbookService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CookBookResponse> create(
            @RequestBody CookBookRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cookbookService.create(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CookBookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cookbookService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CookBookResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cookbookService.getByUserId(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CookBookResponse> update(
            @PathVariable Long id,
            @RequestBody CookBookRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(cookbookService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        cookbookService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cookbookId}/recipes/{recipeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CookBookResponse> addRecipe(
            @PathVariable Long cookbookId,
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(cookbookService.addRecipe(cookbookId, recipeId, userId));
    }

    @DeleteMapping("/{cookbookId}/recipes/{recipeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CookBookResponse> removeRecipe(
            @PathVariable Long cookbookId,
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(cookbookService.removeRecipe(cookbookId, recipeId, userId));
    }
}