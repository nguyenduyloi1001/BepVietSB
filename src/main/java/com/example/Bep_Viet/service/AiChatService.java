package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.ChatRequest;
import com.example.Bep_Viet.response.ChatResponse;

import java.util.List;

public interface AiChatService {
    ChatResponse chat(Long userId, String guestId, ChatRequest request);
    List<ChatResponse.SuggestionItem> getSearchedRecipes(Long userId);
}