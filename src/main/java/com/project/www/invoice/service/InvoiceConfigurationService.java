package com.project.www.invoice.service;

import com.project.www.invoice.dto.InvoiceConfigurationDto;
import java.util.List;

public interface InvoiceConfigurationService {
    InvoiceConfigurationDto createConfiguration(InvoiceConfigurationDto dto);
    InvoiceConfigurationDto updateConfiguration(Long id, InvoiceConfigurationDto dto);
    void deleteConfiguration(Long id);
    InvoiceConfigurationDto getConfigurationById(Long id);
    List<InvoiceConfigurationDto> getAllConfigurationsForTenant();
    InvoiceConfigurationDto getActiveConfigurationForTenant();
    void activateConfiguration(Long id);
}
