package com.project.www.mapper;

import com.project.www.dto.VendorCategoryDto;
import com.project.www.entity.VendorCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorCategoryMapper {
    VendorCategoryDto toDto(VendorCategory entity);
    VendorCategory toEntity(VendorCategoryDto dto);
    void updateEntityFromDto(VendorCategoryDto dto, @MappingTarget VendorCategory entity);
}
