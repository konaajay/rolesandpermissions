package com.project.www.constants;

import java.util.HashMap;
import java.util.Map;

public class ModulePricing {
    
    public static final Map<String, Double> PRICES = new HashMap<>();

    static {
        PRICES.put(Modules.CRM, 500.0);
        PRICES.put(Modules.HRMS, 1000.0);
        PRICES.put(Modules.LMS, 1500.0);
        PRICES.put(Modules.VENDOR, 700.0);
        PRICES.put(Modules.MARKETING, 800.0);
        PRICES.put(Modules.LEADS, 400.0);
        PRICES.put(Modules.EMPLOYEE, 300.0);
        PRICES.put(Modules.COURSE, 600.0);
        PRICES.put(Modules.AFFILIATE, 500.0);
        PRICES.put(Modules.PAYROLL, 1200.0);
        PRICES.put(Modules.ATTENDANCE, 400.0);
        PRICES.put(Modules.PERFORMANCE, 500.0);
        PRICES.put(Modules.SETTINGS, 100.0);
        PRICES.put(Modules.LEAVE, 200.0);
        PRICES.put(Modules.REPORTS, 300.0);
        PRICES.put(Modules.SUPPORT_TICKETS, 400.0);
        PRICES.put(Modules.TASKS, 250.0);
        PRICES.put(Modules.REVENUE, 450.0);
        PRICES.put(Modules.ADMIN, 0.0); // Free base module
    }

    public static Double getPrice(String moduleName) {
        return PRICES.getOrDefault(moduleName, 0.0);
    }
    
    public static Map<String, Double> getAllPricing() {
        return PRICES;
    }
}
