package com.project.www.marketing.repository;

import com.project.www.marketing.repository.LandingPageRepository;

import com.project.www.marketing.entity.LandingPage;

import com.project.www.enums.*;

import com.project.www.marketing.entity.LandingPage;

public interface LandingPageRepository
        extends org.springframework.data.jpa.repository.JpaRepository<LandingPage, Long> {
    java.util.Optional<LandingPage> findBySlug(String slug);
}
