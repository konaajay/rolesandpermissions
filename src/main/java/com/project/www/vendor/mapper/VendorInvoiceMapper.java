package com.project.www.vendor.mapper;

import com.project.www.vendor.dto.VendorInvoiceDto;
import com.project.www.vendor.entity.VendorInvoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {VendorInvoiceItemMapper.class})
public interface VendorInvoiceMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.vendorName", target = "vendorName")
    @Mapping(source = "requirement.id", target = "requirementId")
    @Mapping(source = "amount", target = "amountValue")
    @Mapping(source = "invoiceDate", target = "date")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "invoiceNumber", source = "id", qualifiedByName = "idToInv")
    VendorInvoiceDto toDto(VendorInvoice entity);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "requirement", ignore = true)
    @Mapping(source = "amountValue", target = "amount")
    @Mapping(source = "date", target = "invoiceDate")
    VendorInvoice toEntity(VendorInvoiceDto dto);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "requirement", ignore = true)
    @Mapping(source = "amountValue", target = "amount")
    @Mapping(source = "date", target = "invoiceDate")
    void updateEntityFromDto(VendorInvoiceDto dto, @MappingTarget VendorInvoice entity);
    
    @org.mapstruct.Named("idToInv")
    default String idToInv(Long id) {
        return id != null ? "INV-" + (4000 + id) : null;
    }
}
