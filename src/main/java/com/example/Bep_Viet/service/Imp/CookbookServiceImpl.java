package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Cookbook;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.CookbookRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.CookBookRequest;
import com.example.Bep_Viet.response.CookBookResponse;
import com.example.Bep_Viet.response.RecipeResponse;
import com.example.Bep_Viet.service.CookbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CookbookServiceImpl implements CookbookService {
    private final CookbookRepository repository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    @Override
    public CookBookResponse create(CookBookRequest request, Long userId) {
        if (repository.existsByNameAndUserId(request.getName(),userId)){
            throw new AppException(ErrorCode.COOKBOOK_NAME_EXISTED);
        }
        User user = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        Cookbook cookbook = Cookbook.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.getIsPublic())
                .build();
        return mapToResponse(repository.save(cookbook));
    }

    @Override
    public CookBookResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public List<CookBookResponse> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public CookBookResponse update(Long id, CookBookRequest request, Long currentUserId) {
        Cookbook cookbook = findAndCheckOwner(id, currentUserId);

        if (!cookbook.getName().equals(request.getName()) &&
                repository.existsByNameAndUserIdAndIdNot(request.getName(), currentUserId, id)) {
            throw new AppException(ErrorCode.COOKBOOK_NAME_EXISTED);
        }

        cookbook.setName(request.getName());
        cookbook.setDescription(request.getDescription());
        cookbook.setIsPublic(request.getIsPublic());

        return mapToResponse(repository.save(cookbook));
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        repository.delete(findAndCheckOwner(id, currentUserId));
    }

    @Override
    public CookBookResponse addRecipe(Long cookbookId, Long recipeId, Long currentUserId) {
        Cookbook cookbook = findAndCheckOwner(cookbookId, currentUserId);

        if (cookbook.getRecipeIds().contains(recipeId)) {
            throw new AppException(ErrorCode.RECIPE_ALREADY_IN_COOKBOOK);
        }
        if (!recipeRepository.existsById(recipeId)) {
            throw new AppException(ErrorCode.RECIPE_NOT_FOUND);
        }

        cookbook.getRecipeIds().add(recipeId);
        return mapToResponse(repository.save(cookbook));
    }

    @Override
    public CookBookResponse removeRecipe(Long cookbookId, Long recipeId, Long currentUserId) {
        Cookbook cookbook = findAndCheckOwner(cookbookId, currentUserId);
        if (!cookbook.getRecipeIds().remove(recipeId)) {
            throw new AppException(ErrorCode.RECIPE_NOT_IN_COOKBOOK);
        }

        return mapToResponse(repository.save(cookbook));
    }

    //helper
    private Cookbook findById(Long id){
        return repository.findById(id).orElseThrow(()-> new AppException(ErrorCode.COOKBOOK_NOT_FOUND));
    }

    private Cookbook findAndCheckOwner(Long id, Long userId) {
        Cookbook cookbook = findById(id);
        if (!cookbook.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.COOKBOOK_FORBIDDEN);
        }
        return cookbook;
    }

    private CookBookResponse mapToResponse(Cookbook cookbook){
        List<RecipeResponse> recipes = recipeRepository.findAllById(cookbook.getRecipeIds()).stream()
                .map(r-> RecipeResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .imageUrl(r.getImageUrl())
                        .cookingTime(r.getCookingTime())
                        .servings(r.getServings())
                        .avgRating(r.getAvgRating())
                        .slug(r.getSlug())
                        .status(r.getStatus())
                        .build()).toList();
        return CookBookResponse.builder()
                .id(cookbook.getId())
                .userId(cookbook.getUser().getId())
                .userName(cookbook.getUser().getUsername())
                .name(cookbook.getName())
                .description(cookbook.getDescription())
                .isPublic(cookbook.getIsPublic())
                .createdAt(cookbook.getCreatedAt())
                .updatedAt(cookbook.getUpdatedAt())
                .recipes(recipes)
                .build();
    }
}
