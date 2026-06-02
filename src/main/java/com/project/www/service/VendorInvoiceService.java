package com.project.www.service;

import com.project.www.dto.VendorInvoiceDto;
import java.util.List;

public interface VendorInvoiceService {
    VendorInvoiceDto createInvoice(VendorInvoiceDto dto);
    VendorInvoiceDto updateInvoice(Long id, VendorInvoiceDto dto);
    VendorInvoiceDto getInvoiceById(Long id);
    List<VendorInvoiceDto> getAllInvoices();
    void deleteInvoice(Long id);
}
