package com.project.www.service;

import com.project.www.enums.*;

import com.project.www.dto.CampaignReportDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.stream.Stream;

@Service
public class ReportExporter {

    /**
     * Streams reports to a writer to avoid high memory consumption.
     */
    public void exportCampaignsToCSVStream(Stream<CampaignReportDTO> reports, Writer writer) throws IOException {
        // Header
        writer.write(
                "Campaign ID,Campaign Name,Impressions,Visits,Leads,Purchases,Conversion Rate (%),Drop-off Rate (%),Spent,Revenue,Refunds,ROI (%)\n");

        Iterator<CampaignReportDTO> it = reports.iterator();
        while (it.hasNext()) {
            CampaignReportDTO r = it.next();
            writer.write(String.format("%d,%s,%d,%d,%d,%d,%.2f,%.2f,%s,%s,%s,%.2f\n",
                    r.getCampaignId(),
                    escapeCsv(r.getCampaignName()),
                    r.getImpressions(),
                    r.getVisits(),
                    r.getLeads(),
                    r.getPurchases(),
                    r.getConversionRate(),
                    r.getDropOffRate(),
                    r.getSpent() != null ? r.getSpent().toString() : "0",
                    r.getRevenue() != null ? r.getRevenue().toString() : "0",
                    r.getRefunds() != null ? r.getRefunds().toString() : "0",
                    r.getRoi()));
        }
        writer.flush();
    }

    private String escapeCsv(String data) {
        if (data == null)
            return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("'")) {
            escapedData = "\"" + escapedData.replace("\"", "\"\"") + "\"";
        }
        return escapedData;
    }
}
