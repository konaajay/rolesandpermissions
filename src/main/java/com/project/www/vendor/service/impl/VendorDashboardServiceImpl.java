package com.project.www.vendor.service.impl;

import com.project.www.vendor.dto.VendorDashboardDto;
import com.project.www.vendor.entity.PurchaseOrder;
import com.project.www.vendor.entity.Vendor;
import com.project.www.vendor.entity.VendorContract;
import com.project.www.vendor.entity.VendorInvoice;
import com.project.www.vendor.repository.PurchaseOrderRepository;
import com.project.www.vendor.repository.VendorContractRepository;
import com.project.www.vendor.repository.VendorInvoiceRepository;
import com.project.www.vendor.repository.VendorRepository;
import com.project.www.vendor.service.VendorDashboardService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorDashboardServiceImpl implements VendorDashboardService {

    private final VendorRepository vendorRepository;
    private final VendorContractRepository contractRepository;
    private final PurchaseOrderRepository poRepository;
    private final VendorInvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public VendorDashboardDto getDashboardData(String filter) {
        Long tenantId = TenantContext.getCurrentTenant();

        List<Vendor> vendors = vendorRepository.findByTenantIdAndDeletedFalse(tenantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<VendorContract> contracts = contractRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);
        List<PurchaseOrder> pos = poRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);
        List<VendorInvoice> invoices = invoiceRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);

        // Stats
        int activeVendors = (int) vendors.stream().filter(v -> "Active".equalsIgnoreCase(v.getStatus())).count();
        int activeContracts = (int) contracts.stream().filter(c -> "Active".equalsIgnoreCase(c.getStatus())).count();
        
        BigDecimal totalSpend = pos.stream()
            .map(PurchaseOrder::getTotalAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        int pendingApprovals = (int) invoices.stream().filter(i -> "Pending".equalsIgnoreCase(i.getStatus())).count();
        pendingApprovals += (int) pos.stream().filter(p -> "Requested".equalsIgnoreCase(p.getStatus())).count();

        VendorDashboardDto.Stats stats = VendorDashboardDto.Stats.builder()
            .activeVendors(activeVendors)
            .activeContracts(activeContracts)
            .procurementSpend("$" + formatCompact(totalSpend))
            .pendingApprovals(pendingApprovals)
            .build();

        // Vendor Data Chart
        Map<String, Long> categoryCount = vendors.stream()
            .filter(v -> v.getCategory() != null)
            .collect(Collectors.groupingBy(v -> v.getCategory().getName(), Collectors.counting()));
            
        List<VendorDashboardDto.VendorChartData> vendorChart = categoryCount.entrySet().stream()
            .map(e -> new VendorDashboardDto.VendorChartData(e.getKey(), e.getValue().intValue()))
            .collect(Collectors.toList());

        // Dynamic spend data based on filter
        List<VendorDashboardDto.SpendChartData> spendChart = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", Locale.US);
        
        int monthsToLoop = 6;
        int monthsOffset = 0;
        
        if ("This Year".equalsIgnoreCase(filter)) {
            monthsToLoop = today.getMonthValue();
            monthsOffset = 0;
        } else if ("Last Year".equalsIgnoreCase(filter)) {
            monthsToLoop = 12;
            monthsOffset = today.getMonthValue();
        } else if (filter.matches("\\d{4}")) {
            int year = Integer.parseInt(filter);
            int currentYear = today.getYear();
            if (year <= currentYear) {
                monthsToLoop = (year == currentYear) ? today.getMonthValue() : 12;
                monthsOffset = (currentYear - year) * 12 + today.getMonthValue() - monthsToLoop;
            }
        }

        for (int i = monthsToLoop - 1; i >= 0; i--) {
            java.time.YearMonth targetMonth = java.time.YearMonth.from(today.minusMonths(i + monthsOffset));
            String monthName = targetMonth.format(monthFormatter);
            
            double monthlySpend = pos.stream()
                .filter(p -> p.getCreatedAt() != null)
                .filter(p -> java.time.YearMonth.from(p.getCreatedAt()).equals(targetMonth))
                .map(PurchaseOrder::getTotalAmount)
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
                
            spendChart.add(new VendorDashboardDto.SpendChartData(monthName, monthlySpend));
        }

        // Alerts (Dynamically from expiring contracts and high risk vendors)
        List<VendorDashboardDto.Alert> alerts = new ArrayList<>();
        contracts.stream().filter(c -> "Active".equalsIgnoreCase(c.getStatus()))
            .limit(2)
            .forEach(c -> alerts.add(new VendorDashboardDto.Alert(c.getVendor().getVendorName(), "Contract expires " + c.getExpires(), "medium")));
            
        vendors.stream().filter(v -> "Under Review".equalsIgnoreCase(v.getStatus()))
            .limit(2)
            .forEach(v -> alerts.add(new VendorDashboardDto.Alert(v.getVendorName(), "Vendor status is under review", "high")));

        // Activities (Recent POs and Invoices)
        List<VendorDashboardDto.Activity> activities = new ArrayList<>();
        pos.stream().limit(2).forEach(p -> activities.add(
            new VendorDashboardDto.Activity("PO " + p.getStatus(), p.getPoNumber() + " for " + p.getVendor().getVendorName(), "Recent")
        ));
        invoices.stream().limit(2).forEach(i -> activities.add(
            new VendorDashboardDto.Activity("Invoice " + i.getStatus(), i.getInvoiceNumber() + " from " + i.getVendor().getVendorName(), "Recent")
        ));

        return VendorDashboardDto.builder()
            .stats(stats)
            .spendData(spendChart)
            .vendorData(vendorChart)
            .alerts(alerts)
            .activities(activities)
            .build();
    }
    
    private String formatCompact(BigDecimal amount) {
        if (amount == null) return "0";
        double val = amount.doubleValue();
        if (val >= 1_000_000) return String.format("%.1fM", val / 1_000_000);
        if (val >= 1_000) return String.format("%.1fk", val / 1_000);
        return NumberFormat.getNumberInstance(Locale.US).format(val);
    }
}
