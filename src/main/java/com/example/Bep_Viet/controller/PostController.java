package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.PostStatus;
import com.example.Bep_Viet.enums.PostType;
import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.PostRequest;
import com.example.Bep_Viet.response.PostResponse;
import com.example.Bep_Viet.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAll() {
        return ResponseEntity.ok(postService.getAllPost());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PostResponse>> getByType(@PathVariable PostType type) {
        return ResponseEntity.ok(postService.getByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PostResponse>> getByStatus(@PathVariable PostStatus status) {
        return ResponseEntity.ok(postService.getByStatus(status));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> create(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(postService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'CHEF', 'BLOGGER')")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        boolean isAdmin = currentUser.getRole() == Role.ADMIN;


        postService.delete(id, userId, isAdmin);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(postService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(postService.reject(id));
    }
}