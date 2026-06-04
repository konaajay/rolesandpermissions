package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.LandingPage;

public interface LandingPageRepository
        extends org.springframework.data.jpa.repository.JpaRepository<LandingPage, Long> {
    java.util.Optional<LandingPage> findBySlug(String slug);
}
