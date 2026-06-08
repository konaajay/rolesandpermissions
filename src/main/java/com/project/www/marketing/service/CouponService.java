package com.project.www.marketing.service;

import com.project.www.marketing.service.CouponService;

import com.project.www.accessmanagement.entity.User;

import com.project.www.enums.*;

import com.project.www.marketing.dto.CouponRequest;
import com.project.www.entity.*;
import com.project.www.marketing.entity.*;
import com.project.www.marketing.repository.CouponRepository;
import com.project.www.marketing.repository.CouponUsageRepository;
import com.project.www.marketing.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@lombok.extern.slf4j.Slf4j
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;


    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    @Transactional
    public Coupon createCoupon(CouponRequest req, String creator) {
        if (req == null) throw new IllegalArgumentException("Request cannot be null");

        if (couponRepository.findByCodeAndDeletedFalse(req.getCode()).isPresent()) {
            throw new RuntimeException("Coupon code already exists: " + req.getCode());
        }

        Coupon coupon = mapRequestToCoupon(new Coupon(), req);
        coupon.setCreatedBy(creator);
        
        Coupon saved = couponRepository.save(coupon);
        return saved;
    }

    @Transactional
    public Coupon updateCoupon(Long id, CouponRequest req) {
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + id));

        // Duplicate code validation on update
        Optional<Coupon> checkOther = couponRepository.findByCodeAndDeletedFalse(req.getCode());
        if (checkOther.isPresent() && !checkOther.get().getId().equals(id)) {
            throw new RuntimeException("New coupon code '" + req.getCode() + "' is already taken by another coupon.");
        }

        mapRequestToCoupon(existing, req);
        Coupon saved = couponRepository.save(existing);
        return saved;
    }

    private Coupon mapRequestToCoupon(Coupon c, CouponRequest req) {
        c.setCode(req.getCode().toUpperCase());
        c.setDiscountType(req.getDiscountType());
        c.setDiscountValue(req.getDiscountValue());
        c.setDiscountCap(req.getDiscountCap());
        c.setMaxUsage(req.getMaxUsage());
        c.setMinPurchaseAmount(req.getMinPurchaseAmount() != null ? req.getMinPurchaseAmount() : 0.0);
        c.setPerUserLimit(req.getPerUserLimit() != null ? req.getPerUserLimit() : 1);
        c.setFirstOrderOnly(req.isFirstOrderOnly());
        c.setAutoApply(req.isAutoApply());
        c.setAffiliateId(req.getAffiliateId());
        c.setLearnerId(req.getLearnerId());

        if (req.getExpiryDate() != null && !req.getExpiryDate().isBlank()) {
            try {
                String ds = req.getExpiryDate();
                if (ds.length() == 10) {
                    c.setExpiryDate(LocalDate.parse(ds).atTime(23, 59, 59));
                } else {
                    c.setExpiryDate(LocalDateTime.parse(ds));
                }
            } catch (Exception e) {
                log.error("Date parsing failed: {}", req.getExpiryDate());
            }
        }
        return c;
    }


    public List<Coupon> getAllCoupons() {
        return couponRepository.findByDeletedFalse();
    }

    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCodeAndDeletedFalse(code);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setStatus(CouponStatus.valueOf(status.toUpperCase()));
        couponRepository.save(coupon);
    }

    @Transactional
    public void softDelete(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setDeleted(true);
        coupon.setStatus(CouponStatus.DELETED);
        couponRepository.save(coupon);
    }

    @Transactional
    public void hardDelete(Long id) {
        couponRepository.deleteById(id);
    }

    public boolean validateCoupon(String code, Long courseId, Double purchaseAmount, Long learnerId) {
        try {
            performValidation(code, courseId, purchaseAmount, learnerId);
            return true;
        } catch (Exception e) {
            log.warn("Coupon validation failed for {}: {}", code, e.getMessage());
            return false;
        }
    }

    public void performValidation(String code, Long courseId, Double purchaseAmount, Long learnerId) {
        Coupon coupon = couponRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Coupon '" + code + "' does not exist or has been deleted."));

        if (CouponStatus.ACTIVE != coupon.getStatus()) {
            throw new RuntimeException("Coupon is currently " + (coupon.getStatus() == CouponStatus.EXPIRED ? "expired." : "inactive (paused)."));
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired on " + coupon.getExpiryDate().toLocalDate());
        }

        if (coupon.getMaxUsage() != null && coupon.getUsedCount() >= coupon.getMaxUsage()) {
            throw new RuntimeException("Coupon usage limit reached. It is no longer available.");
        }

        if (learnerId != null) {
            Optional<CouponUsage> usage = couponUsageRepository
                    .findByCouponIdAndLearnerId(coupon.getId(), learnerId);
            if (usage.isPresent() && usage.get().getUsageCount() >= coupon.getPerUserLimit()) {
                throw new RuntimeException("You have already used this coupon " + coupon.getPerUserLimit() + " times.");
            }
        }

        if (purchaseAmount < coupon.getMinPurchaseAmount()) {
            throw new RuntimeException("Minimum purchase of ₹" + coupon.getMinPurchaseAmount() + " required to use this coupon.");
        }

    }

    @Transactional
    public Double applyCoupon(String code, Long courseId, Double purchaseAmount, Long learnerId) {
        log.info("APPLY_COUPON_INIT | Code: {} | Learner: {}", code, learnerId);
        
        // 1. Semantic Validation (thorough)
        performValidation(code, courseId, purchaseAmount, learnerId);
        
        Coupon coupon = couponRepository.findByCodeAndDeletedFalse(code).get();
        double discount = calculateDiscount(code, purchaseAmount);

        // 2. Atomic Global Usage Update (The Concurrency Fix)
        // This query checks 'usedCount < maxUsage' inside the database transaction
        int updated = couponRepository.incrementUsage(coupon.getId());
        log.info("COUPON_INCREMENT_RESULT | ID: {} | Updated Rows: {}", coupon.getId(), updated);
        
        if (updated == 0) {
            log.error("CONCURRENCY_FAIL | Coupon {} exhausted mid-transaction", code);
            throw new RuntimeException("Coupon was just exhausted by another user. No longer available.");
        }

        // 3. Per-User Usage Tracking
        if (learnerId != null) {
            CouponUsage usage = couponUsageRepository
                    .findByCouponIdAndLearnerId(coupon.getId(), learnerId)
                    .orElseGet(() -> {
                        CouponUsage u = new CouponUsage();
                        u.setCouponId(coupon.getId());
                        u.setLearnerId(learnerId);
                        u.setUsageCount(0);
                        return u;
                    });
            usage.setUsageCount(usage.getUsageCount() + 1);
            couponUsageRepository.save(usage);
        }

        log.info("APPLY_COUPON_SUCCESS | Code: {} | Discount: {}", code, discount);
        return discount;
    }

    public Double calculateDiscount(String code, Double purchaseAmount) {
        Optional<Coupon> couponOpt = couponRepository.findByCodeAndDeletedFalse(code);
        if (couponOpt.isEmpty()) return 0.0;
        Coupon coupon = couponOpt.get();
        double discount = 0.0;
        if (DiscountType.PERCENT == coupon.getDiscountType()) {
            discount = purchaseAmount * (coupon.getDiscountValue() / 100.0);
            if (coupon.getDiscountCap() != null && discount > coupon.getDiscountCap()) discount = coupon.getDiscountCap();
        } else {
            discount = coupon.getDiscountValue();
        }
        return Math.min(discount, purchaseAmount);
    }

    public Optional<Coupon> getAutoApplyCoupon(String campaignName) {
        Optional<Campaign> campaign = campaignRepository.findByCampaignName(campaignName);
        if (campaign.isPresent()) {
            return couponRepository.findByDeletedFalse().stream()
                    .filter(c -> c.getCampaign() != null && c.getCampaign().getCampaignId().equals(campaign.get().getCampaignId()))
                    .filter(c -> c.isAutoApply() && CouponStatus.ACTIVE == c.getStatus())
                    .findFirst();
        }
        return Optional.empty();
    }
}
