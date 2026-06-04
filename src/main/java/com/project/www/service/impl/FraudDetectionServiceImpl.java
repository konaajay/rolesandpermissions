package com.project.www.service.impl;

import com.project.www.enums.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import com.project.www.service.FraudDetectionService;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final Map<String, IpClickStats> ipTracker = new ConcurrentHashMap<>();

    private static final int CLICK_THRESHOLD = 50;
    private static final long WINDOW_MS = 60_000;

    @Override
    public boolean isSuspicious(String affiliateCode, String ipAddress) {
        if (ipAddress == null) return false;

        // Prevention of memory leak
        if (ipTracker.size() > 100_000) {
            ipTracker.clear();
        }

        String key = affiliateCode + ":" + ipAddress;
        IpClickStats stats = ipTracker.computeIfAbsent(key, k -> new IpClickStats());

        long now = System.currentTimeMillis();
        long lastResetTime = stats.lastReset.get();

        if (now - lastResetTime > WINDOW_MS) {
            if (stats.lastReset.compareAndSet(lastResetTime, now)) {
                stats.count.set(0);
            }
        }

        int currentCount = stats.count.incrementAndGet();
        return currentCount > CLICK_THRESHOLD;
    }

    private static class IpClickStats {
        AtomicInteger count = new AtomicInteger(0);
        AtomicLong lastReset = new AtomicLong(System.currentTimeMillis());
    }
}
