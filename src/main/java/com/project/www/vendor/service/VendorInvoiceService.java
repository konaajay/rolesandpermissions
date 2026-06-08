package com.project.www.vendor.service;

import com.project.www.vendor.dto.VendorInvoiceDto;
import java.util.List;

public interface VendorInvoiceService {
    VendorInvoiceDto createInvoice(VendorInvoiceDto dto);
    VendorInvoiceDto updateInvoice(Long id, VendorInvoiceDto dto);
    VendorInvoiceDto getInvoiceById(Long id);
    List<VendorInvoiceDto> getAllInvoices();
    void deleteInvoice(Long id);
}
