package com.project.www.service;

import com.project.www.enums.*;

import com.project.www.entity.LandingPage;
import com.project.www.repository.LandingPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingPageService {

    private final LandingPageRepository landingPageRepository;

    public LandingPage getBySlug(String slug) {
        return landingPageRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Landing page not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public List<LandingPage> getAll() {
        return landingPageRepository.findAll();
    }

    @Transactional
    public LandingPage create(LandingPage page) {
        if (landingPageRepository.findBySlug(page.getSlug()).isPresent()) {
            throw new RuntimeException("Slug already exists: " + page.getSlug());
        }
        return landingPageRepository.save(page);
    }

    @Transactional
    public LandingPage update(Long id, LandingPage page) {
        LandingPage existing = landingPageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landing page not found with id: " + id));

        existing.setTitle(page.getTitle());
        existing.setHeadline(page.getHeadline());
        existing.setSubtitle(page.getSubtitle());
        existing.setPrice(page.getPrice());
        existing.setAdBudget(page.getAdBudget());
        existing.setVideoUrl(page.getVideoUrl());
        existing.setFeatures(page.getFeatures());
        existing.setCtaText(page.getCtaText());
        
        // Note: Usually we don't allow changing slug to avoid breaking links, 
        // but if required, we'd check for uniqueness here.
        if (page.getSlug() != null && !page.getSlug().equals(existing.getSlug())) {
             if (landingPageRepository.findBySlug(page.getSlug()).isPresent()) {
                throw new RuntimeException("New slug already exists: " + page.getSlug());
            }
            existing.setSlug(page.getSlug());
        }

        return landingPageRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!landingPageRepository.existsById(id)) {
            throw new RuntimeException("Landing page not found with id: " + id);
        }
        landingPageRepository.deleteById(id);
    }

    @Transactional
    public void seedDefaults() {
        if (landingPageRepository.count() > 0) {
            log.info("Landing pages already exist, skipping seed.");
            return;
        }

        List<LandingPage> defaults = new ArrayList<>();

        LandingPage main = new LandingPage();
        main.setSlug("main-course-platform");
        main.setTitle("LMS Elite Learning");
        main.setHeadline("Master Your Future with Our Cloud-Based Solutions");
        main.setSubtitle("Join 5000+ students already learning with us.");
        main.setCtaText("Get Started Now");
        main.setFeatures(List.of("100% Online", "Certified Instructors", "Lifetime Access"));
        defaults.add(main);

        LandingPage affiliate = new LandingPage();
        affiliate.setSlug("partner-program");
        affiliate.setTitle("Become a Learning Partner");
        affiliate.setHeadline("Earn Rewards by Sharing Knowledge");
        affiliate.setSubtitle("Join our growing affiliate network.");
        affiliate.setCtaText("Join Program");
        affiliate.setFeatures(List.of("High Commission", "Real-time Tracking", "Marketing Support"));
        defaults.add(affiliate);

        landingPageRepository.saveAll(defaults);
        log.info("Seeded {} default landing pages.", defaults.size());
    }
}
