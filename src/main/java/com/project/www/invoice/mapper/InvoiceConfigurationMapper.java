package com.project.www.invoice.mapper;

import com.project.www.invoice.dto.InvoiceConfigurationDto;
import com.project.www.invoice.entity.InvoiceConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvoiceConfigurationMapper {
    InvoiceConfigurationDto toDto(InvoiceConfiguration entity);
    InvoiceConfiguration toEntity(InvoiceConfigurationDto dto);
    void updateEntityFromDto(InvoiceConfigurationDto dto, @MappingTarget InvoiceConfiguration entity);
}
