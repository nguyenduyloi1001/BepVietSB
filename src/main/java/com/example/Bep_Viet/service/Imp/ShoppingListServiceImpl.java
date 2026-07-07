package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.*;
import com.example.Bep_Viet.repository.MealPlanRepository;
import com.example.Bep_Viet.repository.ShoppingListItemRepository;
import com.example.Bep_Viet.repository.ShoppingListRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.response.ShoppingListItemResponse;
import com.example.Bep_Viet.response.ShoppingListResponse;
import com.example.Bep_Viet.service.ShoppingListService;
import com.example.Bep_Viet.util.UnitNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShoppingListServiceImpl implements ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ShoppingListResponse generateFromMealPlan(Long mealPlanId, Long userId) {
        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.MEAL_PLAN_NOT_FOUND)); // fix: sửa lại error code đúng

        // Kiểm tra đã generate chưa
        if (shoppingListRepository.findByMealPlanId(mealPlanId).isPresent()) {
            throw new AppException(ErrorCode.SHOPPING_LIST_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ShoppingList shoppingList = ShoppingList.builder()
                .user(user)
                .mealPlan(mealPlan)
                .name("Đi chợ tuần " + mealPlan.getWeekStartDate())
                .build();
        ShoppingList saved = shoppingListRepository.save(shoppingList);

        // ── Gộp nguyên liệu có normalize unit ────────────────────────────────
        Map<String, BigDecimal> quantityMap   = new LinkedHashMap<>();
        Map<String, Ingredient> ingredientMap = new LinkedHashMap<>();
        Map<String, String>     unitMap       = new LinkedHashMap<>();

        for (MealPlanItem mealPlanItem : mealPlan.getItems()) {
            for (RecipeIngredient ri : mealPlanItem.getRecipe().getIngredients()) {

                String baseUnit = UnitNormalizer.baseUnit(ri.getUnit());   // "g", "ml", "củ"...
                BigDecimal baseQty = UnitNormalizer.toBase(ri.getQuantity(), ri.getUnit());

                // Key = ingredientId + đơn vị cơ bản → tránh trùng nhóm khác nhau
                String key = ri.getIngredient().getId() + "_" + baseUnit;

                quantityMap.merge(key, baseQty, BigDecimal::add);
                ingredientMap.putIfAbsent(key, ri.getIngredient());
                unitMap.putIfAbsent(key, baseUnit);
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        List<ShoppingListItem> items = new ArrayList<>();
        for (String key : quantityMap.keySet()) {
            ShoppingListItem item = ShoppingListItem.builder()
                    .shoppingList(saved)
                    .ingredient(ingredientMap.get(key))
                    .quantity(quantityMap.get(key))
                    .unit(unitMap.get(key))
                    .isChecked(false)
                    .build();
            items.add(item);
        }
        shoppingListItemRepository.saveAll(items);

        saved.setItems(items);
        return mapToResponse(saved);
    }

    @Override
    public ShoppingListResponse getById(Long id) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOPPING_LIST_NOT_FOUND));
        return mapToResponse(shoppingList);
    }

    @Override
    public List<ShoppingListResponse> getByUserId(Long userId) {
        List<ShoppingList> lists = shoppingListRepository.findByUserId(userId);
        List<ShoppingListResponse> responses = new ArrayList<>();
        for (ShoppingList sl : lists) {
            responses.add(mapToResponse(sl));
        }
        return responses;
    }

    @Override
    @Transactional
    public ShoppingListItemResponse toggleCheck(Long itemId) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOPPING_LIST_NOT_FOUND));
        item.setIsChecked(!item.getIsChecked());
        ShoppingListItem saved = shoppingListItemRepository.save(item);
        return mapItemToResponse(saved);
    }

    @Override
    public void delete(Long id, Long userId) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOPPING_LIST_NOT_FOUND));
        if (!shoppingList.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        shoppingListRepository.delete(shoppingList);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private ShoppingListResponse mapToResponse(ShoppingList sl) {
        List<ShoppingListItemResponse> itemResponses = new ArrayList<>();
        for (ShoppingListItem item : sl.getItems()) {
            itemResponses.add(mapItemToResponse(item));
        }
        return ShoppingListResponse.builder()
                .id(sl.getId())
                .name(sl.getName())
                .mealPlanId(sl.getMealPlan() != null ? sl.getMealPlan().getId() : null)
                .items(itemResponses)
                .createdAt(sl.getCreatedAt())
                .build();
    }

    private ShoppingListItemResponse mapItemToResponse(ShoppingListItem item) {
        return ShoppingListItemResponse.builder()
                .id(item.getId())
                .ingredientId(item.getIngredient().getId())
                .ingredientName(item.getIngredient().getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .isChecked(item.getIsChecked())
                .build();
    }
}