package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.ReportStatus;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.request.ReportRequest;
import com.example.Bep_Viet.request.UpdateReportStatusRequest;
import com.example.Bep_Viet.response.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(Long userId, ReportRequest request);
    List<ReportResponse> getAll();
    List<ReportResponse> getReportByStatus(ReportStatus reportStatus);
    List<ReportResponse> getReportByUser(Long userId);
    List<ReportResponse> getReportsByTarget(Long targetId, TargetType targetType);
    ReportResponse getReportById(Long id);
    ReportResponse updateReportStatus(Long id, UpdateReportStatusRequest request);
    void deleteReport(Long id);
}