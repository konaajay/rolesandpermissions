package com.project.www.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.project.www.entity.CompanyProfile;
import com.project.www.repository.CompanyProfileRepository;
import com.project.www.entity.VendorInvoice;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfGenerationService {

    private final String RECEIPT_DIR = "uploads/receipts/";
    private final CompanyProfileRepository companyProfileRepository;

    public PdfGenerationService(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    public String generatePaymentReceipt(VendorInvoice invoice) {
        Path uploadPath = Paths.get(RECEIPT_DIR + invoice.getTenantId());
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = "Receipt_" + invoice.getId() + "_" + UUID.randomUUID().toString() + ".pdf";
            Path filePath = uploadPath.resolve(fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            // Add Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("PAYMENT RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Add Header Details
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            
            CompanyProfile companyProfile = companyProfileRepository.findByTenantId(invoice.getTenantId()).orElse(null);
            
            if (companyProfile != null) {
                document.add(new Paragraph(companyProfile.getCompanyName(), boldFont));
                if (companyProfile.getGstNumber() != null) {
                    document.add(new Paragraph("GST: " + companyProfile.getGstNumber(), normalFont));
                }
                if (companyProfile.getEmail() != null) {
                    document.add(new Paragraph("Email: " + companyProfile.getEmail(), normalFont));
                }
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")), normalFont));
            document.add(new Paragraph("Receipt No: REC-" + invoice.getId(), normalFont));
            document.add(new Paragraph("Vendor: " + invoice.getVendor().getVendorName(), normalFont));
            document.add(new Paragraph(" "));

            // Add Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Table Header
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            PdfPCell cell1 = new PdfPCell(new Phrase("Description", tableHeaderFont));
            cell1.setPadding(5);
            table.addCell(cell1);
            
            PdfPCell cell2 = new PdfPCell(new Phrase("Amount", tableHeaderFont));
            cell2.setPadding(5);
            table.addCell(cell2);

            // Table Data
            table.addCell(new Phrase("Payment for Invoice: " + (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-" + (4000 + invoice.getId()))));
            table.addCell(new Phrase("$" + invoice.getAmount()));
            
            table.addCell(new Phrase("Amount Paid"));
            table.addCell(new Phrase("$" + (invoice.getAmountPaid() != null ? invoice.getAmountPaid() : invoice.getAmount())));
            
            table.addCell(new Phrase("Amount Pending"));
            table.addCell(new Phrase("$" + (invoice.getAmountPending() != null ? invoice.getAmountPending() : "0.00")));
            
            table.addCell(new Phrase("PO Reference"));
            table.addCell(new Phrase(invoice.getPoRef() != null ? invoice.getPoRef() : "N/A"));

            document.add(table);

            // Add Footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
            Paragraph footer = new Paragraph("This is a system generated receipt. No signature is required.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(50);
            document.add(footer);

            document.close();

            return filePath.toString();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }
}
