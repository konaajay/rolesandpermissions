package com.project.www.event;

import com.project.www.enums.*;

import java.math.BigDecimal;

public class MarketingEvents {

    public static class UserRegisteredEvent {
        private final Long userId;
        private final String email;

        public UserRegisteredEvent(Long userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
    }

    public static class PurchaseCompletedEvent {
        private final Long userId;
        private final Long orderId;
        private final Long courseId;
        private final BigDecimal orderAmount;

        public PurchaseCompletedEvent(Long userId, Long orderId, Long courseId, BigDecimal orderAmount) {
            this.userId = userId;
            this.orderId = orderId;
            this.courseId = courseId;
            this.orderAmount = orderAmount;
        }

        public Long getUserId() { return userId; }
        public Long getOrderId() { return orderId; }
        public Long getCourseId() { return courseId; }
        public BigDecimal getOrderAmount() { return orderAmount; }
    }

    public static class ReferralCompletedEvent {
        private final Long referrerId;
        private final Long refereeId;
        private final String referralCode;

        public ReferralCompletedEvent(Long referrerId, Long refereeId, String referralCode) {
            this.referrerId = referrerId;
            this.refereeId = refereeId;
            this.referralCode = referralCode;
        }

        public Long getReferrerId() { return referrerId; }
        public Long getRefereeId() { return refereeId; }
        public String getReferralCode() { return referralCode; }
    }
}
