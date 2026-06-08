package com.project.www.marketing.repository;

import com.project.www.marketing.repository.CampaignRepository;

import com.project.www.marketing.entity.Campaign;

import com.project.www.enums.*;

import com.project.www.marketing.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    java.util.Optional<Campaign> findByCampaignName(String campaignName);
}
