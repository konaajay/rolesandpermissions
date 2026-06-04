package com.project.www.event;

import com.project.www.enums.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {
    private String orderId;
    private String referralCode;
    private String sessionId;
    private Long studentId;
    private Long courseId;
    private java.math.BigDecimal amount;
}
