package com.project.www.marketing.repository;

import com.project.www.marketing.repository.InteractionRepository;

import com.project.www.marketing.entity.Interaction;

import com.project.www.enums.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.marketing.entity.Interaction;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByCampaignCampaignId(Long campaignId);

    List<Interaction> findByCustomerEmail(String customerEmail);
}
