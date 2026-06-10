package com.project.www.marketing.controller;

import com.project.www.marketing.controller.MessengerController;

import com.project.www.enums.*;

import com.project.www.marketing.entity.PushNotification;
import com.project.www.entity.PushSubscription;
import com.project.www.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketing/messenger")
@RequiredArgsConstructor
public class MessengerController {

    private final PushNotificationService pushService;

    // ✅ View notifications
    @GetMapping("/admin/notifications")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_NOTIFICATION_VIEW')")
    public ResponseEntity<List<PushNotification>> getNotifications() {
        return ResponseEntity.ok(pushService.getAllNotifications());
    }

    // ✅ Send notification
    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_NOTIFICATION_SEND')")
    public ResponseEntity<PushNotification> sendNotification(
            @RequestBody PushNotification notification) {

        if (notification == null || notification.getTitle() == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(pushService.sendPushNotification(notification));
    }

    // ✅ Public subscribe
    @PostMapping("/public/subscribe")
    public ResponseEntity<PushSubscription> subscribe(
            @RequestBody PushSubscription subscription) {

        if (subscription == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(pushService.subscribe(subscription));
    }
}
