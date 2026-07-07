package com.example.Bep_Viet.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotBlank(message = "Bắt buộc có content")
    private String content;
}