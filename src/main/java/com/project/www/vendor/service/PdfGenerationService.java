package com.project.www.vendor.service;

import com.project.www.vendor.service.PdfGenerationService;

import com.project.www.vendor.entity.Vendor;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.project.www.tenant.entity.CompanyProfile;
import com.project.www.tenant.repository.CompanyProfileRepository;
import com.project.www.vendor.entity.VendorInvoice;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PdfGenerationService {

    private final String RECEIPT_DIR = "uploads/receipts/";
    private final CompanyProfileRepository companyProfileRepository;

    public PdfGenerationService(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    private String fmt(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return "$" + NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    public String generatePaymentReceipt(VendorInvoice invoice) {
        Path uploadPath = Paths.get(RECEIPT_DIR + invoice.getTenantId());
        try {
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = "Receipt_" + invoice.getId() + "_" + UUID.randomUUID() + ".pdf";
            Path filePath = uploadPath.resolve(fileName);

            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            // ── Fonts ──────────────────────────────────────────
            Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   18);
            Font boldFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   11);
            Font normalFont  = FontFactory.getFont(FontFactory.HELVETICA,        10);
            Font smallFont   = FontFactory.getFont(FontFactory.HELVETICA,         9);
            Font footerFont  = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);
            Font greenFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10);
            greenFont.setColor(new Color(22, 163, 74));  // emerald-600
            Font amberFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10);
            amberFont.setColor(new Color(217, 119, 6)); // amber-600

            // ── Company Header ─────────────────────────────────
            CompanyProfile cp = companyProfileRepository.findByTenantId(invoice.getTenantId()).orElse(null);
            String companyName = (cp != null && cp.getCompanyName() != null) ? cp.getCompanyName().toUpperCase() : "COMPANY";

            Paragraph titlePara = new Paragraph("PAYMENT RECEIPT", titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(4);
            document.add(titlePara);

            Paragraph compPara = new Paragraph(companyName, boldFont);
            compPara.setAlignment(Element.ALIGN_CENTER);
            document.add(compPara);

            if (cp != null && cp.getEmail() != null) {
                Paragraph emailPara = new Paragraph(cp.getEmail(), smallFont);
                emailPara.setAlignment(Element.ALIGN_CENTER);
                document.add(emailPara);
            }

            document.add(new Paragraph(" "));

            // ── Meta info table (invoice ref / vendor / dates) ─
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingBefore(10f);
            metaTable.setSpacingAfter(10f);
            metaTable.setWidths(new float[]{1f, 1f});

            // helper to add meta row
            String invoiceRef = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-" + invoice.getId();
            addMetaRow(metaTable, "Invoice Ref:", invoiceRef, boldFont, normalFont);
            addMetaRow(metaTable, "Vendor:", invoice.getVendor().getVendorName(), boldFont, normalFont);
            addMetaRow(metaTable, "PO Reference:", invoice.getPoRef() != null ? invoice.getPoRef() : "N/A", boldFont, normalFont);
            addMetaRow(metaTable, "Due Date:", invoice.getDueDate() != null ? invoice.getDueDate() : "N/A", boldFont, normalFont);
            addMetaRow(metaTable, "Receipt Date:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), boldFont, normalFont);
            addMetaRow(metaTable, "Status:", invoice.getStatus(), boldFont, normalFont);
            document.add(metaTable);

            document.add(new Paragraph(" "));

            // ── Payment Installments Table ─────────────────────
            Paragraph sectionTitle = new Paragraph("PAYMENT BREAKDOWN", boldFont);
            sectionTitle.setSpacingAfter(6);
            document.add(sectionTitle);

            PdfPTable payTable = new PdfPTable(3);
            payTable.setWidthPercentage(100);
            payTable.setWidths(new float[]{0.6f, 3f, 1.4f});
            payTable.setSpacingBefore(4f);
            payTable.setSpacingAfter(4f);

            // Header row
            Color headerBg = new Color(30, 41, 59); // slate-800
            addTableHeader(payTable, "#",            boldFont, headerBg);
            addTableHeader(payTable, "Description",  boldFont, headerBg);
            addTableHeader(payTable, "Amount",       boldFont, headerBg);

            // Parse payment history
            List<PaymentStep> steps = parsePaymentHistory(invoice.getPaymentHistory());

            if (steps.isEmpty()) {
                // No history — single row with total paid
                PdfPCell nc = borderCell(new Phrase("1", normalFont));
                payTable.addCell(nc);
                payTable.addCell(borderCell(new Phrase("Payment for " + invoiceRef, normalFont)));
                payTable.addCell(borderCell(new Phrase(fmt(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : invoice.getAmount()), normalFont)));
            } else {
                // List each installment
                for (int i = 0; i < steps.size(); i++) {
                    PaymentStep step = steps.get(i);
                    payTable.addCell(borderCell(new Phrase(String.valueOf(i + 1), normalFont)));
                    String desc = step.note + " – " + invoiceRef
                            + (step.date != null ? " (" + step.date + ")" : "");
                    payTable.addCell(borderCell(new Phrase(desc, normalFont)));
                    payTable.addCell(borderCell(new Phrase("$" + NumberFormat.getNumberInstance(Locale.US).format(step.amount), normalFont)));
                }
            }

            document.add(payTable);

            // ── Summary ────────────────────────────────────────
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingBefore(8f);
            summaryTable.setWidths(new float[]{2f, 1.5f});

            addSummaryRow(summaryTable, "Invoice Total:", fmt(invoice.getAmount()), normalFont, boldFont, false);
            addSummaryRow(summaryTable, "Total Paid:", fmt(invoice.getAmountPaid()), greenFont, greenFont, false);
            BigDecimal pending = invoice.getAmountPending() != null ? invoice.getAmountPending() : BigDecimal.ZERO;
            addSummaryRow(summaryTable, "Balance Remaining:", fmt(pending), pending.compareTo(BigDecimal.ZERO) > 0 ? amberFont : normalFont, pending.compareTo(BigDecimal.ZERO) > 0 ? amberFont : normalFont, true);
            document.add(summaryTable);

            // ── Footer ─────────────────────────────────────────
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph(
                    "This is a computer generated receipt. No signature is required.\nGenerated on " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                    footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            return filePath.toString();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addMetaRow(PdfPTable table, String label, String value, Font bold, Font normal) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, bold));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setPadding(3);
        table.addCell(lCell);
        PdfPCell vCell = new PdfPCell(new Phrase(value, normal));
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setPadding(3);
        table.addCell(vCell);
    }

    private void addTableHeader(PdfPTable table, String text, Font font, Color bg) {
        Font whiteFont = FontFactory.getFont(font.getFamilyname(), font.getSize(), Font.BOLD);
        whiteFont.setColor(Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, whiteFont));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setBorderColor(new Color(51, 65, 85));
        table.addCell(cell);
    }

    private PdfPCell borderCell(Phrase phrase) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setPadding(6);
        cell.setBorderColor(new Color(148, 163, 184)); // slate-400
        return cell;
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, boolean isLast) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setPadding(5);
        if (isLast) lc.setBorderWidthTop(1.5f);
        table.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setPadding(5);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (isLast) vc.setBorderWidthTop(1.5f);
        table.addCell(vc);
    }

    // ── Simple JSON parser for paymentHistory ─────────────────────────────────

    private static class PaymentStep {
        String date;
        double amount;
        String note;
    }

    private List<PaymentStep> parsePaymentHistory(String json) {
        List<PaymentStep> steps = new ArrayList<>();
        if (json == null || json.isBlank()) return steps;
        try {
            // Use Jackson available via Spring Boot
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(json);
            if (arr.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                    PaymentStep step = new PaymentStep();
                    step.date   = node.has("date")   ? node.get("date").asText()   : "";
                    step.amount = node.has("amount") ? node.get("amount").asDouble(): 0;
                    step.note   = node.has("note")   ? node.get("note").asText()   : "Payment";
                    steps.add(step);
                }
            }
        } catch (Exception ignored) {}
        return steps;
    }
}
