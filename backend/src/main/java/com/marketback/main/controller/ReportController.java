package com.marketback.main.controller;

import com.marketback.main.entity.Report;
import com.marketback.main.service.ReportService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController extends BaseCrudController<Report> {

    public ReportController(ReportService reportService) {
        super(reportService, "reportId");
    }
}
