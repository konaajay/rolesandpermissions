package com.project.www.tenant.service.impl;

import com.project.www.tenant.dto.BulkModuleItemRequest;
import com.project.www.tenant.dto.BulkModuleSaveRequest;
import com.project.www.tenant.dto.TenantModuleUpdateRequest;
import com.project.www.tenant.entity.TenantInvoice;
import com.project.www.tenant.entity.TenantInvoiceInstallment;
import com.project.www.tenant.entity.TenantInvoiceItem;
import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.repository.TenantInvoiceInstallmentRepository;
import com.project.www.tenant.repository.TenantInvoiceItemRepository;
import com.project.www.tenant.repository.TenantInvoiceRepository;
import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.tenant.service.TenantModuleService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantModuleServiceImpl implements TenantModuleService {

    private final TenantModuleRepository tenantModuleRepository;
    private final TenantInvoiceRepository tenantInvoiceRepository;
    private final TenantInvoiceItemRepository tenantInvoiceItemRepository;
    private final TenantInvoiceInstallmentRepository tenantInvoiceInstallmentRepository;
    private final com.project.www.tenant.repository.SubscriptionRepository subscriptionRepository;
    private final com.project.www.tenant.repository.TenantRepository tenantRepository;

    @Override
    public List<TenantModule> getModulesForTenant(Long tenantId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return tenantModuleRepository.findByTenantId(tenantId);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public List<TenantModule> getActiveModulesForTenantRequiresNew(Long tenantId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        Long originalId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            return tenantModuleRepository.findByTenantIdAndActiveTrue(tenantId);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
            TenantContext.setCurrentTenant(originalId);
        }
    }
    @Override
    public void enableModule(Long tenantId, String moduleName, TenantModuleUpdateRequest request) {
        // Kept for backward compatibility but doesn't generate invoice anymore.
        // It's better to use bulk save for billing.
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            TenantModule module = tenantModuleRepository.findByTenantIdAndModuleName(tenantId, moduleName)
                    .orElseGet(() -> TenantModule.builder()
                            .tenantId(tenantId)
                            .moduleName(moduleName)
                            .build());
            module.setActive(true);
            if (request != null) {
                module.setAmount(request.getAmount());
                module.setSpecialRequirements(request.getSpecialRequirements());
                module.setExtraCharges(request.getExtraCharges());
                module.setStartDate(request.getStartDate());
                module.setExpiryDate(request.getExpiryDate());
            }
            tenantModuleRepository.save(module);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    public void disableModule(Long tenantId, String moduleName) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            tenantModuleRepository.findByTenantIdAndModuleName(tenantId, moduleName).ifPresent(module -> {
                module.setActive(false);
                tenantModuleRepository.save(module);
            });
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void saveBulkModules(Long tenantId, BulkModuleSaveRequest request) {
        double totalInvoiceAmount = 0.0;

            java.util.Set<String> requestedModules = request.getModules().stream()
                    .map(BulkModuleItemRequest::getModuleName)
                    .collect(java.util.stream.Collectors.toSet());

            // 1. Process and save all modules
            for (BulkModuleItemRequest item : request.getModules()) {
                TenantModule module = tenantModuleRepository.findByTenantIdAndModuleName(tenantId, item.getModuleName())
                        .orElseGet(() -> TenantModule.builder()
                                .tenantId(tenantId)
                                .moduleName(item.getModuleName())
                                .build());
                module.setActive(true);
                module.setAmount(item.getAmount());
                module.setSpecialRequirements(item.getSpecialRequirements());
                module.setExtraCharges(item.getExtraCharges());
                module.setStartDate(item.getStartDate());
                module.setExpiryDate(item.getExpiryDate());
                tenantModuleRepository.save(module);

                double amount = item.getAmount() != null ? item.getAmount() : 0.0;
                double extra = item.getExtraCharges() != null ? item.getExtraCharges() : 0.0;
                totalInvoiceAmount += (amount + extra);
            }

            // Deactivate other non-core modules that are NOT part of this new subscription
            java.util.List<TenantModule> existingModules = tenantModuleRepository.findByTenantId(tenantId);
            for (TenantModule existing : existingModules) {
                if ("ADMIN".equals(existing.getModuleName()) || "SETTINGS".equals(existing.getModuleName()) || "EMPLOYEE".equals(existing.getModuleName())) {
                    if (!existing.getActive()) {
                        existing.setActive(true);
                        tenantModuleRepository.save(existing);
                    }
                } else if (!requestedModules.contains(existing.getModuleName())) {
                    existing.setActive(false);
                    tenantModuleRepository.save(existing);
                }
            }

            // 3. Save Subscription entity
            com.project.www.tenant.entity.Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            
            // Re-calculate grand total for subscription based on logic below
            double subtotalForSub = totalInvoiceAmount;
            double gstPercentageForSub = request.getGstPercentage() != null ? request.getGstPercentage() : 18.0;
            double initialGrandTotal = subtotalForSub + (subtotalForSub * (gstPercentageForSub / 100.0));

            com.project.www.tenant.entity.Subscription subscription = com.project.www.tenant.entity.Subscription.builder()
                    .tenant(tenant)
                    .planName("CUSTOM_BULK")
                    .billingInterval(request.getPaymentType())
                    .amount(totalInvoiceAmount)
                    .status("ACTIVE")
                    .startDate(java.time.LocalDate.now())
                    .endDate(java.time.LocalDate.now().plusYears(1))
                    .amountPaid(0.0)
                    .amountPending(initialGrandTotal)
                    .build();
            subscriptionRepository.save(subscription);

            // 4. Generate Invoice (if applicable)
            if (totalInvoiceAmount > 0) {          
            double subtotal = totalInvoiceAmount;
            
            double discount = 0.0;
            if (request.getDiscountValue() != null && request.getDiscountValue() > 0) {
                if ("PERCENTAGE".equalsIgnoreCase(request.getDiscountType())) {
                    discount = subtotal * (request.getDiscountValue() / 100.0);
                } else {
                    discount = request.getDiscountValue();
                }
            }
            
            double amountAfterDiscount = subtotal - discount;
            double gstPercentage = request.getGstPercentage() != null ? request.getGstPercentage() : 18.0;
            double gstAmount = amountAfterDiscount * (gstPercentage / 100.0); 
            
            double cgst = gstAmount / 2.0; 
            double sgst = gstAmount / 2.0; 
            double igst = 0.0;
            double grandTotal = amountAfterDiscount + gstAmount;

            TenantInvoice invoice = TenantInvoice.builder()
                    .invoiceNumber("INV-" + tenantId + "-" + System.currentTimeMillis())
                    .tenantId(tenantId)
                    .invoiceType(request.getInvoiceType() != null ? request.getInvoiceType() : "NEW_SUBSCRIPTION")
                    .subtotal(subtotal)
                    .gstAmount(gstAmount)
                    .cgst(cgst)
                    .sgst(sgst)
                    .igst(igst)
                    .discount(discount)
                    .totalAmount(grandTotal)
                    .paidAmount(0.0)
                    .pendingAmount(grandTotal)
                    .paymentType(request.getPaymentType())
                    .invoiceDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(7)) // default 7 days due
                    .status("Pending")
                    .build();

            invoice = tenantInvoiceRepository.save(invoice);

            // 3. Save invoice items
            for (BulkModuleItemRequest item : request.getModules()) {
                double itemAmt = item.getAmount() != null ? item.getAmount() : 0.0;
                double extra = item.getExtraCharges() != null ? item.getExtraCharges() : 0.0;
                TenantInvoiceItem invoiceItem = TenantInvoiceItem.builder()
                        .invoiceId(invoice.getId())
                        .moduleName(item.getModuleName())
                        .amount(itemAmt)
                        .quantity(1)
                        .unitPrice(itemAmt)
                        .taxRate(18.0)
                        .total(itemAmt + extra)
                        .extraCharges(extra)
                        .startDate(item.getStartDate())
                        .expiryDate(item.getExpiryDate())
                        .build();
                tenantInvoiceItemRepository.save(invoiceItem);
            }

            // 4. Handle Installments
            if ("INSTALLMENT".equalsIgnoreCase(request.getPaymentType()) && request.getNoOfInstallments() != null && request.getNoOfInstallments() > 0) {
                double installmentAmt = request.getInstallmentAmount() != null ? request.getInstallmentAmount() : (grandTotal / request.getNoOfInstallments());
                for (int i = 1; i <= request.getNoOfInstallments(); i++) {
                    TenantInvoiceInstallment installment = TenantInvoiceInstallment.builder()
                            .invoiceId(invoice.getId())
                            .installmentNo(i)
                            .amount(installmentAmt)
                            .dueDate(LocalDate.now().plusMonths(i - 1)) // e.g. first due now, next due next month
                            .paid(false)
                            .build();
                    tenantInvoiceInstallmentRepository.save(installment);
                }
            } else if ("FULL".equalsIgnoreCase(request.getPaymentType())) {
                // If paid immediately, we can update status to Paid or create one fully paid installment.
                // Assuming it's pending until payment is actually received.
                TenantInvoiceInstallment installment = TenantInvoiceInstallment.builder()
                        .invoiceId(invoice.getId())
                        .installmentNo(1)
                        .amount(totalInvoiceAmount)
                        .dueDate(LocalDate.now())
                        .paid(false)
                        .build();
                tenantInvoiceInstallmentRepository.save(installment);
            }

            }

    }

    @Override
    public List<TenantInvoice> getInvoicesForTenant(Long tenantId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return tenantInvoiceRepository.findByTenantId(tenantId);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }
    @Override
    public List<TenantInvoiceInstallment> getInstallmentsForInvoice(Long invoiceId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return tenantInvoiceInstallmentRepository.findByInvoiceId(invoiceId);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    public List<TenantInvoiceItem> getItemsForInvoice(Long invoiceId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return tenantInvoiceItemRepository.findByInvoiceId(invoiceId);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void payInstallment(Long invoiceId, Long installmentId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            TenantInvoiceInstallment installment = tenantInvoiceInstallmentRepository.findById(installmentId)
                    .orElseThrow(() -> new RuntimeException("Installment not found"));
            
            if (installment.getPaid() != null && installment.getPaid()) {
                throw new RuntimeException("Installment is already paid");
            }
            
            installment.setPaid(true);
            tenantInvoiceInstallmentRepository.save(installment);

            // Update invoice pending amount and status
            TenantInvoice invoice = tenantInvoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
            
            double paid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0.0;
            paid += installment.getAmount();
            invoice.setPaidAmount(paid);
            
            double pending = invoice.getTotalAmount() - paid;
            if (pending < 0) pending = 0;
            invoice.setPendingAmount(pending);
            
            if (pending == 0) {
                invoice.setStatus("Paid");
            } else {
                invoice.setStatus("Partially Paid");
            }
            tenantInvoiceRepository.save(invoice);
            
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }
}
