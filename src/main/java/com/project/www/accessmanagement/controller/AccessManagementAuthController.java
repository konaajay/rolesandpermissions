package com.project.www.accessmanagement.controller;

import com.project.www.tenant.entity.Tenant;

import com.project.www.tenant.dto.TenantResponse;

import com.project.www.tenant.dto.CreateTenantRequest;

import com.project.www.tenant.service.TenantService;

import com.project.www.accessmanagement.dto.AuthResponse;
import com.project.www.accessmanagement.dto.LoginRequest;
import com.project.www.accessmanagement.dto.RegisterRequest;
import com.project.www.accessmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AccessManagementAuthController {

    private final AuthService authService;
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.project.www.tenant.service.TenantService tenantService;
    
    @org.springframework.beans.factory.annotation.Autowired
    private JavaMailSender mailSender;
    
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @GetMapping("/test-mail")
    public String testMail() {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo("yourpersonalemail@gmail.com"); // Replaced dynamically during testing if needed
            msg.setSubject("Test Mail");
            msg.setText("Mail working perfectly.");
            mailSender.send(msg);
            return "Mail Sent Successfully from " + fromEmail;
        } catch (Exception e) {
            e.printStackTrace();
            return "Mail Failed: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.project.www.accessmanagement.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static java.util.Map<String, String> otpStore = new java.util.concurrent.ConcurrentHashMap<>();

    @PostMapping("/register-company")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> registerCompany(
            @jakarta.validation.Valid @RequestBody com.project.www.tenant.dto.CreateTenantRequest request
    ) {
        com.project.www.tenant.dto.TenantResponse tenant = tenantService.createTenant(request);
        java.util.Map<String, Object> responseMap = new java.util.HashMap<>();
        responseMap.put("message", "Tenant created successfully");
        responseMap.put("domain", tenant.getDomain() != null ? tenant.getDomain() : "");
        responseMap.put("email", request.getAdminEmail());
        responseMap.put("password", request.getAdminPassword());
        return org.springframework.http.ResponseEntity.ok(responseMap);
    }

    @PostMapping("/forgot-password")
    public org.springframework.http.ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Email is required"));
        }
        
        // Find user by email across tenants
        java.util.List<com.project.www.accessmanagement.entity.User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "User not found"));
        }
        
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        otpStore.put(email, otp);
        
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(email);
            msg.setSubject("Password Reset Verification Code");
            msg.setText("Your OTP for password reset is: " + otp);
            mailSender.send(msg);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "OTP sent to your email"));
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public org.springframework.http.ResponseEntity<?> resetPasswordWithOtp(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        
        if (email == null || otp == null || newPassword == null) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Missing required fields"));
        }
        
        String storedOtp = otpStore.get(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid or expired OTP"));
        }
        
        java.util.List<com.project.www.accessmanagement.entity.User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "User not found"));
        }
        
        // Reset password for all accounts with this email
        for (com.project.www.accessmanagement.entity.User user : users) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
        
        otpStore.remove(email);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Password reset successfully"));
    }
}