package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
                SELECT COALESCE(SUM(o.totalAmount), 0)
                FROM Order o
                WHERE o.campaign.campaignId = :campaignId
                AND o.status = 'COMPLETED'
            """)
    BigDecimal sumRevenueByCampaign(@Param("campaignId") Long campaignId);

    @Query("""
                SELECT COALESCE(SUM(o.totalAmount), 0)
                FROM Order o
                WHERE o.campaign.campaignId = :campaignId
                AND o.status = 'REFUNDED'
            """)
    BigDecimal sumRefundedAmountByCampaign(@Param("campaignId") Long campaignId);

    long countByCampaign_CampaignId(Long campaignId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.campaign.campaignId = :campaignId AND o.status = 'COMPLETED'")
    long countCompletedByCampaignId(@Param("campaignId") Long campaignId);
}
