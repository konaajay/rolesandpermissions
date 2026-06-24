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
                module.setPaymentMethod(request.getPaymentMethod());
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
    public void saveBulkModules(Long tenantId, BulkModuleSaveRequest request) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();

            double totalInvoiceAmount = 0.0;

            // 1. Process and save all modules
            for (BulkModuleItemRequest item : request.getModules()) {
                TenantModule module = tenantModuleRepository.findByTenantIdAndModuleName(tenantId, item.getModuleName())
                        .orElseGet(() -> TenantModule.builder()
                                .tenantId(tenantId)
                                .moduleName(item.getModuleName())
                                .build());
                module.setActive(true);
                module.setAmount(item.getAmount());
                // In bulk mode, payment method belongs to the invoice, not individual module
                module.setPaymentMethod(request.getPaymentType());
                module.setSpecialRequirements(item.getSpecialRequirements());
                module.setExtraCharges(item.getExtraCharges());
                module.setStartDate(item.getStartDate());
                module.setExpiryDate(item.getExpiryDate());
                tenantModuleRepository.save(module);

                double amount = item.getAmount() != null ? item.getAmount() : 0.0;
                double extra = item.getExtraCharges() != null ? item.getExtraCharges() : 0.0;
                totalInvoiceAmount += (amount + extra);
            }

            // 2. Generate a single invoice
            double subtotal = totalInvoiceAmount;
            double gstAmount = subtotal * 0.18; // 18% GST
            double grandTotal = subtotal + gstAmount;

            TenantInvoice invoice = TenantInvoice.builder()
                    .invoiceNumber("INV-" + tenantId + "-" + System.currentTimeMillis())
                    .tenantId(tenantId)
                    .invoiceType(request.getInvoiceType() != null ? request.getInvoiceType() : "NEW_SUBSCRIPTION")
                    .subtotal(subtotal)
                    .gstAmount(gstAmount)
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
                TenantInvoiceItem invoiceItem = TenantInvoiceItem.builder()
                        .invoiceId(invoice.getId())
                        .moduleName(item.getModuleName())
                        .amount(item.getAmount() != null ? item.getAmount() : 0.0)
                        .extraCharges(item.getExtraCharges() != null ? item.getExtraCharges() : 0.0)
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

        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
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
