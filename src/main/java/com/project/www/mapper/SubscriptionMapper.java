package com.project.www.mapper;

import com.project.www.dto.SubscriptionResponse;
import com.project.www.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    SubscriptionResponse toDto(Subscription entity);
}
