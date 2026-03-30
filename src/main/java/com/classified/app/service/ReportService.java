package com.classified.app.service;

import com.classified.app.dto.request.CreateReportRequest;
import com.classified.app.dto.response.PagedResponse;
import com.classified.app.model.Report;

public interface ReportService {
    Report createReport(CreateReportRequest request, String reporterId);
    PagedResponse<Report> getReportsByStatus(String status, int page, int size);
    Report updateReportStatus(String id, Report.ReportStatus status);
    long getPendingReportsCount();
}
