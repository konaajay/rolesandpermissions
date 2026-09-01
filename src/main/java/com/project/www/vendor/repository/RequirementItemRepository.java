package com.project.www.vendor.repository;

import com.project.www.vendor.entity.RequirementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementItemRepository extends JpaRepository<RequirementItem, Long> {
}
