package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("marketingModuleLeadRepository")
public interface LeadRepository extends JpaRepository<Lead, Long> {
    boolean existsByEmailAndBatchId(String email, Long batchId);
    boolean existsByEmailAndCourseInterest(String email, String courseInterest);
    long countByUtmSourceAndUtmCampaign(String utmSource, String utmCampaign);

}
