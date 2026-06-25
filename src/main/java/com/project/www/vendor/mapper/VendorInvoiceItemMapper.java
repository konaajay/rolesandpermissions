package com.project.www.vendor.mapper;

import com.project.www.vendor.dto.VendorInvoiceItemDto;
import com.project.www.vendor.entity.VendorInvoiceItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VendorInvoiceItemMapper {
    VendorInvoiceItemDto toDto(VendorInvoiceItem entity);
    VendorInvoiceItem toEntity(VendorInvoiceItemDto dto);
}
