package com.example.Bep_Viet.repository;


import com.example.Bep_Viet.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop6BySessionIdOrderByCreatedAtDesc(Long sessionId);
}