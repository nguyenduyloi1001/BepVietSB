package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.model.RecipeStep;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.RecipeStepRepository;
import com.example.Bep_Viet.request.RecipeStepRequest;
import com.example.Bep_Viet.response.RecipeStepResponse;
import com.example.Bep_Viet.service.RecipeStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecipeStepServiceImpl implements RecipeStepService {
    private final RecipeStepRepository repository;
    private final RecipeRepository recipeRepository;
    @Override
    public void addAll(Recipe recipe, List<RecipeStepRequest> requests) {
        for(RecipeStepRequest request : requests){
            repository.save(RecipeStep.builder()
                    .recipe(recipe)
                    .stepNumber(request.getStepNumber())
                    .instruction(request.getInstruction())
                    .imageUrl(request.getImageUrl())
                    .videoUrl(request.getVideoUrl())
                    .timerMinutes(request.getTimerMinutes())
                    .build());
        }
    }

    @Override
    public RecipeStepResponse add(Long recipeId, RecipeStepRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()->new AppException(ErrorCode.RECIPE_NOT_FOUND));
        if (repository.existsByRecipeIdAndStepNumber(recipeId, request.getStepNumber())) {
            throw new AppException(ErrorCode.STEP_NUMBER_EXISTED);
        }
        return mapToResponse(repository.save(RecipeStep.builder()
                .recipe(recipe)
                .stepNumber(request.getStepNumber())
                .instruction(request.getInstruction())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .timerMinutes(request.getTimerMinutes())
                .build()));
    }

    @Override
    public RecipeStepResponse update(Long id, RecipeStepRequest request) {
        RecipeStep step = findById(id);

        if (request.getInstruction() != null) step.setInstruction(request.getInstruction());
        if (request.getImageUrl() != null) step.setImageUrl(request.getImageUrl());
        if (request.getVideoUrl() != null) step.setVideoUrl(request.getVideoUrl());
        if (request.getTimerMinutes() != null) step.setTimerMinutes(request.getTimerMinutes());

        return mapToResponse(repository.save(step));
    }

    @Override
    public void delete(Long id) {
        RecipeStep recipeStep = findById(id);
        repository.delete(recipeStep);
    }

    @Override
    public List<RecipeStepResponse> getByRecipeId(Long recipeId) {
        return repository.findByRecipeIdOrderByStepNumberAsc(recipeId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public void deleteByRecipeId(Long recipeId) {
        repository.deleteByRecipeId(recipeId);
    }

    //help
    private RecipeStep findById(Long id){
        return repository.findById(id).orElseThrow(()-> new AppException(ErrorCode.RECIPE_STEP_NOT_FOUND));
    }

    private RecipeStepResponse mapToResponse(RecipeStep recipeStep){
        return RecipeStepResponse.builder()
                .id(recipeStep.getId())
                .stepNumber(recipeStep.getStepNumber())
                .instruction(recipeStep.getInstruction())
                .imageUrl(recipeStep.getImageUrl())
                .videoUrl(recipeStep.getVideoUrl())
                .timerMinutes(recipeStep.getTimerMinutes())
                .build();
    }
}