package com.project.www.marketing.repository;

import com.project.www.marketing.repository.EmailRecipientRepository;

import com.project.www.marketing.entity.EmailRecipient;

import com.project.www.enums.*;

import com.project.www.marketing.entity.EmailRecipient;
import com.project.www.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface EmailRecipientRepository extends JpaRepository<EmailRecipient, Long> {

    boolean existsByCampaign_IdAndEmail(Long campaignId, String email);

    @Query("SELECT r FROM EmailRecipient r JOIN FETCH r.campaign WHERE r.campaign.id = :campaignId AND r.status = :status")
    List<EmailRecipient> findByCampaignIdAndStatusWithFetch(@Param("campaignId") Long campaignId, @Param("status") EmailStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE EmailRecipient r SET r.status = :status, r.sentAt = :time, r.failureReason = :reason WHERE r.id = :id")
    int updateStatusAndTimestamp(@Param("id") Long id, @Param("status") EmailStatus status, @Param("time") LocalDateTime time, @Param("reason") String reason);
}
