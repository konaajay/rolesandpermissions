package com.project.www.mapper;

import com.project.www.dto.PurchaseOrderDto;
import com.project.www.entity.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PurchaseOrderMapper {

    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "vendor.vendorName", target = "vendorName")
    @Mapping(source = "orderDate", target = "date")
    PurchaseOrderDto toDto(PurchaseOrder entity);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(source = "date", target = "orderDate")
    PurchaseOrder toEntity(PurchaseOrderDto dto);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(source = "date", target = "orderDate")
    void updateEntityFromDto(PurchaseOrderDto dto, @MappingTarget PurchaseOrder entity);
}
