package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.DayOfWeek;
import com.example.Bep_Viet.enums.MealTime;
import com.example.Bep_Viet.request.MealPlanItemRequest;
import com.example.Bep_Viet.request.MealPlanRequest;
import com.example.Bep_Viet.response.MealPlanResponse;
import com.example.Bep_Viet.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MealPlanResponse> create(
            @Valid @RequestBody MealPlanRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MealPlanResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(service.getById(id, userId));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MealPlanResponse>> getMyPlans(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MealPlanResponse> addItem(
            @PathVariable Long id,
            @Valid @RequestBody MealPlanItemRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(id, request, userId));
    }


    @PutMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MealPlanResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody MealPlanItemRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(service.updateItem(id, request, userId));
    }

    @DeleteMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long id,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam MealTime mealTime,
            @AuthenticationPrincipal Long userId) {
        service.removeItem(id, dayOfWeek, mealTime, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}