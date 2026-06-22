package com.project.www.constants;

import java.util.HashMap;
import java.util.Map;

public class ModulePricing {
    
    public static final Map<String, Double> PRICES = new HashMap<>();

    static {
        PRICES.put(Modules.CRM, 0.0);
        PRICES.put(Modules.HRMS, 0.0);
        PRICES.put(Modules.VENDOR, 0.0);
        PRICES.put(Modules.MARKETING, 0.0);
        PRICES.put(Modules.LEADS, 0.0);
        PRICES.put(Modules.EMPLOYEE, 0.0);
        PRICES.put(Modules.AFFILIATE, 0.0);
        PRICES.put(Modules.PAYROLL, 0.0);
        PRICES.put(Modules.ATTENDANCE, 0.0);
        PRICES.put(Modules.PERFORMANCE, 0.0);
        PRICES.put(Modules.SETTINGS, 0.0);
        PRICES.put(Modules.LEAVE, 0.0);
        PRICES.put(Modules.REPORTS, 0.0);
        PRICES.put(Modules.SUPPORT_TICKETS, 0.0);
        PRICES.put(Modules.TASKS, 0.0);
        PRICES.put(Modules.REVENUE, 0.0);
        PRICES.put(Modules.ADMIN, 0.0); // Free base module
    }

    public static Double getPrice(String moduleName) {
        return PRICES.getOrDefault(moduleName, 0.0);
    }
    
    public static Map<String, Double> getAllPricing() {
        return PRICES;
    }
}
