package com.project.www.vendor.service;

import com.project.www.vendor.dto.VendorContractDto;
import java.util.List;

public interface VendorContractService {
    VendorContractDto createContract(VendorContractDto dto);
    VendorContractDto updateContract(Long id, VendorContractDto dto);
    VendorContractDto getContractById(Long id);
    List<VendorContractDto> getAllContracts();
    void deleteContract(Long id);
}
