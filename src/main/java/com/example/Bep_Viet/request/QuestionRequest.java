package com.example.Bep_Viet.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionRequest {

    @NotBlank(message = "Bắt buộc có title")
    private String title;

    @NotBlank(message = "Bắt buộc có content")
    private String content;
}