package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.QuestionRequest;
import com.example.Bep_Viet.response.QuestionResponse;

import java.util.List;

public interface QuestionService {
    QuestionResponse create(QuestionRequest request, Long userId);
    QuestionResponse getById(Long id);
    List<QuestionResponse> getAll();
    List<QuestionResponse> getByUserId(Long userId);
    List<QuestionResponse> search(String keyword);
    QuestionResponse update(Long id, QuestionRequest request, Long currentUserId);
    void delete(Long id, Long currentUserId);
}
