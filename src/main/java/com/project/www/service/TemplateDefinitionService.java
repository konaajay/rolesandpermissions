package com.project.www.service;

import com.project.www.entity.TemplateDefinition;
import com.project.www.repository.TemplateDefinitionRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateDefinitionService {
    private final TemplateDefinitionRepository repository;
    private final PdfAndQrGenerationService pdfService;
    private final com.project.www.repository.UserRepository userRepository;
    private final com.project.www.repository.CompanyProfileRepository companyProfileRepository;

    @org.springframework.beans.factory.annotation.Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }

    public List<TemplateDefinition> getAllTemplates(String type) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (type != null && !type.isBlank()) {
            return repository.findByTenantIdAndTemplateType(tenantId, type.toUpperCase());
        }
        return repository.findByTenantId(tenantId);
    }

    public TemplateDefinition getTemplateById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    @Transactional
    public TemplateDefinition createTemplate(TemplateDefinition template) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();

        if (template.getTemplateCode() == null || template.getTemplateCode().isBlank()) {
            throw new RuntimeException("Template code is required");
        }

        template.setTemplateCode(template.getTemplateCode().toUpperCase().replaceAll("\\s+", "_"));

        if (repository.findByTenantIdAndTemplateCode(tenantId, template.getTemplateCode()).isPresent()) {
            throw new RuntimeException("A template with this code already exists.");
        }

        template.setTenantId(tenantId);
        template.setCreatedBy(username);
        template.setTemplateType(template.getTemplateType().toUpperCase());
        if (template.getActive() == null) template.setActive(true);

        return repository.save(template);
    }

    @Transactional
    public TemplateDefinition updateTemplate(Long id, TemplateDefinition req) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();

        TemplateDefinition existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // Allow editing system templates per user request
        // if (Boolean.TRUE.equals(existing.getIsSystemTemplate()) && Boolean.FALSE.equals(existing.getIsEditable())) {
        //     throw new RuntimeException("System templates cannot be edited. Please clone it to create a custom template.");
        // }

        if (req.getTemplateCode() != null) {
            String newCode = req.getTemplateCode().toUpperCase().replaceAll("\\s+", "_");
            if (repository.existsByTenantIdAndTemplateCodeAndIdNot(tenantId, newCode, id)) {
                throw new RuntimeException("A template with this code already exists.");
            }
            existing.setTemplateCode(newCode);
        }

        if (req.getTemplateName() != null) existing.setTemplateName(req.getTemplateName());
        if (req.getTemplateType() != null) existing.setTemplateType(req.getTemplateType().toUpperCase());
        if (req.getContentHtml() != null) existing.setContentHtml(req.getContentHtml());
        if (req.getBackgroundImageUrl() != null) existing.setBackgroundImageUrl(req.getBackgroundImageUrl());
        if (req.getActive() != null) existing.setActive(req.getActive());

        existing.setUpdatedBy(username);

        return repository.save(existing);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        TemplateDefinition existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
                
        if (Boolean.TRUE.equals(existing.getIsSystemTemplate())) {
            throw new RuntimeException("System templates cannot be deleted.");
        }
        
        repository.delete(existing);
    }

    public byte[] generateSamplePdf(Long id) throws Exception {
        Long tenantId = TenantContext.getCurrentTenant();
        TemplateDefinition t = getTemplateById(id);
        String html = t.getContentHtml();
        
        // Replace with real company images, then sample text data
        html = applyCompanyImages(html, tenantId);
        html = html.replace("{{COMPANY_NAME}}", "Sample Company Ltd.")
                   .replace("{{COMPANY_ADDRESS}}", "123 Business Road, Tech City")
                   .replace("{{DOCUMENT_NO}}", "DOC-2026-001")
                   .replace("{{ISSUE_DATE}}", java.time.LocalDate.now().toString())
                   .replace("{{EMPLOYEE_NAME}}", "John Doe")
                   .replace("{{EMPLOYEE_ID}}", "EMP-001")
                   .replace("{{DESIGNATION}}", "Software Engineer")
                   .replace("{{DEPARTMENT}}", "Engineering")
                   .replace("{{JOINING_DATE}}", "2026-01-01")
                   .replace("{{EMPLOYMENT_TYPE}}", "Full-time")
                   .replace("{{WORK_LOCATION}}", "Head Office")
                   .replace("{{ANNUAL_CTC}}", "$100,000")
                   .replace("{{PROBATION_PERIOD}}", "6 Months")
                   .replace("{{REPORTING_MANAGER}}", "Jane Smith")
                   .replace("{{SIGNATORY_NAME}}", "Alice Director")
                   .replace("{{SIGNATORY_DESIGNATION}}", "Managing Director")
                   .replace("{{START_DATE}}", "2025-01-01")
                   .replace("{{END_DATE}}", "2026-01-01")
                   .replace("{{RELIEVING_DATE}}", "2026-01-01")
                   .replace("{{WARNING_REASON}}", "Repeated unexcused absences")
                   .replace("{{OLD_DESIGNATION}}", "Junior Engineer")
                   .replace("{{NEW_DESIGNATION}}", "Software Engineer")
                   .replace("{{EFFECTIVE_DATE}}", "2026-01-01")
                   .replace("{{OLD_LOCATION}}", "Branch A")
                   .replace("{{NEW_LOCATION}}", "Head Office")
                   .replace("{{TRANSFER_DATE}}", "2026-01-01")
                   .replace("{{COURSE_NAME}}", "Advanced React")
                   .replace("{{TRAINING_NAME}}", "Security Awareness")
                   .replace("{{ACHIEVEMENT_NAME}}", "Employee of the Year")
                   .replace("{{EVENT_NAME}}", "Annual Tech Fest")
                   .replace("{{EVENT_DATE}}", "2026-05-15")
                   .replace("{{QR_CODE}}", "<div style=\"width:50px; height:50px; background:#ccc; border:1px solid #999;\">QR</div>");
        
        // Remove unreplaced placeholders
        html = html.replaceAll("\\{\\{[^}]+\\}\\}", "");
        
        // Escape bare & that are not already HTML entities (required by openhtmltopdf XML parser)
        html = html.replaceAll("&(?!(?:amp|lt|gt|quot|apos|#\\d+|#x[0-9a-fA-F]+);)", "&amp;");
        
        boolean isLandscape = "CERTIFICATE".equals(t.getTemplateType());
        return pdfService.generatePdfFromHtml(html, isLandscape);
    }

    public byte[] generateDocumentPdf(Long id, Long userId) throws Exception {
        Long tenantId = TenantContext.getCurrentTenant();
        TemplateDefinition t = getTemplateById(id);
        com.project.www.entity.User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String html = t.getContentHtml();
        
        // Apply real company images first (resolves both {{}} tokens and data-variable img tags)
        html = applyCompanyImages(html, tenantId);

        // Load company profile for name/address
        com.project.www.entity.CompanyProfile company =
            companyProfileRepository.findByTenantId(tenantId).orElse(null);
        String companyName = company != null && company.getCompanyName() != null ? company.getCompanyName() : "Company Ltd.";
        String companyAddress = company != null ? buildAddress(company) : "";

        html = html.replace("{{COMPANY_NAME}}", companyName)
                   .replace("{{COMPANY_ADDRESS}}", companyAddress)
                   .replace("{{EMPLOYEE_NAME}}", user.getFirstName() + " " + user.getLastName())
                   .replace("{{EMPLOYEE_ID}}", "EMP-" + user.getId())
                   .replace("{{ISSUE_DATE}}", java.time.LocalDate.now().toString())
                   .replace("{{DOCUMENT_NO}}", "DOC-" + java.time.Year.now().getValue() + "-" + user.getId());

        if (user.getRole() != null) {
            html = html.replace("{{DESIGNATION}}", user.getRole().getName());
        }

        // Remove remaining unreplaced placeholders
        html = html.replaceAll("\\{\\{[^}]+\\}\\}", "");
        
        // Escape bare & that are not already HTML entities (required by openhtmltopdf XML parser)
        html = html.replaceAll("&(?!(?:amp|lt|gt|quot|apos|#\\d+|#x[0-9a-fA-F]+);)", "&amp;");

        boolean isLandscape = "CERTIFICATE".equals(t.getTemplateType());
        return pdfService.generatePdfFromHtml(html, isLandscape);
    }

    /**
     * Resolves company image placeholders in two ways:
     * 1. {{COMPANY_LOGO}} / {{COMPANY_SIGNATURE}} / {{COMPANY_STAMP}} text tokens → <img> tags
     * 2. <img data-variable="COMPANY_LOGO" ...> → replaces src with real URL
     * Falls back to a styled placeholder div if no URL is configured.
     */
    private String applyCompanyImages(String html, Long tenantId) {
        com.project.www.entity.CompanyProfile company =
            companyProfileRepository.findByTenantId(tenantId).orElse(null);

        String logoImg    = imageTag(company != null ? company.getLogoUrl() : null,    "COMPANY_LOGO",      "150", "Company Logo");
        String signImg    = imageTag(company != null ? company.getSignatureUrl() : null, "COMPANY_SIGNATURE", "120", "Signature");
        String stampImg   = imageTag(company != null ? company.getStampUrl() : null,    "COMPANY_STAMP",     "100", "Stamp");
        String headerImg  = imageTag(company != null ? company.getHeaderImageUrl() : null, "HEADER_IMAGE",   "100%", "Header");
        String footerImg  = imageTag(company != null ? company.getFooterImageUrl() : null, "FOOTER_IMAGE",   "100%", "Footer");

        // 1. Replace text-style {{PLACEHOLDER}} tokens
        html = html.replace("{{COMPANY_LOGO}}",      logoImg)
                   .replace("{{COMPANY_SIGNATURE}}", signImg)
                   .replace("{{COMPANY_STAMP}}",     stampImg)
                   .replace("{{HEADER_IMAGE}}",      headerImg)
                   .replace("{{FOOTER_IMAGE}}",      footerImg);

        // 2. Replace data-variable img tags (visual editor style)
        html = resolveDataVariable(html, "COMPANY_LOGO",      company != null ? company.getLogoUrl() : null,      "150");
        html = resolveDataVariable(html, "COMPANY_SIGNATURE", company != null ? company.getSignatureUrl() : null, "120");
        html = resolveDataVariable(html, "COMPANY_STAMP",     company != null ? company.getStampUrl() : null,     "100");
        html = resolveDataVariable(html, "HEADER_IMAGE",      company != null ? company.getHeaderImageUrl() : null, "100%");
        html = resolveDataVariable(html, "FOOTER_IMAGE",      company != null ? company.getFooterImageUrl() : null, "100%");

        return html;
    }

    /** Replaces <img data-variable="VAR" ...> src with the real URL, or a fallback div. */
    private String resolveDataVariable(String html, String variable, String url, String width) {
        // Match <img ... data-variable="VAR" ... /> and replace the whole tag
        String pattern = "<img([^>]*?)data-variable=\"" + variable + "\"([^>]*?)/>";
        if (url != null && !url.isBlank()) {
            String replacement = "<img src='" + url + "' width='" + width + "' style='max-width:100%;' />";
            html = html.replaceAll(pattern, replacement);
            // Also match without self-close
            html = html.replaceAll("<img([^>]*?)data-variable=\"" + variable + "\"([^>]*?)>", replacement);
        } else {
            html = html.replaceAll(pattern, "<div style='border:1px dashed #ccc;padding:8px;color:#999;font-size:10px;'>" + variable + "</div>");
            html = html.replaceAll("<img([^>]*?)data-variable=\"" + variable + "\"([^>]*?)>", "<div style='border:1px dashed #ccc;padding:8px;color:#999;font-size:10px;'>" + variable + "</div>");
        }
        return html;
    }

    private String imageTag(String url, String variable, String width, String alt) {
        if (url != null && !url.isBlank()) {
            return "<img src='" + url + "' width='" + width + "' alt='" + alt + "' style='max-width:100%;' />";
        }
        return "<div style='border:1px dashed #ccc;padding:8px;color:#999;font-size:10px;display:inline-block;'>" + alt + " not configured</div>";
    }

    private String buildAddress(com.project.www.entity.CompanyProfile c) {
        StringBuilder sb = new StringBuilder();
        if (c.getAddressLine1() != null) sb.append(c.getAddressLine1());
        if (c.getCity() != null) sb.append(", ").append(c.getCity());
        if (c.getState() != null) sb.append(", ").append(c.getState());
        if (c.getCountry() != null) sb.append(", ").append(c.getCountry());
        return sb.toString();
    }

    private static TemplateDefinition createSystemTemplate(String code, String name, String type, String html) {
        TemplateDefinition t = new TemplateDefinition();
        t.setTemplateCode(code);
        t.setTemplateName(name);
        t.setTemplateType(type);
        t.setContentHtml(html);
        t.setIsSystemTemplate(true);
        t.setIsEditable(false);
        return t;
    }

    private static String getCertificateHtml(String title, String description, String colorCode) {
        return "<div style=\"font-family: Arial, sans-serif; text-align: center; padding: 30px; margin: 5px; border: 10px solid " + colorCode + "; position: relative; background-color: #fff; box-sizing: border-box; page-break-inside: avoid;\">" +
               "<div style=\"position: absolute; top: 10px; left: 10px; right: 10px; bottom: 10px; border: 2px solid " + colorCode + ";\"></div>" +
               "<div style=\"margin-bottom: 15px;\">{{COMPANY_LOGO}}</div>" +
               "<h1 style=\"font-size: 32px; color: " + colorCode + "; margin: 10px 0; text-transform: uppercase; letter-spacing: 2px; font-weight: bold;\">" + title + "</h1>" +
               "<p style=\"font-size: 16px; color: #555; margin-bottom: 15px;\">This is to certify that</p>" +
               "<h2 style=\"font-size: 36px; color: #333; margin: 10px 0; font-family: 'Times New Roman', Georgia, serif; font-style: italic; border-bottom: 1px solid #ddd; display: inline-block; padding: 0 30px 10px 30px;\">{{EMPLOYEE_NAME}}</h2>" +
               "<div style=\"font-size: 16px; color: #444; line-height: 1.5; margin: 20px auto; max-width: 80%;\">" + description + "</div>" +
               "<table style=\"width: 100%; margin-top: 30px; font-size: 12px;\"><tr>" +
               "<td style=\"width: 33%; text-align: center; vertical-align: bottom;\">" +
               "<p style=\"margin: 0 0 10px 0;\">Date: <strong>{{ISSUE_DATE}}</strong></p>" +
               "</td>" +
               "<td style=\"width: 33%; text-align: center; vertical-align: bottom;\">{{COMPANY_STAMP}}</td>" +
               "<td style=\"width: 33%; text-align: center; vertical-align: bottom;\">" +
               "<div>{{COMPANY_SIGNATURE}}</div>" +
               "<p style=\"margin: 10px auto 5px auto; width: 60%; border-top: 1px solid #333; padding-top: 5px; font-weight: bold;\">{{SIGNATORY_NAME}}</p>" +
               "<p style=\"margin: 0; color: #555;\">{{SIGNATORY_DESIGNATION}}</p>" +
               "</td></tr></table>" +
               "<div style=\"position: absolute; bottom: 30px; left: 30px; text-align: left;\">" +
               "<p style=\"font-size: 10px; color: #888; margin: 0 0 5px 0;\">Scan to verify</p>" +
               "<div style=\"width: 50px;\">{{QR_CODE}}</div>" +
               "</div>" +
               "</div>";
    }

    private static String getDocumentHtml(String title, String content) {
        return "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 13px; color: #222; position: relative; padding: 40px;\">" +
               "<table style=\"width: 100%; margin-bottom: 15px;\"><tr>" +
               "<td style=\"width: 50%; vertical-align: top; padding: 0;\">{{COMPANY_LOGO}}</td>" +
               "<td style=\"width: 50%; vertical-align: top; text-align: right; padding: 0;\">" +
               "<h2 style=\"margin: 0; color: #0f172a; font-size: 16px; font-weight: bold;\">{{COMPANY_NAME}}</h2>" +
               "<p style=\"margin: 5px 0 0 0; font-size: 11px; line-height: 1.4; color: #555;\">{{COMPANY_ADDRESS}}</p>" +
               "</td></tr></table>" +
               "<div style=\"height: 4px; background-color: #2563eb; margin-bottom: 20px;\"></div>" +
               "<table style=\"width: 100%; margin-bottom: 30px; font-size: 11px; color: #555;\"><tr>" +
               "<td style=\"text-align: left;\">Ref No: <strong>{{DOCUMENT_NO}}</strong></td>" +
               "<td style=\"text-align: right;\">Date: <strong>{{ISSUE_DATE}}</strong></td>" +
               "</tr></table>" +
               "<div style=\"text-align: center; margin-bottom: 40px;\">" +
               "<h1 style=\"color: #1e3a8a; margin: 0; font-size: 22px; text-transform: uppercase; letter-spacing: 1px;\">" + title + "</h1>" +
               "<div style=\"margin: 10px auto; width: 60px; height: 1px; background-color: #1e3a8a; position: relative;\">" +
               "<div style=\"position: absolute; top: -3px; left: 26px; width: 6px; height: 6px; background-color: #1e3a8a; transform: rotate(45deg);\"></div>" +
               "</div>" +
               "</div>" +
               "<div style=\"margin-bottom: 20px; line-height: 1.5;\">" +
               "<p style=\"margin: 0;\">To,</p>" +
               "<p style=\"margin: 5px 0 0 0; font-weight: bold; font-size: 14px;\">{{EMPLOYEE_NAME}}</p>" +
               "<p style=\"margin: 0;\">Employee ID: {{EMPLOYEE_ID}}</p>" +
               "<p style=\"margin: 0;\">{{EMPLOYEE_ADDRESS}}</p>" +
               "</div>" +
               "<div style=\"margin-bottom: 30px; line-height: 1.6; text-align: justify;\">" +
               "<p>Dear <strong>{{EMPLOYEE_NAME}}</strong>,</p>" +
               content +
               "</div>" +
               "<table style=\"width: 100%; margin-top: 60px;\"><tr>" +
               "<td style=\"width: 40%; vertical-align: bottom;\">" +
               "<div>{{COMPANY_SIGNATURE}}</div>" +
               "<p style=\"margin: 10px 0 5px 0; border-top: 1px solid #333; padding-top: 5px; font-weight: bold;\">{{SIGNATORY_NAME}}</p>" +
               "<p style=\"margin: 0; color: #555; font-size: 11px;\">{{SIGNATORY_DESIGNATION}}</p>" +
               "<p style=\"margin: 0; color: #222; font-size: 11px; font-weight: bold;\">{{COMPANY_NAME}}</p>" +
               "</td>" +
               "<td style=\"width: 20%; text-align: center; vertical-align: bottom;\">{{COMPANY_STAMP}}</td>" +
               "<td style=\"width: 40%; text-align: right; vertical-align: bottom;\">" +
               "<p style=\"margin: 0 0 40px 0; font-size: 11px; text-align: left; padding-left: 20px;\">I accept the terms and conditions:</p>" +
               "<p style=\"margin: 10px 0 0 0; border-top: 1px solid #333; display: inline-block; width: 200px; text-align: left; padding-top: 5px;\">Signature &amp; Date</p>" +
               "</td></tr></table>" +
               "<div style=\"margin-top: 40px; padding-top: 20px; font-size: 10px; color: #888; text-align: left; border-top: 1px solid #eee;\">" +
               "<table style=\"width: 100%;\"><tr>" +
               "<td style=\"vertical-align: middle;\">This is a system generated document. Scan the QR code to verify.</td>" +
               "<td style=\"text-align: right; width: 60px;\">{{QR_CODE}}</td>" +
               "</tr></table>" +
               "</div>" +
               "</div>";
    }

    private static final List<TemplateDefinition> SYSTEM_TEMPLATES = List.of(
            // Documents
            createSystemTemplate("OFFER_LETTER", "Offer Letter", "DOCUMENT", getDocumentHtml("Offer Letter", 
                "<p>We are pleased to offer you the position of <strong>{{DESIGNATION}}</strong> in our organization. We were highly impressed with your qualifications and experience, and we believe that you will be a valuable addition to our team.</p>" +
                "<p>The following are the terms and conditions of our offer:</p>" +
                "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; font-size: 12px;\">" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold; width: 40%;\">Position</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{DESIGNATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Department</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{DEPARTMENT}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Date of Joining</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{JOINING_DATE}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Employment Type</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{EMPLOYMENT_TYPE}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Work Location</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{WORK_LOCATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Annual CTC</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{ANNUAL_CTC}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Probation Period</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{PROBATION_PERIOD}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Reporting To</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{REPORTING_MANAGER}}</td></tr>" +
                "</table>" +
                "<p style=\"color: #1e3a8a; font-weight: bold; font-size: 12px; margin-top: 20px;\">TERMS &amp; CONDITIONS:</p>" +
                "<ol style=\"font-size: 11px; padding-left: 20px; line-height: 1.6;\">" +
                "<li>Your employment will be governed by the policies and procedures of the company.</li>" +
                "<li>You will be on probation for a period of {{PROBATION_PERIOD}} from your date of joining.</li>" +
                "<li>The company reserves the right to terminate your employment during probation with one month's notice.</li>" +
                "<li>You are requested to submit all relevant documents at the time of joining.</li>" +
                "<li>Confidentiality of company information is mandatory.</li>" +
                "<li>This offer is valid subject to successful verification of your documents and background.</li>" +
                "</ol>" +
                "<p>Please sign and return a copy of this letter as a token of your acceptance of this offer.</p>" +
                "<p>We look forward to having you on our team and wish you a successful career with us.</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("APPOINTMENT_LETTER", "Appointment Letter", "DOCUMENT", getDocumentHtml("Appointment Letter", 
                "<p>We are pleased to inform you that you are hereby appointed as <strong>{{DESIGNATION}}</strong> in our organization on the terms and conditions mentioned below.</p>" +
                "<p>You will be a part of our <strong>{{DEPARTMENT}}</strong> Department and report to <strong>{{REPORTING_MANAGER}}</strong>.</p>" +
                "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; font-size: 12px;\">" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold; width: 40%;\">Position</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{DESIGNATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Date of Appointment</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{JOINING_DATE}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Annual CTC</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{ANNUAL_CTC}}</td></tr>" +
                "</table>" +
                "<p>You are expected to comply with company policies and procedures at all times. Welcome to the team!</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("JOINING_LETTER", "Joining Letter", "DOCUMENT", getDocumentHtml("Joining Letter", 
                "<p>We are delighted to confirm that you have joined <strong>{{COMPANY_NAME}}</strong> as <strong>{{DESIGNATION}}</strong> on <strong>{{JOINING_DATE}}</strong>.</p>" +
                "<p>We welcome you to the team and look forward to a successful association.</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("EXPERIENCE_LETTER", "Experience Letter", "DOCUMENT", getDocumentHtml("Experience Letter", 
                "<h4 style=\"text-align: center; text-decoration: underline;\">TO WHOMSOEVER IT MAY CONCERN</h4>" +
                "<p>This is to certify that <strong>{{EMPLOYEE_NAME}}</strong> (Employee ID: {{EMPLOYEE_ID}}) has worked with <strong>{{COMPANY_NAME}}</strong> from <strong>{{START_DATE}}</strong> to <strong>{{END_DATE}}</strong>.</p>" +
                "<p>During their tenure, they were designated as <strong>{{DESIGNATION}}</strong> in the <strong>{{DEPARTMENT}}</strong> Department.</p>" +
                "<p>We found them sincere, hardworking, and dedicated to their responsibilities.</p>" +
                "<p>We wish them all the best for their future endeavors.</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("RELIEVING_LETTER", "Relieving Letter", "DOCUMENT", getDocumentHtml("Relieving Letter", 
                "<h4 style=\"text-align: center; text-decoration: underline;\">TO WHOMSOEVER IT MAY CONCERN</h4>" +
                "<p>This is to certify that <strong>{{EMPLOYEE_NAME}}</strong> (Employee ID: {{EMPLOYEE_ID}}) has been relieved from the services of <strong>{{COMPANY_NAME}}</strong> on <strong>{{RELIEVING_DATE}}</strong>.</p>" +
                "<p>They have successfully completed the handover of their responsibilities.</p>" +
                "<p>We wish them all the best for their future.</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("WARNING_LETTER", "Warning Letter", "DOCUMENT", getDocumentHtml("Warning Letter", 
                "<p>This is to formally warn you regarding the following:</p>" +
                "<div style=\"padding: 15px; border-left: 4px solid #e74c3c; background-color: #fdf5f5; margin: 20px 0;\">" +
                "<strong>Reason:</strong> {{WARNING_REASON}}" +
                "</div>" +
                "<p>You are advised to maintain proper conduct and adhere to company policies. Repeated violations may lead to further disciplinary action.</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("PROMOTION_LETTER", "Promotion Letter", "DOCUMENT", getDocumentHtml("Promotion Letter", 
                "<p>We are pleased to inform you that you have been promoted from <strong>{{OLD_DESIGNATION}}</strong> to <strong>{{NEW_DESIGNATION}}</strong> effective <strong>{{EFFECTIVE_DATE}}</strong>.</p>" +
                "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; font-size: 12px;\">" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold; width: 40%;\">Old Designation</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{OLD_DESIGNATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">New Designation</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{NEW_DESIGNATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Effective Date</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{EFFECTIVE_DATE}}</td></tr>" +
                "</table>" +
                "<p>Congratulations on your achievement! We look forward to your continued contribution to the organization.</p>" +
                "<p>Sincerely,</p>"
            )),
            createSystemTemplate("TRANSFER_LETTER", "Transfer Letter", "DOCUMENT", getDocumentHtml("Transfer Letter", 
                "<p>This is to inform you that you are hereby transferred from <strong>{{OLD_LOCATION}}</strong> to <strong>{{NEW_LOCATION}}</strong> effective <strong>{{TRANSFER_DATE}}</strong>.</p>" +
                "<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; font-size: 12px;\">" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold; width: 40%;\">From Location</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{OLD_LOCATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">To Location</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{NEW_LOCATION}}</td></tr>" +
                "<tr><td style=\"border: 1px solid #ccc; padding: 8px; font-weight: bold;\">Effective Date</td><td style=\"border: 1px solid #ccc; padding: 8px;\">{{TRANSFER_DATE}}</td></tr>" +
                "</table>" +
                "<p>All other terms and conditions of your employment remain unchanged.</p>" +
                "<p>Sincerely,</p>"
            )),

            // Certificates (Passing specific border colors)
            createSystemTemplate("INTERNSHIP_CERTIFICATE", "Internship Certificate", "CERTIFICATE", getCertificateHtml("Internship Certificate", 
                "has successfully completed an Internship as a <strong>{{DESIGNATION}}</strong><br/><br/>from <strong>{{START_DATE}}</strong> to <strong>{{END_DATE}}</strong><br/>with {{COMPANY_NAME}}.<br/><br/>We appreciate their efforts and wish them all the best for their future.", "#f59e0b" // Amber
            )),
            createSystemTemplate("COURSE_COMPLETION_CERTIFICATE", "Course Completion Certificate", "CERTIFICATE", getCertificateHtml("Course Completion Certificate", 
                "has successfully completed the course<br/><br/><strong>{{COURSE_NAME}}</strong><br/><br/>from {{START_DATE}} to {{END_DATE}}<br/>conducted by {{COMPANY_NAME}}.<br/><br/>We wish them all the best for their future endeavors.", "#10b981" // Emerald Green
            )),
            createSystemTemplate("TRAINING_CERTIFICATE", "Training Certificate", "CERTIFICATE", getCertificateHtml("Training Certificate", 
                "has successfully completed the training on<br/><br/><strong>{{TRAINING_NAME}}</strong><br/><br/>from {{START_DATE}} to {{END_DATE}}<br/>conducted by {{COMPANY_NAME}}.", "#3b82f6" // Blue
            )),
            createSystemTemplate("ACHIEVEMENT_CERTIFICATE", "Achievement Certificate", "CERTIFICATE", getCertificateHtml("Certificate of Achievement", 
                "is awarded this certificate for<br/><br/><strong style=\"font-size: 24px;\">{{ACHIEVEMENT_NAME}}</strong><br/><br/>and exceptional contribution towards<br/>the success of the project.", "#ef4444" // Red
            )),
            createSystemTemplate("PARTICIPATION_CERTIFICATE", "Participation Certificate", "CERTIFICATE", getCertificateHtml("Certificate of Participation", 
                "has participated in the<br/><br/><strong>{{EVENT_NAME}}</strong><br/><br/>held on {{EVENT_DATE}}.<br/><br/>We appreciate your active participation.", "#8b5cf6" // Purple
            )),
            createSystemTemplate("EMPLOYEE_RECOGNITION_CERTIFICATE", "Employee Recognition Certificate", "CERTIFICATE", getCertificateHtml("Employee Recognition", 
                "is recognized for their dedication, hard work<br/>and valuable contribution to the organization.<br/><br/>Thank you for being an inspiration!", "#eab308" // Gold
            ))
    );

    public List<TemplateDefinition> getAvailableSystemTemplates() {
        return SYSTEM_TEMPLATES;
    }

    @Transactional
    public List<TemplateDefinition> importSystemTemplates(List<String> templateCodes) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();
        
        List<TemplateDefinition> imported = new java.util.ArrayList<>();
        
        for (String code : templateCodes) {
            SYSTEM_TEMPLATES.stream()
                    .filter(t -> t.getTemplateCode().equals(code))
                    .findFirst()
                    .ifPresent(sys -> {
                        var existingOpt = repository.findByTenantIdAndTemplateCode(tenantId, code);
                        if (existingOpt.isEmpty()) {
                            TemplateDefinition newTemp = new TemplateDefinition();
                            newTemp.setTenantId(tenantId);
                            newTemp.setTemplateCode(sys.getTemplateCode());
                            newTemp.setTemplateName(sys.getTemplateName());
                            newTemp.setTemplateType(sys.getTemplateType());
                            newTemp.setContentHtml(sys.getContentHtml());
                            newTemp.setIsSystemTemplate(sys.getIsSystemTemplate());
                            newTemp.setIsEditable(sys.getIsEditable());
                            newTemp.setCreatedBy(username);
                            imported.add(repository.save(newTemp));
                        } else {
                            TemplateDefinition existing = existingOpt.get();
                            if (Boolean.TRUE.equals(existing.getIsSystemTemplate())) {
                                existing.setContentHtml(sys.getContentHtml());
                                existing.setTemplateName(sys.getTemplateName());
                                repository.save(existing);
                            }
                        }
                    });
        }
        return imported;
    }

    public String uploadBackgroundImage(org.springframework.web.multipart.MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = java.util.UUID.randomUUID().toString() + extension;
            
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "backgrounds");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            
            java.nio.file.Path filePath = uploadPath.resolve(fileName);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return backendUrl + "/uploads/backgrounds/" + fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to store background image", e);
        }
    }
}
