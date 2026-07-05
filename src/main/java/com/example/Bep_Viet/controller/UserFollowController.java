package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.response.UserResponse;
import com.example.Bep_Viet.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PostMapping("/follow/{followingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> followUser(
            @PathVariable Long followingId,
            @AuthenticationPrincipal Long followerId) {  // ← lấy từ token
        userFollowService.followUser(followerId, followingId);
        return ResponseEntity.ok("Follow thành công");
    }

    @DeleteMapping("/unfollow/{followingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> unfollowUser(
            @PathVariable Long followingId,
            @AuthenticationPrincipal Long followerId) {  // ← lấy từ token
        userFollowService.unfollowUser(followerId, followingId);
        return ResponseEntity.ok("Unfollow thành công");
    }

    // Public - ai cũng xem được
    @GetMapping("/{followerId}/is-following/{followingId}")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        return ResponseEntity.ok(userFollowService.isFollowing(followerId, followingId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(userFollowService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(userFollowService.getFollowing(userId));
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> countFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(userFollowService.countFollowers(userId));
    }

    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> countFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(userFollowService.countFollowing(userId));
    }
}
