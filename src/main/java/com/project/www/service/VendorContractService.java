package com.project.www.service;

import com.project.www.dto.VendorContractDto;
import java.util.List;

public interface VendorContractService {
    VendorContractDto createContract(VendorContractDto dto);
    VendorContractDto updateContract(Long id, VendorContractDto dto);
    VendorContractDto getContractById(Long id);
    List<VendorContractDto> getAllContracts();
    void deleteContract(Long id);
}
