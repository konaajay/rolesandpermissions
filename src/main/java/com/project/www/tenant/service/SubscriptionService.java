package com.project.www.tenant.service;

import com.project.www.tenant.dto.SubscriptionRequest;
import com.project.www.tenant.dto.SubscriptionResponse;
import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse upgradeSubscription(SubscriptionRequest request);
    List<SubscriptionResponse> getSubscriptionHistory();
    List<SubscriptionResponse> getAllSubscriptions(); // Platform Admin Only
}
