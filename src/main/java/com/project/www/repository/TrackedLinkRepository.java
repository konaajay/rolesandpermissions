package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.TrackedLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TrackedLinkRepository extends JpaRepository<TrackedLink, Long> {
    Optional<TrackedLink> findByTrackedLinkId(String trackedLinkId);
    List<TrackedLink> findAllByOrderByTimestampDesc();
}
