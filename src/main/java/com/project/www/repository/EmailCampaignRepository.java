package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.EmailCampaign;
import com.project.www.enums.EmailCampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {

    /**
     * Atomic Increment for Success Counts.
     * Prevents race conditions during high-volume sending. 🛡️🏁✍️
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailCampaign c SET c.successCount = c.successCount + 1 WHERE c.id = :id")
    int incrementSuccessCount(@Param("id") Long id);

    /**
     * Atomic Increment for Failed Counts.
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailCampaign c SET c.failedCount = c.failedCount + 1 WHERE c.id = :id")
    int incrementFailedCount(@Param("id") Long id);

    /**
     * Atomic Increment for Total Recipients.
     * Used during bulk imports and CSV processing. 🛡️🏁
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailCampaign c SET c.totalRecipients = c.totalRecipients + :count WHERE c.id = :id")
    int incrementTotalRecipients(@Param("id") Long id, @Param("count") int count);

    /**
     * Atomic Status Update with Timestamp.
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailCampaign c SET c.status = :status, c.lastExecutedAt = :time WHERE c.id = :id")
    int updateCampaignExecutionStatus(@Param("id") Long id, @Param("status") EmailCampaignStatus status, @Param("time") LocalDateTime time);
}
