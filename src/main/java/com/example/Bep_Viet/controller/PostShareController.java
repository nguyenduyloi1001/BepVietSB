package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.PostShareRequest;
import com.example.Bep_Viet.response.PostShareResponse;
import com.example.Bep_Viet.service.PostShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post-shares")
@RequiredArgsConstructor
public class PostShareController {

    private final PostShareService postShareService;

    // Đăng lại bài viết
    @PostMapping
    public ResponseEntity<PostShareResponse> sharePost(
            @AuthenticationPrincipal Long userId,
            @RequestBody PostShareRequest request) {
        return ResponseEntity.ok(postShareService.sharePost(userId, request));
    }

    // Huỷ đăng lại
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> unsharePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long shareId) {
        postShareService.unsharePost(userId, shareId);
        return ResponseEntity.noContent().build();
    }

    // Lấy danh sách bài đăng lại trên trang cá nhân của 1 user (public, ai xem cũng thấy)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostShareResponse>> getSharesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postShareService.getSharesByUser(userId, pageable));
    }
}