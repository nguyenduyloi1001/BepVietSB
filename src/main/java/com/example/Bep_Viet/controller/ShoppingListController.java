package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.response.ShoppingListItemResponse;
import com.example.Bep_Viet.response.ShoppingListResponse;
import com.example.Bep_Viet.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {
    private final ShoppingListService shoppingListService;

    // Generate từ meal plan
    @PostMapping("/generate/{mealPlanId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingListResponse> generate(
            @PathVariable Long mealPlanId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shoppingListService.generateFromMealPlan(mealPlanId, userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingListResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shoppingListService.getById(id));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShoppingListResponse>> getMyLists(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(shoppingListService.getByUserId(userId));
    }

    // Tick/untick nguyên liệu đã mua
    @PatchMapping("/items/{itemId}/toggle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingListItemResponse> toggleCheck(@PathVariable Long itemId) {
        return ResponseEntity.ok(shoppingListService.toggleCheck(itemId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {  // thêm userId
        shoppingListService.delete(id, userId);       // truyền xuống service
        return ResponseEntity.noContent().build();
    }
}