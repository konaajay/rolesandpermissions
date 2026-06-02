package com.project.www.service;

import com.project.www.dto.VendorDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface VendorService {

    VendorDto createVendor(VendorDto vendorDto);

    VendorDto updateVendor(Long id, VendorDto vendorDto);

    VendorDto getVendorById(Long id);

    Page<VendorDto> getAllVendors(int page, int size, String sortBy, String sortDir);

    Page<VendorDto> searchVendors(String searchTerm, int page, int size, String sortBy, String sortDir);

    void softDeleteVendor(Long id);

    VendorDto toggleVendorStatus(Long id);

    VendorDto uploadVendorDocument(Long id, MultipartFile file);
}
