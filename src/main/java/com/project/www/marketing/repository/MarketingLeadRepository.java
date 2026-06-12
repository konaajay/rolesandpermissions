package com.project.www.marketing.repository;

import com.project.www.marketing.entity.MarketingLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingLeadRepository extends JpaRepository<MarketingLead, Long> {
}
