package com.project.www.repository;

import com.project.www.enums.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.entity.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByCampaignCampaignId(Long campaignId);
}
