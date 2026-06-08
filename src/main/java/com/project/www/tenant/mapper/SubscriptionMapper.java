package com.project.www.tenant.mapper;

import com.project.www.tenant.dto.SubscriptionResponse;
import com.project.www.tenant.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    SubscriptionResponse toDto(Subscription entity);
}
