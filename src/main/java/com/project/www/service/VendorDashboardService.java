package com.project.www.service;

import com.project.www.dto.VendorDashboardDto;

public interface VendorDashboardService {
    VendorDashboardDto getDashboardData(String filter);
}
