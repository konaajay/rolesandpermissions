package com.project.www.marketing.repository;

import com.project.www.marketing.repository.CouponUsageRepository;

import com.project.www.marketing.entity.CouponUsage;

import com.project.www.enums.*;

import com.project.www.marketing.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    Optional<CouponUsage> findByCouponIdAndLearnerId(Long couponId, Long learnerId);
}
