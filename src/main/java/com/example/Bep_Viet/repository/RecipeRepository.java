package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {
    Optional<Recipe> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    boolean existsByNameAndUserId(String name, Long userId);

    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Recipe> findByStatus(RecipeStatus status);

    List<Recipe> findByUserId(Long userId);

    List<Recipe> findByUserIdAndStatus(Long userId,RecipeStatus status);
    @Query("""
        SELECT r FROM Recipe r
        WHERE (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
               OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:dishTypeId IS NULL OR r.dishType.id = :dishTypeId)
          AND (:regionId IS NULL OR r.region.id = :regionId)
          AND (:difficultyId IS NULL OR r.difficulty.id = :difficultyId)
          AND r.status = 'PUBLISHED'
        """)
    List<Recipe> searchRecipes(
            @Param("keyword") String keyword,
            @Param("dishTypeId") Long dishTypeId,
            @Param("regionId") Long regionId,
            @Param("difficultyId") Long difficultyId);

    @Modifying
    @Query("UPDATE Recipe r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") Long id);

    //AI
    @Query(value = """
    SELECT
        r.id,
        r.name,
        r.image_url,
        r.cooking_time,
        r.avg_rating,
        GROUP_CONCAT(
            CONCAT(i.name, ':', COALESCE(ri.quantity,''), ':', COALESCE(ri.unit,''))
            SEPARATOR '|'
        ) AS ingredients_str,
        COUNT(DISTINCT CASE
            WHEN LOWER(i.name) IN (:ingredients) THEN ri.id
        END) AS match_count
    FROM recipe r
    JOIN recipe_ingredient ri ON r.id = ri.recipe_id
    JOIN ingredient i ON ri.ingredient_id = i.id
    WHERE r.status = 'PUBLISHED'
    GROUP BY r.id, r.name, r.image_url, r.cooking_time, r.avg_rating
    HAVING match_count > 0
    ORDER BY match_count DESC, r.avg_rating DESC
    LIMIT 20
    """, nativeQuery = true)
    List<Object[]> findCandidatesByIngredients(
            @Param("ingredients") List<String> ingredients
    );

    @Query("""
    SELECT r FROM Recipe r
    JOIN r.user u
    WHERE r.status = 'PUBLISHED'
    ORDER BY
        CASE WHEN u.role = 'CHEF' AND r.createdAt >= :cutoffDate THEN 0 ELSE 1 END ASC,
        r.createdAt DESC
    """)
    List<Recipe> findAllSortedByRoleAndDate(@Param("cutoffDate") LocalDateTime cutoffDate);

}
