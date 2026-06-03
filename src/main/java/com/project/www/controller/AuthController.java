package com.project.www.controller;

import com.project.www.dto.AuthResponse;
import com.project.www.dto.LoginRequest;
import com.project.www.dto.RegisterRequest;
import com.project.www.service.AuthService;
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
public class AuthController {

    private final AuthService authService;
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.project.www.service.TenantService tenantService;
    
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

    @PostMapping("/register-company")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> registerCompany(
            @jakarta.validation.Valid @RequestBody com.project.www.dto.CreateTenantRequest request
    ) {
        com.project.www.dto.TenantResponse tenant = tenantService.createTenant(request);
        java.util.Map<String, Object> responseMap = new java.util.HashMap<>();
        responseMap.put("message", "Tenant created successfully");
        responseMap.put("domain", tenant.getDomain() != null ? tenant.getDomain() : "");
        responseMap.put("email", request.getAdminEmail());
        responseMap.put("password", request.getAdminPassword());
        return org.springframework.http.ResponseEntity.ok(responseMap);
    }
}