package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.LeadStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeadStatusHistoryRepository extends JpaRepository<LeadStatusHistory, Long> {
    List<LeadStatusHistory> findByLeadIdOrderByTimestampDesc(Long leadId);
}
