package com.project.www.mapper;

import com.project.www.dto.VendorContractDto;
import com.project.www.entity.VendorContract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorContractMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.vendorName", target = "vendorName")
    @Mapping(source = "amount", target = "amountValue")
    VendorContractDto toDto(VendorContract entity);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(source = "amountValue", target = "amount")
    VendorContract toEntity(VendorContractDto dto);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(source = "amountValue", target = "amount")
    void updateEntityFromDto(VendorContractDto dto, @MappingTarget VendorContract entity);
}
