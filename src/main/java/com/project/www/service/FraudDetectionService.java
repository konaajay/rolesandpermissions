package com.project.www.service;

import com.project.www.enums.*;

public interface FraudDetectionService {
    boolean isSuspicious(String affiliateCode, String ipAddress);
}
