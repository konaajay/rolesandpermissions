package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    List<Coupon> findByDeletedFalse();
    
    Optional<Coupon> findByCodeAndDeletedFalse(String code);

    /**
     * Fixed Increment logic: 
     * Handles NULL maxUsage (unlimited usage) correctly using (c.maxUsage IS NULL OR c.usedCount < c.maxUsage).
     * Clears/Flushes automatically to ensure and avoid stale entities in the Persistence Context.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Coupon c 
        SET c.usedCount = c.usedCount + 1 
        WHERE c.id = :id 
        AND c.deleted = false 
        AND (c.maxUsage IS NULL OR c.usedCount < c.maxUsage)
    """)
    int incrementUsage(@Param("id") Long id);
}
