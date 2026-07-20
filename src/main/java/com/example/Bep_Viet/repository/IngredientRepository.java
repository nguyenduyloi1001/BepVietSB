package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient,Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    Optional<Ingredient> findBySlug(String slug);

    @Query("""
        SELECT i FROM Ingredient i
        WHERE (:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR i.category.id = :categoryId)
        """)
    List<Ingredient> searchIngredients(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId);

    List<Ingredient> findByNameContainingIgnoreCase(String keyword);


    Optional<Ingredient> findByNameIgnoreCase(String name);

    // MỚI: dùng cho chat AI match theo TỪ ĐẦU TIÊN của tên nguyên liệu
    // (vd "cá" khớp "Cá hồi", "Cá thu"nhưng k khớp "Cà rốt", "Cải xanh"
    // vì khác từ đầu, tránh lỗi fold dấu thanh điệu của MySQL collation)
    @Query(value = """
        SELECT * FROM ingredient
        WHERE LOWER(name) = LOWER(:keyword)
           OR LOWER(name) LIKE CONCAT(LOWER(:keyword), ' %')
        """, nativeQuery = true)
    List<Ingredient> findByNameStartingWithWord(@Param("keyword") String keyword);
}