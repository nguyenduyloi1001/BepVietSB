package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnswerResponse {
    private Long id;
    private Long questionId;
    private Long userId;
    private String email;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
