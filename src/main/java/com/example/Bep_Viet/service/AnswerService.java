package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.AnswerRequest;
import com.example.Bep_Viet.response.AnswerResponse;

public interface AnswerService {
    AnswerResponse create(Long questionId, AnswerRequest request, Long adminId);
    AnswerResponse update(Long answerId, AnswerRequest request, Long currentUserId);
    void delete(Long answerId, Long currentUserId);
}