package com.project.www.marketing.controller;

import com.project.www.marketing.controller.AdminCouponController;

import com.project.www.marketing.entity.Coupon;

import com.project.www.marketing.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketing/admin/coupons")
@RequiredArgsConstructor

@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_VIEW')")
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping

    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_CREATE')")
    public ResponseEntity<Coupon> createCoupon(@RequestBody com.project.www.marketing.dto.CouponRequest request) {
        String creator = "ADMIN_USER";
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            creator = auth.getName();
        }
        return ResponseEntity.ok(couponService.createCoupon(request, creator));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_UPDATE')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        couponService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_DELETE')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        couponService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_DELETE')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        couponService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
