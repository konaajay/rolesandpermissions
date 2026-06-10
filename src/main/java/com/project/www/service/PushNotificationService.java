package com.project.www.service;

import com.project.www.tenant.entity.Subscription;

import com.project.www.config.MarketingProperties;
import com.project.www.marketing.entity.PushNotification;
import com.project.www.entity.PushSubscription;
import com.project.www.repository.PushNotificationRepository;
import com.project.www.repository.PushSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@lombok.extern.slf4j.Slf4j
public class PushNotificationService {
    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushNotificationRepository pushRepository;
    private final PushSubscriptionRepository subscriptionRepository;
    private final RestTemplate restTemplate;
    private final MarketingProperties marketingProperties;

    public PushNotificationService(PushNotificationRepository pushRepository,
            PushSubscriptionRepository subscriptionRepository,
            RestTemplate restTemplate,
            MarketingProperties marketingProperties) {
        this.pushRepository = pushRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.restTemplate = restTemplate;
        this.marketingProperties = marketingProperties;
    }

    public List<PushNotification> getAllNotifications() {
        return pushRepository.findAllByOrderByCreatedAtDesc();
    }

    @SuppressWarnings("null")
    @Transactional
    public @org.springframework.lang.NonNull PushNotification sendPushNotification(
            @org.springframework.lang.NonNull PushNotification notification) {
        String channel = notification.getTargetChannel();
        List<PushSubscription> targets;

        if ("WEB_PUSH".equals(channel)) {
            targets = subscriptionRepository.findByPlatform("BROWSER");
        } else if ("MOBILE_PUSH".equals(channel)) {
            targets = subscriptionRepository.findAll().stream()
                    .filter(s -> "IOS".equals(s.getPlatform()) || "ANDROID".equals(s.getPlatform())
                            || "MOBILE".equals(s.getPlatform()))
                    .toList();
        } else {
            targets = List.of();
        }

        log.info("Attempting to send {} push notification to {} devices: {}",
                channel, targets.size(), notification.getTitle());

        if (!targets.isEmpty() && marketingProperties.getFirebase() != null
                && marketingProperties.getFirebase().getServerKey() != null) {
            String fcmUrl = "https://fcm.googleapis.com/fcm/send";
            for (PushSubscription sub : targets) {
                try {
                    Map<String, Object> body = new HashMap<>();
                    body.put("to", sub.getDeviceToken());
                    body.put("notification", Map.of(
                            "title", notification.getTitle(),
                            "body", notification.getBody()));

                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.set("Authorization", "key=" + marketingProperties.getFirebase().getServerKey());
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

                    org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                            body, headers);
                    restTemplate.postForEntity(fcmUrl, entity, String.class);
                } catch (Exception e) {
                    log.error("Failed to send push to device {}: {}", sub.getDeviceToken(), e.getMessage());
                }
            }
        }

        notification.setStatus("SENT");
        notification.setRecipientsCount(targets.size());
        notification.setSentAt(LocalDateTime.now());

        return pushRepository.save(notification);
    }

    public PushSubscription subscribe(PushSubscription subscription) {
        if (subscription == null)
            throw new IllegalArgumentException("Subscription cannot be null");
        return subscriptionRepository.save(subscription);
    }
}
