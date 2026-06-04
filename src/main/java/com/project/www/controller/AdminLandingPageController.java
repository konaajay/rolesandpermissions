package com.project.www.controller;

import com.project.www.enums.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.www.dto.LandingPageRequest;
import com.project.www.entity.LandingPage;
import com.project.www.repository.LandingPageRepository;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/marketing/admin/landing")
@CrossOrigin(origins = "*")
@Transactional
public class AdminLandingPageController {

    @Autowired
    private LandingPageRepository repository;

    @PostMapping
    public ResponseEntity<LandingPage> createPage(@Valid @RequestBody LandingPageRequest request) {
        LandingPage page = new LandingPage();
        mapRequestToEntity(request, page);
        return ResponseEntity.ok(repository.save(page));
    }

    @GetMapping
    public ResponseEntity<List<LandingPage>> getAllPages() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandingPage> updatePage(@PathVariable Long id,
            @Valid @RequestBody LandingPageRequest request) {
        return repository.findById(id)
                .map(page -> {
                    mapRequestToEntity(request, page);
                    return ResponseEntity.ok(repository.save(page));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void mapRequestToEntity(LandingPageRequest request, LandingPage page) {
        page.setTitle(request.getTitle());
        page.setSlug(request.getSlug());
        page.setHeadline(request.getHeadline());
        page.setSubtitle(request.getSubtitle());
        page.setDescription(request.getDescription());
        page.setModuleType(request.getModuleType());
        page.setLandingPageType(request.getLandingPageType());
        page.setPrice(request.getPrice());
        page.setAdBudget(request.getAdBudget());
        page.setVideoUrl(request.getVideoUrl());
        page.setFeatures(request.getFeatures());
        page.setCtaText(request.getCtaText());
    }
}
