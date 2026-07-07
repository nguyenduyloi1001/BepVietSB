package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.response.NotificationResponse;
import com.example.Bep_Viet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getAll(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userId));
    }

    // GET /api/notifications/unread
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getUnread(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getMyUnread(userId));
    }

    // GET /api/notifications/unread/count
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countUnread(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    // PATCH /api/notifications/{id}/read
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markOne(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.markOneAsRead(id, userId));
    }

    // PATCH /api/notifications/read-all
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAll(@AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}