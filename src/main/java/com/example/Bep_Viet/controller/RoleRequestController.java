package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.RoleRequestStatus;
import com.example.Bep_Viet.request.RoleRequestRequest;
import com.example.Bep_Viet.response.RoleRequestResponse;
import com.example.Bep_Viet.service.RoleRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role-requests")
@RequiredArgsConstructor
public class RoleRequestController {

    private final RoleRequestService roleRequestService;

    // User nộp đơn
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleRequestResponse> submitRequest(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid RoleRequestRequest request) {
        return ResponseEntity.ok(roleRequestService.submitRequest(userId, request));
    }

    // Admin duyệt đơn
    @PatchMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleRequestResponse> approveRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long adminId) {
        return ResponseEntity.ok(roleRequestService.approveRequest(requestId, adminId));
    }

    // Admin từ chối đơn
    @PatchMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleRequestResponse> rejectRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long adminId,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(roleRequestService.rejectRequest(requestId, adminId, note));
    }

    // Admin xem danh sách đơn PENDING
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleRequestResponse>> getPendingRequests() {
        return ResponseEntity.ok(roleRequestService.getPendingRequests());
    }

    // User xem lịch sử đơn của mình
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleRequestResponse>> getMyRequests(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(roleRequestService.getRequestsByUser(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleRequestResponse>> getAllRequests(
            @RequestParam(required = false) RoleRequestStatus status) {
        if (status != null) {
            return ResponseEntity.ok(roleRequestService.getRequestsByStatus(status));
        }
        return ResponseEntity.ok(roleRequestService.getAllRequests());
    }
}