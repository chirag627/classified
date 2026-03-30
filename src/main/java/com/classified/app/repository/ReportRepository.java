package com.classified.app.repository;

import com.classified.app.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByAdId(String adId);
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    long countByStatus(Report.ReportStatus status);
}
