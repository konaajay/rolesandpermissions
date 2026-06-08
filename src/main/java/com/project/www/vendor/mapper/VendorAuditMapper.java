package com.project.www.vendor.mapper;

import com.project.www.vendor.dto.VendorAuditDto;
import com.project.www.vendor.entity.VendorAudit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorAuditMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.vendorName", target = "vendorName")
    VendorAuditDto toDto(VendorAudit entity);

    @Mapping(target = "vendor", ignore = true)
    VendorAudit toEntity(VendorAuditDto dto);

    @Mapping(target = "vendor", ignore = true)
    void updateEntityFromDto(VendorAuditDto dto, @MappingTarget VendorAudit entity);
}
