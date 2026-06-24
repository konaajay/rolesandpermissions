package com.project.www.subscription.repository;

import com.project.www.subscription.entity.PlanModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanModuleRepository extends JpaRepository<PlanModule, Long> {
    List<PlanModule> findByPlanId(Long planId);
    List<PlanModule> findByPlanIdAndIsEnabledTrue(Long planId);
    void deleteByPlanId(Long planId);
}
