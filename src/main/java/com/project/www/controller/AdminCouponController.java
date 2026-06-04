package com.project.www.controller;

import com.project.www.enums.*;

import com.project.www.entity.Coupon;
import com.project.www.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketing/admin/coupons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping
    
    public ResponseEntity<Coupon> createCoupon(@RequestBody com.project.www.dto.CouponRequest request) {
        String creator = "ADMIN_USER";
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            creator = auth.getName();
        }
        return ResponseEntity.ok(couponService.createCoupon(request, creator));
    }

    @PatchMapping("/{id}/status")
    
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        couponService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        couponService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        couponService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
