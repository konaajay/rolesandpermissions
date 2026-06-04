package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByLearnerId(Long learnerId);

    List<PushSubscription> findByPlatform(String platform);
}
