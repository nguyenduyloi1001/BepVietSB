package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.request.ShareRequest;
import com.example.Bep_Viet.response.ShareResponse;
import com.example.Bep_Viet.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareController {
    private final ShareService shareService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShareResponse> share(
            @Valid @RequestBody ShareRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shareService.share(request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unshare(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        shareService.unshare(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/target")
    public ResponseEntity<List<ShareResponse>> getByTarget(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(shareService.getByTarget(targetId, targetType));
    }

    @GetMapping("/target/count")
    public ResponseEntity<Long> count(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(shareService.count(targetId, targetType));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ShareResponse>> getMyShares(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(shareService.getByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ShareResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(shareService.getByUserId(userId));
    }
}