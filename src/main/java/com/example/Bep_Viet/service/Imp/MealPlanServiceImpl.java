package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.DayOfWeek;
import com.example.Bep_Viet.enums.MealTime;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.MealPlan;
import com.example.Bep_Viet.model.MealPlanItem;
import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.MealPlanItemRepository;
import com.example.Bep_Viet.repository.MealPlanRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.MealPlanItemRequest;
import com.example.Bep_Viet.request.MealPlanRequest;
import com.example.Bep_Viet.response.MealPlanItemResponse;
import com.example.Bep_Viet.response.MealPlanResponse;
import com.example.Bep_Viet.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPlanServiceImpl implements MealPlanService {
    private final MealPlanRepository repository;
    private final MealPlanItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    @Override
    public MealPlanResponse create(MealPlanRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MealPlan mealPlan = MealPlan.builder()
                .user(user)
                .name(request.getName())
                .weekStartDate(request.getWeekStartDate())
                .build();

        return mapToResponse(repository.save(mealPlan));
    }

    @Override
    public MealPlanResponse getById(Long id, Long currentUserId) {
        MealPlan mealPlan = findById(id);
        checkOwner(mealPlan, currentUserId);
        return mapToResponse(mealPlan);
    }

    @Override
    public List<MealPlanResponse> getByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MealPlanResponse addItem(Long mealPlanId, MealPlanItemRequest request, Long currentUserId) {
        MealPlan mealPlan = findById(mealPlanId);
        checkOwner(mealPlan, currentUserId);

        if (itemRepository.existsByMealPlanIdAndDayOfWeekAndMealTime(
                mealPlanId, request.getDayOfWeek(), request.getMealTime())) {
            throw new AppException(ErrorCode.MEAL_PLAN_ITEM_EXISTED);
        }

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));

        MealPlanItem item = MealPlanItem.builder()
                .mealPlan(mealPlan)
                .recipe(recipe)
                .dayOfWeek(request.getDayOfWeek())
                .mealTime(request.getMealTime())
                .build();

        itemRepository.save(item);
        return mapToResponse(findById(mealPlanId));
    }

    @Transactional
    @Override
    public MealPlanResponse updateItem(Long mealPlanId, MealPlanItemRequest request, Long currentUserId) {
        MealPlan mealPlan = findById(mealPlanId);
        checkOwner(mealPlan, currentUserId);

        MealPlanItem item = itemRepository
                .findByMealPlanIdAndDayOfWeekAndMealTime(
                        mealPlanId, request.getDayOfWeek(), request.getMealTime())
                .orElseThrow(() -> new AppException(ErrorCode.MEAL_PLAN_NOT_FOUND));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));

        item.setRecipe(recipe); // update in place, no delete+insert

        itemRepository.save(item);
        return mapToResponse(findById(mealPlanId));
    }

    @Override
    public void removeItem(Long mealPlanId, DayOfWeek dayOfWeek, MealTime mealTime, Long currentUserId) {
        MealPlan mealPlan = findById(mealPlanId);
        checkOwner(mealPlan, currentUserId);

        // Thêm check này
        if (!itemRepository.existsByMealPlanIdAndDayOfWeekAndMealTime(
                mealPlanId, dayOfWeek, mealTime)) {
            throw new AppException(ErrorCode.MEAL_PLAN_NOT_FOUND);
        }

        itemRepository.deleteByMealPlanIdAndDayOfWeekAndMealTime(mealPlanId, dayOfWeek, mealTime);
    }

    @Override
    @Transactional
    public void delete(Long id, Long currentUserId) {
        MealPlan mealPlan = findById(id);
        checkOwner(mealPlan, currentUserId);
        repository.delete(mealPlan);
    }
    private MealPlan findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MEAL_PLAN_NOT_FOUND));
    }

    private void checkOwner(MealPlan mealPlan, Long currentUserId) {
        if (!mealPlan.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private MealPlanResponse mapToResponse(MealPlan mp) {
        return MealPlanResponse.builder()
                .id(mp.getId())
                .userId(mp.getUser().getId())
                .name(mp.getName())
                .weekStartDate(mp.getWeekStartDate())
                .items(mp.getItems() == null ? List.of() :
                        mp.getItems().stream().map(this::mapItemToResponse).toList())
                .createdAt(mp.getCreatedAt())
                .build();
    }

    private MealPlanItemResponse mapItemToResponse(MealPlanItem item) {
        return MealPlanItemResponse.builder()
                .id(item.getId())
                .recipeId(item.getRecipe().getId())
                .recipeName(item.getRecipe().getName())
                .recipeImageUrl(item.getRecipe().getImageUrl())
                .dayOfWeek(item.getDayOfWeek())
                .mealTime(item.getMealTime())
                .build();
    }
}
