package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuestionResponse {
    private Long id;
    private Long userId;
    private String email;
    private String userName;
    private String title;
    private String content;
    private boolean isSolved;
    private AnswerResponse answer;
    private LocalDateTime createdAt;
}
