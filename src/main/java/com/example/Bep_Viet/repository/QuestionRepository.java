package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question,Long> {

    List<Question> findByUserId(Long userId);

    @Query("""
        SELECT q FROM Question q
        WHERE (:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY q.createdAt DESC
        """)
    List<Question> searchQuestions(@Param("keyword") String keyword);
}
