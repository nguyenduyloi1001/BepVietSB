package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.model.Recipe;
import org.springframework.data.domain.Pageable;
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
    // ⭐ SỬA: thêm r.slug ở vị trí cột thứ 2 (ngay sau id) để trả về cho frontend
    // build URL /recipes/:slug. LƯU Ý: việc này làm lệch index tất cả cột phía sau
    // so với bản cũ - đã đồng bộ lại trong AiChatServiceImpl.
    @Query(value = """
    SELECT
        r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating,
        GROUP_CONCAT(
            CONCAT(i.name, ':', COALESCE(ri.quantity,''), ':', COALESCE(ri.unit,''))
            SEPARATOR '|'
        ) AS ingredients_str,
        COUNT(DISTINCT CASE WHEN LOWER(i.name) IN (:ingredients) THEN ri.id END) AS match_count
    FROM recipe r
    JOIN recipe_ingredient ri ON r.id = ri.recipe_id
    JOIN ingredient i ON ri.ingredient_id = i.id
    LEFT JOIN categories dt ON r.dish_type_id = dt.id
    LEFT JOIN categories rg ON r.region_id = rg.id
    WHERE r.status = 'PUBLISHED'
      AND (
            :dietMode = 'ANY'
            OR (:dietMode = 'CHAY' AND dt.slug = 'mon-chay')
            OR (:dietMode = 'MAN'  AND (dt.slug IS NULL OR dt.slug != 'mon-chay'))
          )
      AND (
            :regionMode = 'ANY'
            OR (:regionMode = 'BAC'   AND rg.slug = 'mien-bac')
            OR (:regionMode = 'TRUNG' AND rg.slug = 'mien-trung')
            OR (:regionMode = 'NAM'   AND rg.slug = 'mien-nam')
          )
      AND (:maxTime IS NULL OR r.cooking_time <= :maxTime)
    GROUP BY r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating
    HAVING match_count > 0
    ORDER BY match_count DESC, r.avg_rating DESC
    LIMIT 20
    """, nativeQuery = true)
    List<Object[]> findCandidatesByIngredients(
            @Param("ingredients") List<String> ingredients,
            @Param("dietMode") String dietMode,
            @Param("regionMode") String regionMode,
            @Param("maxTime") Integer maxTime
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

    // =========================================================
    // ⭐ Cho tính năng "Dành cho bạn" ở trang chủ
    // Tìm recipe id khớp với top ingredient user hay hỏi, có lọc diet
    // (Giữ nguyên - chỗ này chỉ trả về id, không cần slug vì dùng nội bộ)
    // =========================================================
    @Query(value = """
        SELECT r.id
        FROM recipe r
        JOIN recipe_ingredient ri ON r.id = ri.recipe_id
        JOIN ingredient i ON ri.ingredient_id = i.id
        LEFT JOIN categories dt ON r.dish_type_id = dt.id
        WHERE r.status = 'PUBLISHED'
          AND (
                :dietMode = 'ANY'
                OR (:dietMode = 'CHAY' AND dt.slug = 'mon-chay')
                OR (:dietMode = 'MAN'  AND (dt.slug IS NULL OR dt.slug != 'mon-chay'))
              )
        GROUP BY r.id
        HAVING COUNT(DISTINCT CASE WHEN LOWER(i.name) IN (:ingredients) THEN ri.id END) > 0
        ORDER BY COUNT(DISTINCT CASE WHEN LOWER(i.name) IN (:ingredients) THEN ri.id END) DESC,
                 r.avg_rating DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Long> findRecipeIdsByIngredientsAndDiet(
            @Param("ingredients") List<String> ingredients,
            @Param("dietMode") String dietMode
    );

    // ⭐ Fallback cho user mới chưa có preference -> lấy món rating cao nhất
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PUBLISHED'
        ORDER BY r.avgRating DESC, r.viewCount DESC
        """)
    List<Recipe> findTopRatedRecipes(Pageable pageable);

    // ⭐ SỬA: thêm r.slug ở cột thứ 2
    @Query(value = """
    SELECT r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating,
        GROUP_CONCAT(DISTINCT
            CONCAT(i.name, ':', COALESCE(ri.quantity,''), ':', COALESCE(ri.unit,''))
            SEPARATOR '|'
        ) AS ingredients_str,
        COUNT(DISTINCT l.id) AS like_count
    FROM recipe r
    JOIN recipe_ingredient ri ON r.id = ri.recipe_id
    JOIN ingredient i ON ri.ingredient_id = i.id
    LEFT JOIN likes l ON l.target_id = r.id AND l.target_type = 'recipe'
    WHERE r.status = 'PUBLISHED'
    GROUP BY r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating
    ORDER BY like_count DESC, r.avg_rating DESC
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findMostLikedRecipes();

    // ⭐ SỬA: thêm r.slug ở cột thứ 2
    @Query(value = """
    SELECT r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating,
        GROUP_CONCAT(DISTINCT
            CONCAT(i.name, ':', COALESCE(ri.quantity,''), ':', COALESCE(ri.unit,''))
            SEPARATOR '|'
        ) AS ingredients_str
    FROM recipe r
    JOIN recipe_ingredient ri ON r.id = ri.recipe_id
    JOIN ingredient i ON ri.ingredient_id = i.id
    WHERE r.status = 'PUBLISHED'
    GROUP BY r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating
    ORDER BY r.avg_rating DESC, r.view_count DESC
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findTopRatedRecipesRaw();

    // ⭐ SỬA: thêm r.slug ở cột thứ 2
    @Query(value = """
        SELECT r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating,
            GROUP_CONCAT(DISTINCT
                CONCAT(i.name, ':', COALESCE(ri.quantity,''), ':', COALESCE(ri.unit,''))
                SEPARATOR '|'
            ) AS ingredients_str
        FROM recipe r
        JOIN recipe_ingredient ri ON r.id = ri.recipe_id
        JOIN ingredient i ON ri.ingredient_id = i.id
        JOIN categories rg ON r.region_id = rg.id
        WHERE r.status = 'PUBLISHED' AND rg.slug = :regionSlug
        GROUP BY r.id, r.slug, r.name, r.image_url, r.cooking_time, r.avg_rating
        ORDER BY r.avg_rating DESC, r.view_count DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findRecipesByRegionOnly(@Param("regionSlug") String regionSlug);

}