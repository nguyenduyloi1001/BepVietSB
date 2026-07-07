package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.LikeRequest;
import com.example.Bep_Viet.response.LikeResponse;
import com.example.Bep_Viet.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // Toggle like/unlike - cần truyền userId (sau này thay bằng JWT)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/toggle/{userId}")
    public ResponseEntity<LikeResponse> toggle(
            @PathVariable Long userId,
            @RequestBody LikeRequest request) {
        return ResponseEntity.ok(likeService.toggle(userId, request));
    }

    // Đếm số like
    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long targetId,
            @RequestParam String targetType) {
        return ResponseEntity.ok(likeService.count(targetId, targetType));
    }

    // Kiểm tra user đã like chưa
    @GetMapping("/check")
    public ResponseEntity<Boolean> isLiked(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam String targetType) {
        return ResponseEntity.ok(likeService.isLiked(userId, targetId, targetType));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countUserLikes(@PathVariable Long userId) {
        return ResponseEntity.ok(likeService.countTotalLikesByUserId(userId));
    }
}
