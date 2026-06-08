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
        private final BigDecimal orderAmount;

        public PurchaseCompletedEvent(Long userId, Long orderId, BigDecimal orderAmount) {
            this.userId = userId;
            this.orderId = orderId;
            this.orderAmount = orderAmount;
        }

        public Long getUserId() { return userId; }
        public Long getOrderId() { return orderId; }
        public BigDecimal getOrderAmount() { return orderAmount; }
    }

}
