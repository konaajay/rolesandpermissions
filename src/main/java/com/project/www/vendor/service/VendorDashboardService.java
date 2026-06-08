package com.project.www.vendor.service;

import com.project.www.vendor.dto.VendorDashboardDto;

public interface VendorDashboardService {
    VendorDashboardDto getDashboardData(String filter);
}
