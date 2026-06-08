package com.project.www.vendor.service;

import com.project.www.vendor.dto.VendorAuditDto;
import java.util.List;

public interface VendorAuditService {
    VendorAuditDto createAudit(VendorAuditDto dto);
    VendorAuditDto updateAudit(Long id, VendorAuditDto dto);
    VendorAuditDto getAuditById(Long id);
    List<VendorAuditDto> getAllAudits();
    void deleteAudit(Long id);
}
