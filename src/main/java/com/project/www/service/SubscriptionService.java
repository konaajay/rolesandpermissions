package com.project.www.service;

import com.project.www.dto.SubscriptionRequest;
import com.project.www.dto.SubscriptionResponse;
import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse upgradeSubscription(SubscriptionRequest request);
    List<SubscriptionResponse> getSubscriptionHistory();
    List<SubscriptionResponse> getAllSubscriptions(); // Platform Admin Only
}
