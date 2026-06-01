package com.project.www.service;

import com.project.www.dto.GenerateCertificateDto;
import com.project.www.entity.EmployeeCertificate;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CertificateService {
    EmployeeCertificate generateCertificate(Long tenantId, GenerateCertificateDto dto) throws Exception;
    List<EmployeeCertificate> getAllCertificates(Long tenantId);
    EmployeeCertificate getCertificateById(Long tenantId, Long id);
    EmployeeCertificate revokeCertificate(Long tenantId, Long id);
    byte[] downloadCertificatePdf(Long tenantId, Long id) throws Exception;
    
    // Public verification
    Object verifyCertificate(String identifier);
    String previewCertificateHtml(Long tenantId, GenerateCertificateDto dto) throws Exception;
}
