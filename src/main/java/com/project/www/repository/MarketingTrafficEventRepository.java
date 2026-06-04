package com.project.www.repository;

import com.project.www.enums.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.project.www.entity.TrafficEvent;

@Repository
public interface MarketingTrafficEventRepository extends JpaRepository<TrafficEvent, Long> {
    
    @Query("SELECT COUNT(DISTINCT e.sessionId) FROM TrafficEvent e WHERE e.trackedLinkId = :tid AND e.eventType = :type")
    long countUniqueClicks(@Param("tid") String tid, @Param("type") String type);
    
    @Query("SELECT COUNT(e) FROM TrafficEvent e WHERE e.trackedLinkId = :tid AND e.eventType = 'LEAD'")
    long countLeadsByTid(@Param("tid") String tid);
    
    long countByTrackedLinkIdAndEventType(String trackedLinkId, String eventType);

    @Query("SELECT COUNT(e) FROM TrafficEvent e WHERE e.utmSource = :s AND e.utmCampaign = :c AND e.eventType = :t")
    long countByUtmSourceAndUtmCampaignAndEventType(@Param("s") String s, @Param("c") String c, @Param("t") String t);

    @Query("SELECT COUNT(e) FROM TrafficEvent e WHERE e.eventType = :type")
    long countByEventType(@Param("type") String type);

    @Query("SELECT e.source, COUNT(DISTINCT e.sessionId) FROM TrafficEvent e GROUP BY e.source")
    List<Object[]> countUniqueBySource();

    @Query("SELECT e.eventType, COUNT(DISTINCT e.sessionId) FROM TrafficEvent e GROUP BY e.eventType")
    List<Object[]> countUniqueByEventType();

    @Query("SELECT e.utmCampaign, COUNT(DISTINCT e.sessionId) FROM TrafficEvent e GROUP BY e.utmCampaign")
    List<Object[]> countUniqueByUtmCampaign();

    @Query("SELECT e.utmMedium, COUNT(DISTINCT e.sessionId) FROM TrafficEvent e GROUP BY e.utmMedium")
    List<Object[]> countUniqueByUtmMedium();

    @Query("SELECT e.utmCampaign, e.eventType, COUNT(DISTINCT e.sessionId) FROM TrafficEvent e GROUP BY e.utmCampaign, e.eventType")
    List<Object[]> getCampaignFunnelStats();
}
