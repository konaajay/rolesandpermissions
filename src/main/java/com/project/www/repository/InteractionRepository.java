package com.project.www.repository;

import com.project.www.enums.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.entity.Interaction;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByCampaignCampaignId(Long campaignId);

    List<Interaction> findByCustomerEmail(String customerEmail);
}
