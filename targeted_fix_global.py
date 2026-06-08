import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    (r"import\s+com\.project\.www\.marketing\.entity\.LeadNote\s*;", "import com.project.www.entity.LeadNote;"),
    (r"import\s+com\.project\.www\.marketing\.entity\.LeadProfile\s*;", "import com.project.www.entity.LeadProfile;"),
    (r"import\s+com\.project\.www\.marketing\.entity\.LeadStatusHistory\s*;", "import com.project.www.entity.LeadStatusHistory;"),
    (r"import\s+com\.project\.www\.marketing\.entity\.Lead\s*;", "import com.project.www.entity.Lead;"),
    (r"import\s+com\.project\.www\.entity\.LeadStatus\s*;", "import com.project.www.enums.LeadStatus;"),
    
    (r"import\s+com\.project\.www\.entity\.Campaign\s*;", "import com.project.www.marketing.entity.Campaign;"),
    (r"import\s+com\.project\.www\.entity\.EmailRecipient\s*;", "import com.project.www.marketing.entity.EmailRecipient;"),
    (r"import\s+com\.project\.www\.entity\.EmailCampaign\s*;", "import com.project.www.marketing.entity.EmailCampaign;"),

    (r"import\s+com\.project\.www\.repository\.UserRepository\s*;", "import com.project.www.user.repository.UserRepository;"),
    (r"import\s+com\.project\.www\.repository\.CompanyProfileRepository\s*;", "import com.project.www.tenant.repository.CompanyProfileRepository;"),
    (r"import\s+com\.project\.www\.entity\.CompanyProfile\s*;", "import com.project.www.tenant.entity.CompanyProfile;"),
    (r"import\s+com\.project\.www\.repository\.TenantRepository\s*;", "import com.project.www.tenant.repository.TenantRepository;"),

    (r"import\s+com\.project\.www\.dto\.TenantModuleUpdateRequest\s*;", "import com.project.www.tenant.dto.TenantModuleUpdateRequest;"),
    (r"import\s+com\.project\.www\.repository\.GlobalUserRegistryRepository\s*;", "import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"import\s+com\.project\.www\.service\.TemplateDefinitionService\s*;", "import com.project.www.tenant.service.TemplateDefinitionService;"),
    (r"import\s+com\.project\.www\.service\.GlobalUserRegistrySyncService\s*;", "import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"import\s+com\.project\.www\.service\.TenantDatabaseService\s*;", "import com.project.www.tenant.service.TenantDatabaseService;"),

    (r"import\s+com\.project\.www\.user\.controller\.AuthService\s*;", "import com.project.www.user.service.AuthService;"),
    (r"import\s+com\.project\.www\.service\.TenantService\s*;", "import com.project.www.tenant.service.TenantService;"),
    (r"import\s+com\.project\.www\.dto\.CreateTenantRequest\s*;", "import com.project.www.tenant.dto.CreateTenantRequest;"),
    (r"import\s+com\.project\.www\.service\.AuthService\s*;", "import com.project.www.user.service.AuthService;"),
    
    (r"import\s+com\.project\.www\.user\.controller\.CreateUserRequest\s*;", "import com.project.www.user.dto.CreateUserRequest;"),
    (r"import\s+com\.project\.www\.user\.controller\.ResetPasswordRequest\s*;", "import com.project.www.user.dto.ResetPasswordRequest;"),
    
    (r"import\s+com\.project\.www\.user\.service\.CreateUserRequest\s*;", "import com.project.www.user.dto.CreateUserRequest;"),
    (r"import\s+com\.project\.www\.user\.service\.ResetPasswordRequest\s*;", "import com.project.www.user.dto.ResetPasswordRequest;"),

    # other specific misses from latest log:
    (r"import\s+com\.project\.www\.dto\.PermissionResponse\s*;", "import com.project.www.accessmanagement.dto.PermissionResponse;"),
    (r"import\s+com\.project\.www\.dto\.MapPermissionsRequest\s*;", "import com.project.www.accessmanagement.dto.MapPermissionsRequest;"),
    (r"import\s+com\.project\.www\.dto\.RoleResponse\s*;", "import com.project.www.accessmanagement.dto.RoleResponse;"),
    (r"import\s+com\.project\.www\.dto\.RoleHierarchyResponse\s*;", "import com.project.www.accessmanagement.dto.RoleHierarchyResponse;"),
    
    (r"import\s+com\.project\.www\.dto\.CampaignReportDTO\s*;", "import com.project.www.marketing.dto.CampaignReportDTO;"),
    (r"import\s+com\.project\.www\.dto\.CampaignRequestDTO\s*;", "import com.project.www.marketing.dto.CampaignRequestDTO;"),
    (r"import\s+com\.project\.www\.dto\.CampaignResponseDTO\s*;", "import com.project.www.marketing.dto.CampaignResponseDTO;"),
    (r"import\s+com\.project\.www\.dto\.CouponApplyRequest\s*;", "import com.project.www.marketing.dto.CouponApplyRequest;"),
    (r"import\s+com\.project\.www\.dto\.CouponRequest\s*;", "import com.project.www.marketing.dto.CouponRequest;"),
    (r"import\s+com\.project\.www\.dto\.CreateLeadRequest\s*;", "import com.project.www.marketing.dto.CreateLeadRequest;"),
    (r"import\s+com\.project\.www\.dto\.LandingPageRequest\s*;", "import com.project.www.marketing.dto.LandingPageRequest;"),
    (r"import\s+com\.project\.www\.dto\.LeadCaptureRequest\s*;", "import com.project.www.marketing.dto.LeadCaptureRequest;"),
    (r"import\s+com\.project\.www\.dto\.LeadConversionRequest\s*;", "import com.project.www.marketing.dto.LeadConversionRequest;"),
    (r"import\s+com\.project\.www\.dto\.LeadNoteRequest\s*;", "import com.project.www.marketing.dto.LeadNoteRequest;"),
    (r"import\s+com\.project\.www\.dto\.LeadRequest\s*;", "import com.project.www.marketing.dto.LeadRequest;"),
    (r"import\s+com\.project\.www\.dto\.TrackClickRequest\s*;", "import com.project.www.marketing.dto.TrackClickRequest;"),
    (r"import\s+com\.project\.www\.dto\.TrackedLinkAnalyticsDTO\s*;", "import com.project.www.marketing.dto.TrackedLinkAnalyticsDTO;"),
    (r"import\s+com\.project\.www\.dto\.TrackingRequest\s*;", "import com.project.www.marketing.dto.TrackingRequest;"),
]

for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if not file.endswith('.java'): continue
        filepath = os.path.join(root, file)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        original_content = content
        for old_pattern, new_str in replacements:
            content = re.sub(old_pattern, new_str, content)
            
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed {file}")

print("Done targeted fixes.")
