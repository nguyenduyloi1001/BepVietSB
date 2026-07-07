package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Rating;
import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.RatingRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.RatingRequest;
import com.example.Bep_Viet.response.RatingResponse;
import com.example.Bep_Viet.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    @Override
    public RatingResponse rate(RatingRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
        Recipe recipe = recipeRepository.findById(request.getRecipeId()).orElseThrow(()->new AppException(ErrorCode.RECIPE_NOT_FOUND));
        Rating rating = ratingRepository.findByUserIdAndRecipeId(userId, request.getRecipeId()).orElse(Rating.builder()
                .user(user)
                .recipe(recipe)
                .build());
        rating.setStars(request.getStars());
        ratingRepository.save(rating);

        // Cập nhật avgRating vào recipe
        Double avg = ratingRepository.getAverageStars(request.getRecipeId());
        recipe.setAvgRating(avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO);
        recipeRepository.save(recipe);

        return mapToResponse(rating);
    }

    @Override
    public List<RatingResponse> getByRecipeId(Long recipeId) {
        return ratingRepository.findByRecipeId(recipeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Double getAverageStars(Long recipeId) {
        Double avg = ratingRepository.getAverageStars(recipeId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Override
    public long countByRecipeId(Long recipeId) {
        return ratingRepository.countByRecipeId(recipeId);
    }

    @Override
    public void delete(Long recipeId, Long userId) {
        Rating rating = ratingRepository
                .findByUserIdAndRecipeId(userId, recipeId)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_FOUND));
        ratingRepository.delete(rating);
    }
    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .userName(rating.getUser().getUsername())
                .recipeId(rating.getRecipe().getId())
                .stars(rating.getStars())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}