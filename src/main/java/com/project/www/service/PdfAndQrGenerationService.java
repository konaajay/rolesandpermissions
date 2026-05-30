package com.project.www.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfAndQrGenerationService {

    public String generateQrCodeBase64(String content) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200, hints);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
    }

    public byte[] generatePdfFromHtml(String htmlContent, boolean isLandscape) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        
        // Ensure proper HTML structure for openhtmltopdf
        if (!htmlContent.contains("<html>")) {
            String pageOrientation = isLandscape ? "landscape" : "portrait";
            htmlContent = "<html><head><style>@page { size: A4 " + pageOrientation + "; margin: 10mm; }</style></head><body>" 
                          + htmlContent + "</body></html>";
        }
        
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(outputStream);
        builder.run();
        
        return outputStream.toByteArray();
    }
}
