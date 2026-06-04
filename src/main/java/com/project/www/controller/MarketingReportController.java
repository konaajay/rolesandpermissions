package com.project.www.controller;

import com.project.www.enums.*;

import com.project.www.dto.CampaignReportDTO;
import com.project.www.service.CampaignService;
import com.project.www.service.ReportExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("marketingReportController")
@RequestMapping("/marketing/reports")
@CrossOrigin(origins = "*")
public class MarketingReportController {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private ReportExporter reportExporter;

    @GetMapping("/global/roi")
    public ResponseEntity<?> getGlobalRoi() {
        return ResponseEntity.ok(campaignService.getGlobalAnalyticsReport());
    }

    @GetMapping("/export/csv")
    public void exportReports(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=marketing_report.csv");

        java.util.List<com.project.www.entity.Campaign> campaigns = campaignService.getAllCampaignEntities();

        // Convert to DTO stream
        java.util.stream.Stream<CampaignReportDTO> reportStream = campaigns.stream()
                .map(c -> campaignService.getCampaignReport(c.getCampaignId()));

        reportExporter.exportCampaignsToCSVStream(reportStream, response.getWriter());
    }
}
