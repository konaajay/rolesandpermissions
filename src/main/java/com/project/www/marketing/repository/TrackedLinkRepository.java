package com.project.www.marketing.repository;

import com.project.www.marketing.repository.TrackedLinkRepository;

import com.project.www.marketing.entity.TrackedLink;

import com.project.www.enums.*;

import com.project.www.marketing.entity.TrackedLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TrackedLinkRepository extends JpaRepository<TrackedLink, Long> {
    Optional<TrackedLink> findByTrackedLinkId(String trackedLinkId);
    List<TrackedLink> findAllByOrderByTimestampDesc();
}
