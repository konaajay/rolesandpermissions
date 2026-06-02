package com.project.www.mapper;

import com.project.www.dto.VendorDto;
import com.project.www.entity.Vendor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    VendorDto toDto(Vendor vendor);

    @Mapping(target = "category", ignore = true)
    Vendor toEntity(VendorDto vendorDto);

    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(VendorDto vendorDto, @MappingTarget Vendor vendor);
}
