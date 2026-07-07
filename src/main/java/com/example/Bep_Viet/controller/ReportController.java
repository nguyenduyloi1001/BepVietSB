package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.ReportStatus;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.request.ReportRequest;
import com.example.Bep_Viet.request.UpdateReportStatusRequest;
import com.example.Bep_Viet.response.ReportResponse;
import com.example.Bep_Viet.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(userId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAll() {
        return ResponseEntity.ok(reportService.getAll());
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getByStatus(@RequestParam ReportStatus status) {
        return ResponseEntity.ok(reportService.getReportByStatus(status));
    }

    @GetMapping("/target")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getByTarget(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(reportService.getReportsByTarget(targetId, targetType));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReportResponse>> getMyReports(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(reportService.getReportByUser(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest request) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
