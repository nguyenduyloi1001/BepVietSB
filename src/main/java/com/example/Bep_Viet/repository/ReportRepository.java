package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.ReportStatus;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report,Long> {
    List<Report> findByReportStatus(ReportStatus reportStatus);
    List<Report> findByUserId(Long userId);
    List<Report> findByTargetIdAndTargetType(Long targetId, TargetType targetType);
}
