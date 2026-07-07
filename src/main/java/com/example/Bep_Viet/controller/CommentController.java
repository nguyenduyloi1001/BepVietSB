package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.request.CommentRequest;
import com.example.Bep_Viet.response.CommentResponse;
import com.example.Bep_Viet.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // Lấy tất cả comment (kèm replies) của 1 target
    // GET /api/comments?targetId=1&targetType=POST
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getByTarget(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {  // ← đổi String thành TargetType
        return ResponseEntity.ok(commentService.getByTarget(targetId, targetType));
    }

    // POST /api/comments?userId=1
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> create(
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(request, userId));
    }

    // PUT /api/comments/5?userId=1
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> update(
            @PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(commentService.update(id, content, userId));
    }

    // DELETE /api/comments/5?userId=1
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        commentService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}