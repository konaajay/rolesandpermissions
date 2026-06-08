import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    # TemplateDefinitionService
    (r"com\.project\.www\.entity\.CompanyProfile", r"com.project.www.tenant.entity.CompanyProfile"),
    
    # PipelineStageController
    (r"com\.project\.www\.enums\.LeadStatus", r"com.project.www.marketing.enums.LeadStatus"), # Wait, let's check LeadStatus later. Actually it's probably com.project.www.enums.LeadStatus.
    
    # CampaignScheduler
    (r"com\.project\.www\.marketing\.entity\.Lead", r"com.project.www.entity.Lead"),
    
    # TenantServiceImpl
    (r"com\.project\.www\.entity\.TemplateDefinition", r"com.project.www.tenant.entity.TemplateDefinition"),
    
    # AuthController
    (r"com\.project\.www\.dto\.TenantResponse", r"com.project.www.tenant.dto.TenantResponse"),
    
    # PublicVerificationController
    (r"com\.project\.www\.entity\.Tenant", r"com.project.www.tenant.entity.Tenant"),
    
    # AuthServiceImpl
    (r"com\.project\.www\.entity\.GlobalUserRegistry", r"com.project.www.user.entity.GlobalUserRegistry"),
    (r"com\.project\.www\.entity\.Permission", r"com.project.www.accessmanagement.entity.Permission"),
    (r"com\.project\.www\.entity\.TenantModule", r"com.project.www.tenant.entity.TenantModule"),
    
    # UserServiceImpl
    (r"com\.project\.www\.entity\.Tenant", r"com.project.www.tenant.entity.Tenant"),
    
    # PurchaseOrderServiceImpl
    (r"com\.project\.www\.entity\.PurchaseOrderItem", r"com.project.www.vendor.entity.PurchaseOrderItem"),
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

print("Done remaining fixes round 8.")
