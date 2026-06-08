package com.project.www.marketing.repository;

import com.project.www.marketing.repository.ContentRepository;

import com.project.www.marketing.entity.Content;

import com.project.www.enums.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.marketing.entity.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByCampaignCampaignId(Long campaignId);
}
