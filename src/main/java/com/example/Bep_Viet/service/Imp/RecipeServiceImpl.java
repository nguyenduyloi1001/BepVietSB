package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Category;
import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.*;
import com.example.Bep_Viet.request.RecipeRequest;
import com.example.Bep_Viet.response.RecipeIngredientResponse;
import com.example.Bep_Viet.response.RecipeResponse;
import com.example.Bep_Viet.response.RecipeStepResponse;
import com.example.Bep_Viet.service.RecipeIngredientService;
import com.example.Bep_Viet.service.RecipeService;
import com.example.Bep_Viet.service.RecipeStepService;
import com.example.Bep_Viet.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeIngredientService recipeIngredientService;
    private final RecipeStepService recipeStepService;
    private final RatingRepository ratingRepository;
    private final MealPlanItemRepository mealPlanItemRepository;
    private final ShareRepository   shareRepository;
    private static final int CHEF_PRIORITY_DAYS = 30;

    @Override
    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request, Long userId) {
        if (repository.existsByNameAndUserId(request.getName(), userId)) {
            throw new AppException(ErrorCode.RECIPE_NAME_EXISTED);
        }

        String slug = SlugUtil.toSlug(request.getName());
        if (repository.existsBySlug(slug)) {
            slug = slug + "-" + userId;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Recipe recipe = Recipe.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .cookingTime(request.getCookingTime())
                .servings(request.getServings())
                .slug(slug)
                .status(RecipeStatus.PENDING)
                .user(user)
                .difficulty(findCategoryById(request.getDifficultyId()))
                .region(findCategoryById(request.getRegionId()))
                .occasion(findCategoryById(request.getOccasionId()))
                .dishType(findCategoryById(request.getDishTypeId()))
                .build();

        Recipe saveRecipe = repository.save(recipe);
        recipeIngredientService.addAll(saveRecipe, request.getIngredients());
        recipeStepService.addAll(saveRecipe,request.getSteps());
        return mapToResponse(saveRecipe);
    }

    @Override
    @Transactional
    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = findRecipeById(id);
        repository.incrementViewCount(id);
        return mapToResponse(recipe);
    }

    @Override
    @Transactional
    public RecipeResponse getBySlug(String slug) {
        Recipe recipe= repository.findBySlug(slug).orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        repository.incrementViewCount(recipe.getId());
        return mapToResponse(recipe);
    }

    @Override
    public List<RecipeResponse> getAllRecipe() {
        return repository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<RecipeResponse> search(String keyword, Long dishTypeId, Long regionId, Long difficultyId) {
        return repository.searchRecipes(keyword, dishTypeId, regionId, difficultyId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public RecipeResponse updateRecipe(Long id, RecipeRequest request, Long currentUserId) {
        Recipe recipe = findRecipeById(id);

        if (!recipe.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.RECIPE_FORBIDDEN);
        }

        if (!recipe.getName().equals(request.getName())) {
            if (repository.existsByNameAndUserIdAndIdNot(request.getName(), currentUserId, id)) {
                throw new AppException(ErrorCode.RECIPE_NAME_EXISTED);
            }
            String newSlug = SlugUtil.toSlug(request.getName());
            if (repository.existsBySlugAndIdNot(newSlug, id)) {
                newSlug = newSlug + "-" + currentUserId;
            }
            recipe.setSlug(newSlug);
        }

        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setServings(request.getServings());
        recipe.setDishType(findCategoryById(request.getDishTypeId()));
        recipe.setRegion(findCategoryById(request.getRegionId()));
        recipe.setOccasion(findCategoryById(request.getOccasionId()));
        recipe.setDifficulty(findCategoryById(request.getDifficultyId()));
        recipe.setStatus(RecipeStatus.PENDING);

        Recipe saved = repository.save(recipe);


        recipeIngredientService.deleteByRecipeId(saved.getId());
        recipeIngredientService.addAll(saved, request.getIngredients());

        recipeStepService.deleteByRecipeId(saved.getId());
        recipeStepService.addAll(saved, request.getSteps());

        return mapToResponse(saved);
    }

    @Transactional
    @Override
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Recipe recipe = findRecipeById(id);
        if(!isAdmin && !recipe.getUser().getId().equals(currentUserId)){
            throw new AppException(ErrorCode.RECIPE_FORBIDDEN);
        }
        shareRepository.deleteByTargetIdAndTargetType(id, TargetType.RECIPE);
        mealPlanItemRepository.deleteByRecipeId(id);
        ratingRepository.deleteByRecipeId(id);
        repository.delete(recipe);
    }

    @Override
    public RecipeResponse approve(Long id) {
        Recipe recipe = findRecipeById(id);
        recipe.setStatus(RecipeStatus.PUBLISHED);
        return mapToResponse(repository.save(recipe));
    }

    @Override
    public RecipeResponse reject(Long id) {
        Recipe recipe = findRecipeById(id);
        recipe.setStatus(RecipeStatus.REJECTED);
        return mapToResponse(repository.save(recipe));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeResponse> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<RecipeResponse> getByStatus(RecipeStatus status) {
        return repository.findByStatus(status).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<RecipeResponse> getAllRecipeSorted() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(CHEF_PRIORITY_DAYS);
        return repository.findAllSortedByRoleAndDate(cutoffDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    //helper
    private Recipe findRecipeById(Long id){
        return repository.findById(id).orElseThrow(()->new AppException(ErrorCode.RECIPE_NOT_FOUND));
    }

    private Category findCategoryById(Long id){
        if (id == null)
            return null;
        return categoryRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private RecipeResponse mapToResponse(Recipe recipe){
        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .imageUrl(recipe.getImageUrl())
                .cookingTime(recipe.getCookingTime())
                .servings(recipe.getServings())
                .viewCount(recipe.getViewCount())
                .avgRating(recipe.getAvgRating())
                .slug(recipe.getSlug())
                .status(recipe.getStatus())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .userId(recipe.getUser() == null ? null : recipe.getUser().getId())
                .userName(recipe.getUser() == null ? null : recipe.getUser().getUsername())
                .userAvatar(recipe.getUser() == null ? null : recipe.getUser().getAvatarUrl())
                .userRole(recipe.getUser() == null ? null : recipe.getUser().getRole().name())
                .dishTypeId(recipe.getDishType() == null ? null : recipe.getDishType().getId())
                .dishTypeName(recipe.getDishType() == null ? null : recipe.getDishType().getName())
                .regionId(recipe.getRegion() == null ? null : recipe.getRegion().getId())
                .regionName(recipe.getRegion() == null ? null : recipe.getRegion().getName())
                .occasionId(recipe.getOccasion() == null ? null : recipe.getOccasion().getId())
                .occasionName(recipe.getOccasion() == null ? null : recipe.getOccasion().getName())
                .difficultyId(recipe.getDifficulty() == null ? null : recipe.getDifficulty().getId())
                .difficultyName(recipe.getDifficulty() == null ? null : recipe.getDifficulty().getName())
                .ingredients(recipe.getIngredients() == null ? null :
                        recipe.getIngredients().stream()
                                .map(ri -> RecipeIngredientResponse.builder()
                                        .id(ri.getId())
                                        .ingredientId(ri.getIngredient().getId())
                                        .ingredientName(ri.getIngredient().getName())
                                        .quantity(ri.getQuantity())
                                        .unit(ri.getUnit())
                                        .note(ri.getNote())
                                        .build())
                                .toList())
                .steps(recipe.getSteps() == null ? null :  // thêm vào đây
                        recipe.getSteps().stream()
                                .map(s -> RecipeStepResponse.builder()
                                        .id(s.getId())
                                        .stepNumber(s.getStepNumber())
                                        .instruction(s.getInstruction())
                                        .imageUrl(s.getImageUrl())
                                        .videoUrl(s.getVideoUrl())
                                        .timerMinutes(s.getTimerMinutes())
                                        .build())
                                .toList())
                .build();
    }
}