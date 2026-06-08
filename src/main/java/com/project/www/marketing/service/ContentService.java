package com.project.www.marketing.service;

import com.project.www.marketing.service.ContentService;

import com.project.www.marketing.entity.Content;

import com.project.www.enums.*;

import com.project.www.marketing.entity.Content;
import com.project.www.marketing.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @SuppressWarnings("null")
    public @org.springframework.lang.NonNull Content createContent(@org.springframework.lang.NonNull Content content) {
        return contentRepository.save(content);
    }

    public List<Content> findByCampaign(Long campaignId) {
        return contentRepository.findByCampaignCampaignId(campaignId);
    }

    @SuppressWarnings("null")
    public @org.springframework.lang.NonNull Content updateContent(@org.springframework.lang.NonNull Long id,
            @org.springframework.lang.NonNull Content updated) {
        Content existing = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found: " + id));
        if (updated.getContentTitle() != null)
            existing.setContentTitle(updated.getContentTitle());
        if (updated.getContentType() != null)
            existing.setContentType(updated.getContentType());
        if (updated.getContentUrl() != null)
            existing.setContentUrl(updated.getContentUrl());
        if (updated.getPlatform() != null)
            existing.setPlatform(updated.getPlatform());
        return contentRepository.save(existing);
    }

    public void deleteContent(@org.springframework.lang.NonNull Long id) {
        contentRepository.deleteById(id);
    }
}
