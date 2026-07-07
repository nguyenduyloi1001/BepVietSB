package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestionId(Long questionId);
    boolean existsByQuestionId(Long questionId); // dung de kiem tra question co answer chua
}
