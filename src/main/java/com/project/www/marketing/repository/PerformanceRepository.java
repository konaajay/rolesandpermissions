package com.project.www.marketing.repository;

import com.project.www.marketing.repository.PerformanceRepository;

import com.project.www.marketing.entity.CampaignPerformance;

import com.project.www.enums.*;

import com.project.www.marketing.entity.CampaignPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface PerformanceRepository extends JpaRepository<CampaignPerformance, Long> {
    Optional<CampaignPerformance> findByCampaignCampaignIdAndRecordedDate(Long campaignId, LocalDate recordedDate);
}
