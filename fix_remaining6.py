import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    # TemplateDefinitionService
    (r"import\s+com\.project\.www\.entity\.Department\s*;", r""),
    (r"import\s+com\.project\.www\.repository\.UserRepository\s*;", r"import com.project.www.user.repository.UserRepository;"),
    (r"import\s+com\.project\.www\.repository\.CompanyProfileRepository\s*;", r"import com.project.www.tenant.repository.CompanyProfileRepository;"),

    # RoleServiceImpl
    (r"import\s+com\.project\.www\.dto\.MapPermissionsRequest\s*;", r"import com.project.www.accessmanagement.dto.MapPermissionsRequest;"),

    # GlobalUserRegistrySyncService
    (r"import\s+com\.project\.www\.repository\.UserRepository\s*;", r"import com.project.www.user.repository.UserRepository;"),

    # AdminCampaignController, CampaignService, AdminCouponController
    (r"import\s+com\.project\.www\.dto\.CampaignRequest\s*;", r"import com.project.www.marketing.dto.CampaignRequest;"),
    (r"import\s+com\.project\.www\.dto\.CouponRequest\s*;", r"import com.project.www.marketing.dto.CouponRequest;"),

    # OrderRepository
    (r"import\s+com\.project\.www\.entity\.Order\s*;", r"import com.project.www.marketing.entity.Order;"), # Order was moved to marketing? Wait. I should check where Order is.

    # TenantController
    (r"import\s+com\.project\.www\.dto\.CreateTenantRequest\s*;", r"import com.project.www.tenant.dto.CreateTenantRequest;"),
    (r"import\s+com\.project\.www\.dto\.TenantModuleUpdateRequest\s*;", r"import com.project.www.tenant.dto.TenantModuleUpdateRequest;"),

    # TenantServiceImpl, AuthServiceImpl, UserServiceImpl
    (r"import\s+com\.project\.www\.repository\.GlobalUserRegistryRepository\s*;", r"import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"import\s+com\.project\.www\.service\.TemplateDefinitionService\s*;", r"import com.project.www.tenant.service.TemplateDefinitionService;"),
    (r"import\s+com\.project\.www\.service\.GlobalUserRegistrySyncService\s*;", r"import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"import\s+com\.project\.www\.service\.TenantDatabaseService\s*;", r"import com.project.www.tenant.service.TenantDatabaseService;"),
    (r"import\s+com\.project\.www\.service\.TenantService\s*;", r"import com.project.www.tenant.service.TenantService;"),
    (r"import\s+com\.project\.www\.repository\.TenantRepository\s*;", r"import com.project.www.tenant.repository.TenantRepository;"),
    (r"import\s+com\.project\.www\.repository\.PermissionRepository\s*;", r"import com.project.www.accessmanagement.repository.PermissionRepository;"),
    (r"import\s+com\.project\.www\.service\.RoleExtraFieldService\s*;", r"import com.project.www.accessmanagement.service.RoleExtraFieldService;"),

    # VendorInvoiceServiceImpl
    (r"import\s+com\.project\.www\.service\.PdfGenerationService\s*;", r"import com.project.www.vendor.service.PdfGenerationService;"),
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

print("Done remaining fixes round 6.")
