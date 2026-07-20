package com.example.Bep_Viet.repository;


import com.example.Bep_Viet.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    void deleteByUserIsNullAndCreatedAtBefore(LocalDateTime cutoff);
}