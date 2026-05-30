package com.project.www.service.impl;

import com.project.www.dto.GenerateCertificateDto;
import com.project.www.dto.PublicVerificationDto;
import com.project.www.entity.EmployeeCertificate;
import com.project.www.entity.TemplateDefinition;
import com.project.www.entity.User;
import com.project.www.repository.CompanyProfileRepository;
import com.project.www.repository.EmployeeCertificateRepository;
import com.project.www.repository.TemplateDefinitionRepository;
import com.project.www.repository.UserRepository;
import com.project.www.service.CertificateService;
import com.project.www.service.PdfAndQrGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private EmployeeCertificateRepository certificateRepository;

    @Autowired
    private TemplateDefinitionRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyProfileRepository companyProfileRepository;

    @Autowired
    private PdfAndQrGenerationService pdfAndQrService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public EmployeeCertificate generateCertificate(Long tenantId, GenerateCertificateDto dto) throws Exception {
        User employee = userRepository.findByTenantIdAndEmployeeId(tenantId, dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        TemplateDefinition template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (!template.getTenantId().equals(tenantId) && !template.getIsSystemTemplate()) {
            throw new RuntimeException("Template not accessible for this tenant");
        }

        String certificateNo = generateCertificateNumber(tenantId);
        String verificationToken = UUID.randomUUID().toString();

        EmployeeCertificate certificate = new EmployeeCertificate();
        certificate.setTenantId(tenantId);
        certificate.setCertificateNo(certificateNo);
        certificate.setEmployeeId(employee.getEmployeeId());
        certificate.setTemplate(template);
        certificate.setIssuedDate(dto.getIssuedDate() != null ? dto.getIssuedDate() : LocalDateTime.now());
        certificate.setExpiryDate(dto.getExpiryDate());
        certificate.setVerificationToken(verificationToken);
        certificate.setStatus("ACTIVE");

        return certificateRepository.save(certificate);
    }

    private String generateCertificateNumber(Long tenantId) {
        return "CERT-" + LocalDateTime.now().getYear() + "-" + (System.currentTimeMillis() % 100000);
    }

    @Override
    public List<EmployeeCertificate> getAllCertificates(Long tenantId) {
        return certificateRepository.findByTenantId(tenantId);
    }

    @Override
    public EmployeeCertificate getCertificateById(Long tenantId, Long id) {
        return certificateRepository.findById(id)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
    }

    @Override
    public EmployeeCertificate revokeCertificate(Long tenantId, Long id) {
        EmployeeCertificate cert = getCertificateById(tenantId, id);
        cert.setStatus("REVOKED");
        return certificateRepository.save(cert);
    }

    @Override
    public byte[] downloadCertificatePdf(Long tenantId, Long id) throws Exception {
        EmployeeCertificate cert = getCertificateById(tenantId, id);
        
        User employee = userRepository.findByTenantIdAndEmployeeId(tenantId, cert.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String verificationUrl = frontendUrl + "/verify/" + cert.getVerificationToken();
        String qrCodeBase64 = pdfAndQrService.generateQrCodeBase64(verificationUrl);
        String qrImageTag = "<img src=\"" + qrCodeBase64 + "\" width=\"100\" height=\"100\" />";

        String htmlContent = cert.getTemplate().getContentHtml();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        // Fetch company profile if present
        var companyProfileOpt = companyProfileRepository.findByTenantId(tenantId);
        String companyName = companyProfileOpt.isPresent() && companyProfileOpt.get().getCompanyName() != null ? companyProfileOpt.get().getCompanyName() : "Enterprise SaaS Pvt Ltd";
        String companyAddress = companyProfileOpt.isPresent() && companyProfileOpt.get().getAddressLine1() != null ? companyProfileOpt.get().getAddressLine1() : "Default Address";
        String companyLogo = companyProfileOpt.isPresent() && companyProfileOpt.get().getLogoUrl() != null && !companyProfileOpt.get().getLogoUrl().isBlank() ? "<img src=\"" + companyProfileOpt.get().getLogoUrl() + "\" height=\"50\" />" : "<b>" + companyName + "</b>";
        String companyStamp = companyProfileOpt.isPresent() && companyProfileOpt.get().getStampUrl() != null && !companyProfileOpt.get().getStampUrl().isBlank() ? "<img src=\"" + companyProfileOpt.get().getStampUrl() + "\" height=\"80\" />" : "<div style=\"width: 80px; height: 80px; border: 2px dashed #ccc; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; color: #ccc; font-weight: bold; font-size: 12px;\">STAMP</div>";
        String companySignature = companyProfileOpt.isPresent() && companyProfileOpt.get().getSignatureUrl() != null && !companyProfileOpt.get().getSignatureUrl().isBlank() ? "<img src=\"" + companyProfileOpt.get().getSignatureUrl() + "\" height=\"40\" />" : "";

        htmlContent = htmlContent
                .replace("{{certificateNo}}", cert.getCertificateNo())
                .replace("{{CERTIFICATE_NO}}", cert.getCertificateNo())
                .replace("{{DOCUMENT_NO}}", cert.getCertificateNo())
                .replace("{{COMPANY_NAME}}", companyName)
                .replace("{{COMPANY_LOGO}}", companyLogo)
                .replace("{{COMPANY_SIGNATURE}}", companySignature)
                .replace("{{COMPANY_STAMP}}", companyStamp)
                .replace("{{COMPANY_ADDRESS}}", companyAddress)
                .replace("{{employeeName}}", employee.getFirstName() + " " + employee.getLastName())
                .replace("{{EMPLOYEE_NAME}}", employee.getFirstName() + " " + employee.getLastName())
                .replace("{{FIRST_NAME}}", employee.getFirstName() != null ? employee.getFirstName() : "")
                .replace("{{LAST_NAME}}", employee.getLastName() != null ? employee.getLastName() : "")
                .replace("{{EMPLOYEE_ID}}", employee.getEmployeeId())
                .replace("{{employeeId}}", employee.getEmployeeId() != null ? employee.getEmployeeId() : "N/A")
                .replace("{{EMPLOYEE_ADDRESS}}", "Registered Address on File")
                .replace("{{DESIGNATION}}", employee.getDesignation() != null ? employee.getDesignation() : "N/A")
                .replace("{{designation}}", employee.getDesignation() != null ? employee.getDesignation() : "N/A")
                .replace("{{DEPARTMENT}}", employee.getDepartment() != null ? employee.getDepartment() : "N/A")
                .replace("{{department}}", employee.getDepartment() != null ? employee.getDepartment() : "N/A")
                .replace("{{WORK_LOCATION}}", employee.getAssignedOffice() != null ? employee.getAssignedOffice().getName() : "Head Office")
                .replace("{{JOINING_DATE}}", employee.getJoiningDate() != null ? employee.getJoiningDate().format(formatter) : cert.getIssuedDate().format(formatter))
                .replace("{{EMPLOYMENT_TYPE}}", "Full-Time")
                .replace("{{PROBATION_PERIOD}}", "6 Months")
                .replace("{{ANNUAL_CTC}}", "Not Disclosed")
                .replace("{{REPORTING_MANAGER}}", employee.getManager() != null ? employee.getManager().getName() : "HR Department")
                .replace("{{ISSUE_DATE}}", cert.getIssuedDate().format(formatter))
                .replace("{{issueDate}}", cert.getIssuedDate().format(formatter))
                .replace("{{START_DATE}}", cert.getIssuedDate().format(formatter)) // default to issue date if start date not stored
                .replace("{{END_DATE}}", cert.getExpiryDate() != null ? cert.getExpiryDate().format(formatter) : "Present")
                .replace("{{EXPIRY_DATE}}", cert.getExpiryDate() != null ? cert.getExpiryDate().format(formatter) : "Present")
                .replace("{{SIGNATORY_NAME}}", "Authorized Signatory")
                .replace("{{SIGNATORY_DESIGNATION}}", "HR Manager")
                .replace("{{QR_CODE}}", qrImageTag)
                .replace("{{VERIFICATION_URL}}", verificationUrl)
                .replace("{{verificationUrl}}", qrImageTag);

        boolean isLandscape = "CERTIFICATE".equalsIgnoreCase(cert.getTemplate().getTemplateType());
        return pdfAndQrService.generatePdfFromHtml(htmlContent, isLandscape);
    }

    @Override
    public Object verifyCertificate(String identifier) {
        EmployeeCertificate cert;
        // Try finding by token first
        var byToken = certificateRepository.findByVerificationToken(identifier);
        if (byToken.isPresent()) {
            cert = byToken.get();
        } else {
            // Try by certificate number
            cert = certificateRepository.findByCertificateNo(identifier)
                    .orElseThrow(() -> new RuntimeException("Certificate Not Found"));
        }

        User employee = userRepository.findByTenantIdAndEmployeeId(cert.getTenantId(), cert.getEmployeeId())
                .orElse(null);

        PublicVerificationDto dto = new PublicVerificationDto();
        dto.setCertificateNo(cert.getCertificateNo());
        dto.setEmployeeName(employee != null ? (employee.getFirstName() + " " + employee.getLastName()) : "Unknown");
        dto.setCertificateType(cert.getTemplate().getTemplateName());
        dto.setIssuedDate(cert.getIssuedDate());
        dto.setIssuedBy("Enterprise SaaS Pvt Ltd");
        dto.setStatus(cert.getStatus());

        return dto;
    }
}
