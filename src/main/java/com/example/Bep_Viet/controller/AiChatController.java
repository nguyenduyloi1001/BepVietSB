package com.example.Bep_Viet.controller;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.request.ChatRequest;
import com.example.Bep_Viet.response.ChatResponse;
import com.example.Bep_Viet.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal(errorOnInvalidType = false) Long userId, // null nếu chưa đăng nhập
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId) {

        // guest bắt buộc phải có guestId hợp lệ để định danh, tránh session bị đoán mò
        if (userId == null && (guestId == null || guestId.isBlank())) {
            throw new AppException(ErrorCode.GUEST_ID_REQUIRED);
        }

        return ResponseEntity.ok(aiChatService.chat(userId, guestId, request));
    }

    @GetMapping("/searched-recipes")
    public ResponseEntity<List<ChatResponse.SuggestionItem>> getSearchedRecipes(
            @AuthenticationPrincipal(errorOnInvalidType = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(aiChatService.getSearchedRecipes(userId));
    }
}