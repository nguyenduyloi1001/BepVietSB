package com.example.Bep_Viet.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Tin nhắn không được để trống")
    @Size(max = 500, message = "Tin nhắn tối đa 500 ký tự")
    private String message;
    // null = tạo session mới, có giá trị = tiếp tục hội thoại cũ
    private Long sessionId;
}