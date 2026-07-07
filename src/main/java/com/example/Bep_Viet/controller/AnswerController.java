package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.AnswerRequest;
import com.example.Bep_Viet.response.AnswerResponse;
import com.example.Bep_Viet.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService service;

    @PostMapping("/question/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> create(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(questionId, request, adminId));
    }

    @PutMapping("/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> update(
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(service.update(answerId, request, currentUserId));
    }

    @DeleteMapping("/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long answerId,
            @AuthenticationPrincipal Long currentUserId) {
        service.delete(answerId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}