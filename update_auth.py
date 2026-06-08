import re

file_path = r'E:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\user\service\impl\AuthServiceImpl.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add fields
fields = '''
    @org.springframework.beans.factory.annotation.Autowired
    private com.project.www.user.service.OtpService otpService;
    
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;
    
    @org.springframework.beans.factory.annotation.Value("\")
    private String fromEmail;
'''

content = re.sub(r'(private final com.project.www.user.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;)', r'\1\n' + fields, content)

# Add methods
methods = '''
    @Override
    public void forgotPassword(String email) {
        String originalTenantCode = com.project.www.util.TenantContext.getCurrentTenantCode();
        Long originalTenantId = com.project.www.util.TenantContext.getCurrentTenant();
        try {
            com.project.www.util.TenantContext.clear();
            com.project.www.user.entity.GlobalUserRegistry registryEntry = globalUserRegistryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address."));

            String otp = otpService.generateOtp(email);
            
            org.springframework.mail.SimpleMailMessage msg = new org.springframework.mail.SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(email);
            msg.setSubject("Password Reset OTP");
            msg.setText("Your OTP for password reset is: " + otp + "\\nThis OTP will expire in 10 minutes.");
            mailSender.send(msg);
        } finally {
            com.project.www.util.TenantContext.setCurrentTenant(originalTenantId);
            com.project.www.util.TenantContext.setCurrentTenantCode(originalTenantCode);
        }
    }

    @Override
    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        if (!otpService.validateOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP.");
        }

        String originalTenantCode = com.project.www.util.TenantContext.getCurrentTenantCode();
        Long originalTenantId = com.project.www.util.TenantContext.getCurrentTenant();
        try {
            com.project.www.util.TenantContext.clear();
            com.project.www.user.entity.GlobalUserRegistry registryEntry = globalUserRegistryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address."));
            
            String encodedPassword = passwordEncoder.encode(newPassword);
            registryEntry.setPassword(encodedPassword);
            globalUserRegistryRepository.save(registryEntry);

            com.project.www.util.TenantContext.setCurrentTenant(registryEntry.getTenantId());
            com.project.www.util.TenantContext.setCurrentTenantCode(registryEntry.getTenantCode());
            
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setPassword(encodedPassword);
                userRepository.save(user);
            }
        } finally {
            com.project.www.util.TenantContext.setCurrentTenant(originalTenantId);
            com.project.www.util.TenantContext.setCurrentTenantCode(originalTenantCode);
        }
    }
}'''

content = re.sub(r'}\s*}\s*}$', '}\n' + methods, content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
