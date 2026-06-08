import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    (r"repository\\LeadNoteRepository\.java", r"import com\.project\.www\.marketing\.entity\.LeadNote;", "import com.project.www.entity.LeadNote;"),
    (r"repository\\LeadProfileRepository\.java", r"import com\.project\.www\.marketing\.entity\.LeadProfile;", "import com.project.www.entity.LeadProfile;"),
    (r"repository\\LeadStatusHistoryRepository\.java", r"import com\.project\.www\.marketing\.entity\.LeadStatusHistory;", "import com.project.www.entity.LeadStatusHistory;"),
    (r"repository\\specification\\LeadSpecifications\.java", r"import com\.project\.www\.marketing\.entity\.Lead;", "import com.project.www.entity.Lead;"),
    (r"repository\\specification\\LeadSpecifications\.java", r"import com\.project\.www\.entity\.LeadStatus;", "import com.project.www.enums.LeadStatus;"),
    
    (r"service\\CampaignScheduler\.java", r"import com\.project\.www\.entity\.Campaign;", "import com.project.www.marketing.entity.Campaign;"),
    (r"service\\ResendEmailService\.java", r"import com\.project\.www\.entity\.EmailRecipient;", "import com.project.www.marketing.entity.EmailRecipient;"),
    (r"service\\ResendEmailService\.java", r"import com\.project\.www\.entity\.EmailCampaign;", "import com.project.www.marketing.entity.EmailCampaign;"),
    (r"service\\email\\EmailProvider\.java", r"import com\.project\.www\.entity\.EmailRecipient;", "import com.project.www.marketing.entity.EmailRecipient;"),
    (r"service\\email\\ResendEmailProvider\.java", r"import com\.project\.www\.entity\.EmailRecipient;", "import com.project.www.marketing.entity.EmailRecipient;"),
    (r"service\\email\\SmtpEmailProvider\.java", r"import com\.project\.www\.entity\.EmailRecipient;", "import com.project.www.marketing.entity.EmailRecipient;"),
    (r"service\\email\\WhatsAppEmailProvider\.java", r"import com\.project\.www\.entity\.EmailRecipient;", "import com.project.www.marketing.entity.EmailRecipient;"),

    (r"tenant\\service\\TemplateDefinitionService\.java", r"import com\.project\.www\.repository\.UserRepository;", "import com.project.www.user.repository.UserRepository;"),
    (r"tenant\\service\\TemplateDefinitionService\.java", r"import com\.project\.www\.repository\.CompanyProfileRepository;", "import com.project.www.tenant.repository.CompanyProfileRepository;"),
    (r"tenant\\service\\TemplateDefinitionService\.java", r"import com\.project\.www\.entity\.CompanyProfile;", "import com.project.www.tenant.entity.CompanyProfile;"),

    (r"tenant\\controller\\TenantController\.java", r"import com\.project\.www\.dto\.TenantModuleUpdateRequest;", "import com.project.www.tenant.dto.TenantModuleUpdateRequest;"),
    (r"tenant\\service\\impl\\TenantModuleServiceImpl\.java", r"import com\.project\.www\.dto\.TenantModuleUpdateRequest;", "import com.project.www.tenant.dto.TenantModuleUpdateRequest;"),

    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.repository\.GlobalUserRegistryRepository;", "import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.service\.TemplateDefinitionService;", "import com.project.www.tenant.service.TemplateDefinitionService;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.service\.GlobalUserRegistrySyncService;", "import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.service\.TenantDatabaseService;", "import com.project.www.tenant.service.TenantDatabaseService;"),

    (r"user\\controller\\AuthController\.java", r"import com\.project\.www\.user\.controller\.AuthService;", "import com.project.www.user.service.AuthService;"),
    (r"user\\controller\\AuthController\.java", r"import com\.project\.www\.service\.TenantService;", "import com.project.www.tenant.service.TenantService;"),
    (r"user\\controller\\AuthController\.java", r"import com\.project\.www\.dto\.CreateTenantRequest;", "import com.project.www.tenant.dto.CreateTenantRequest;"),
    (r"user\\controller\\AuthController\.java", r"import com\.project\.www\.service\.AuthService;", "import com.project.www.user.service.AuthService;"),
    
    (r"user\\controller\\UserController\.java", r"import com\.project\.www\.user\.controller\.CreateUserRequest;", "import com.project.www.user.dto.CreateUserRequest;"),
    (r"user\\controller\\UserController\.java", r"import com\.project\.www\.user\.controller\.ResetPasswordRequest;", "import com.project.www.user.dto.ResetPasswordRequest;"),
    
    (r"user\\service\\UserService\.java", r"import com\.project\.www\.user\.service\.CreateUserRequest;", "import com.project.www.user.dto.CreateUserRequest;"),
    (r"user\\service\\UserService\.java", r"import com\.project\.www\.user\.service\.ResetPasswordRequest;", "import com.project.www.user.dto.ResetPasswordRequest;"),

]

for rel_path, old_str, new_str in replacements:
    filepath = os.path.join(BASE_DIR, rel_path)
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # If it's a regex replace
        new_content = re.sub(old_str, new_str, content)
        
        # Also fix class User missing from user files, etc.
        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Fixed {rel_path}")

# Delete the marketing duplicates from entity folder (I restored them accidentally)
marketing_files = ["Campaign.java", "CampaignPerformance.java", "Content.java", "Coupon.java", "CouponUsage.java", "EmailCampaign.java", "EmailRecipient.java", "Interaction.java", "LandingPage.java", "PipelineStage.java", "TrackedLink.java", "TrafficEvent.java"]
for f in marketing_files:
    p = os.path.join(BASE_DIR, "entity", f)
    if os.path.exists(p):
        os.remove(p)
        print(f"Removed duplicate {p}")
