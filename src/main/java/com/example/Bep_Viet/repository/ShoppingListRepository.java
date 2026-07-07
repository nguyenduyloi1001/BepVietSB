package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findByUserId(Long userId);
    Optional<ShoppingList> findByMealPlanId(Long mealPlanId);
}
