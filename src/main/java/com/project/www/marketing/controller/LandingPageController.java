package com.project.www.marketing.controller;

import com.project.www.marketing.controller.LandingPageController;

import com.project.www.marketing.entity.LandingPage;

import com.project.www.enums.*;

import com.project.www.marketing.entity.LandingPage;
import com.project.www.marketing.service.LandingPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/marketing")
@RequiredArgsConstructor

public class LandingPageController {

    private final LandingPageService landingPageService;

    // =========================
    // PUBLIC LANDING PAGE VIEW
    // =========================
    @GetMapping("/public/landing/{slug}")
    public ResponseEntity<LandingPage> getPageBySlug(@PathVariable String slug) {
        log.info("PUBLIC_FETCH_LANDING | Slug: {}", slug);
        return ResponseEntity.ok(landingPageService.getBySlug(slug));
    }
}
