package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.CategoryType;
import com.example.Bep_Viet.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    boolean existsBySlug(String slug);
    List<Category> findByType(CategoryType type);
    List<Category> findByIsActive(Boolean isActive);
    List<Category> findByTypeAndIsActive(CategoryType type, Boolean isActive);
    Optional<Category> findBySlug(String slug);

}
