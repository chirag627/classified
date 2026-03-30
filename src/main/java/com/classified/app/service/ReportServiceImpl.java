package com.classified.app.service;

import com.classified.app.dto.request.CreateReportRequest;
import com.classified.app.dto.response.PagedResponse;
import com.classified.app.exception.ResourceNotFoundException;
import com.classified.app.model.Report;
import com.classified.app.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    public Report createReport(CreateReportRequest request, String reporterId) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .adId(request.getAdId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(Report.ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return reportRepository.save(report);
    }

    @Override
    public PagedResponse<Report> getReportsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Report.ReportStatus reportStatus = Report.ReportStatus.valueOf(status.toUpperCase());
        Page<Report> reports = reportRepository.findByStatus(reportStatus, pageable);
        List<Report> content = reports.getContent();
        return PagedResponse.<Report>builder()
                .content(content)
                .page(reports.getNumber())
                .size(reports.getSize())
                .totalElements(reports.getTotalElements())
                .totalPages(reports.getTotalPages())
                .last(reports.isLast())
                .first(reports.isFirst())
                .build();
    }

    @Override
    public Report updateReportStatus(String id, Report.ReportStatus status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
        report.setStatus(status);
        return reportRepository.save(report);
    }

    @Override
    public long getPendingReportsCount() {
        return reportRepository.countByStatus(Report.ReportStatus.PENDING);
    }
}
