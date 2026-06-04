package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.CampaignPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface PerformanceRepository extends JpaRepository<CampaignPerformance, Long> {
    Optional<CampaignPerformance> findByCampaignCampaignIdAndRecordedDate(Long campaignId, LocalDate recordedDate);
}
