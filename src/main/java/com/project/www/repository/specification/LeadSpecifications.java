package com.project.www.repository.specification;

import com.project.www.enums.*;

import com.project.www.entity.Lead;
import com.project.www.enums.LeadStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class LeadSpecifications {

    public static Specification<Lead> hasCampaignId(Long campaignId) {
        return (root, query, cb) -> campaignId == null ? null
                : cb.equal(root.get("campaign").get("campaignId"), campaignId);
    }

    public static Specification<Lead> hasAssignedToUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("assignedToUserId"), userId);
    }

    public static Specification<Lead> hasStatus(LeadStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Lead> hasSource(String source) {
        return (root, query, cb) -> source == null ? null : cb.equal(root.get("source"), source);
    }

    public static Specification<Lead> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null)
                return null;
            if (start != null && end != null)
                return cb.between(root.get("createdAt"), start, end);
            if (start != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            return cb.lessThanOrEqualTo(root.get("createdAt"), end);
        };
    }
}
