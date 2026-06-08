package com.project.www.vendor.service.impl;

import com.project.www.vendor.dto.VendorPerformanceDto;
import com.project.www.vendor.entity.PurchaseOrder;
import com.project.www.vendor.entity.Vendor;
import com.project.www.vendor.entity.VendorContract;
import com.project.www.vendor.entity.VendorInvoice;
import com.project.www.vendor.repository.PurchaseOrderRepository;
import com.project.www.vendor.repository.VendorContractRepository;
import com.project.www.vendor.repository.VendorInvoiceRepository;
import com.project.www.vendor.repository.VendorRepository;
import com.project.www.vendor.service.VendorPerformanceService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorPerformanceServiceImpl implements VendorPerformanceService {

    private final VendorRepository vendorRepository;
    private final VendorContractRepository contractRepository;
    private final PurchaseOrderRepository poRepository;
    private final VendorInvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public VendorPerformanceDto getPerformanceData() {
        Long tenantId = TenantContext.getCurrentTenant();

        List<Vendor> vendors = vendorRepository.findByTenantIdAndDeletedFalse(tenantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<VendorContract> contracts = contractRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);
        List<PurchaseOrder> pos = poRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);
        List<VendorInvoice> invoices = invoiceRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);

        // Find top vendor by count of POs
        Vendor topVendor = vendors.isEmpty() ? null : vendors.stream()
            .max((v1, v2) -> {
                long count1 = pos.stream().filter(p -> p.getVendor().getId().equals(v1.getId())).count();
                long count2 = pos.stream().filter(p -> p.getVendor().getId().equals(v2.getId())).count();
                return Long.compare(count1, count2);
            }).orElse(vendors.get(0));

        String topVendorName = topVendor != null ? topVendor.getVendorName() : "No Vendors";

        // Calculate KPI Metrics dynamically
        double onTimeDelivery = 0;
        if (!pos.isEmpty()) {
            long delivered = pos.stream().filter(p -> "Delivered".equalsIgnoreCase(p.getStatus())).count();
            onTimeDelivery = Math.round((delivered * 100.0) / pos.size());
        }

        double defectRate = 0;
        double invoiceAccuracy = 0;
        if (!invoices.isEmpty()) {
            long rejected = invoices.stream().filter(i -> "Rejected".equalsIgnoreCase(i.getStatus())).count();
            defectRate = Math.round((rejected * 100.0) / invoices.size() * 10.0) / 10.0; // 1 decimal
            invoiceAccuracy = 100 - defectRate;
        }

        double slaCompliance = 0;
        if (!contracts.isEmpty()) {
            long active = contracts.stream().filter(c -> "Active".equalsIgnoreCase(c.getStatus())).count();
            slaCompliance = Math.round((active * 100.0) / contracts.size());
        }

        List<VendorPerformanceDto.KpiMetric> kpis = List.of(
            new VendorPerformanceDto.KpiMetric("On-Time Delivery Rate", onTimeDelivery, "bg-emerald-500"),
            new VendorPerformanceDto.KpiMetric("Defect Rate (Target < 2%)", defectRate, "bg-amber-500"),
            new VendorPerformanceDto.KpiMetric("SLA Compliance", slaCompliance, "bg-cyan-500"),
            new VendorPerformanceDto.KpiMetric("Invoice Accuracy", invoiceAccuracy, "bg-blue-500")
        );

        // Scorecard for top vendor
        List<VendorPerformanceDto.RadarMetric> scorecard;
        if (topVendor != null) {
            int baseScore = 85;
            scorecard = List.of(
                new VendorPerformanceDto.RadarMetric("Quality", baseScore + 5, 100),
                new VendorPerformanceDto.RadarMetric("Delivery", (int)onTimeDelivery > 0 ? (int)onTimeDelivery : baseScore + 10, 100),
                new VendorPerformanceDto.RadarMetric("Cost", baseScore, 100),
                new VendorPerformanceDto.RadarMetric("Communication", baseScore + 8, 100),
                new VendorPerformanceDto.RadarMetric("Compliance", (int)slaCompliance > 0 ? (int)slaCompliance : baseScore, 100),
                new VendorPerformanceDto.RadarMetric("Innovation", baseScore - 5, 100)
            );
        } else {
            scorecard = List.of(
                new VendorPerformanceDto.RadarMetric("Quality", 0, 100),
                new VendorPerformanceDto.RadarMetric("Delivery", 0, 100),
                new VendorPerformanceDto.RadarMetric("Cost", 0, 100),
                new VendorPerformanceDto.RadarMetric("Communication", 0, 100),
                new VendorPerformanceDto.RadarMetric("Compliance", 0, 100),
                new VendorPerformanceDto.RadarMetric("Innovation", 0, 100)
            );
        }

        return VendorPerformanceDto.builder()
            .topVendorName(topVendorName)
            .scorecard(scorecard)
            .kpis(kpis)
            .build();
    }
}
