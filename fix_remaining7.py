import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    # General FQN fixes where com.project.www.dto is used instead of specific modules
    (r"com\.project\.www\.dto\.CreateTenantRequest", r"com.project.www.tenant.dto.CreateTenantRequest"),
    (r"com\.project\.www\.dto\.TenantModuleUpdateRequest", r"com.project.www.tenant.dto.TenantModuleUpdateRequest"),
    
    # Repositories FQNs
    (r"com\.project\.www\.repository\.GlobalUserRegistryRepository", r"com.project.www.user.repository.GlobalUserRegistryRepository"),
    (r"com\.project\.www\.repository\.TenantRepository", r"com.project.www.tenant.repository.TenantRepository"),
    (r"com\.project\.www\.repository\.PermissionRepository", r"com.project.www.accessmanagement.repository.PermissionRepository"),
    
    # Services FQNs
    (r"com\.project\.www\.service\.TemplateDefinitionService", r"com.project.www.tenant.service.TemplateDefinitionService"),
    (r"com\.project\.www\.service\.GlobalUserRegistrySyncService", r"com.project.www.user.service.GlobalUserRegistrySyncService"),
    (r"com\.project\.www\.service\.TenantDatabaseService", r"com.project.www.tenant.service.TenantDatabaseService"),
    (r"com\.project\.www\.service\.TenantService", r"com.project.www.tenant.service.TenantService"),
    (r"com\.project\.www\.service\.RoleExtraFieldService", r"com.project.www.accessmanagement.service.RoleExtraFieldService"),
    (r"com\.project\.www\.service\.PdfGenerationService", r"com.project.www.vendor.service.PdfGenerationService"),
    
    # AuthController fixes
    (r"com\.project\.www\.dto\.LoginRequest", r"com.project.www.user.dto.LoginRequest"),
    (r"com\.project\.www\.dto\.LoginResponse", r"com.project.www.user.dto.LoginResponse"),
    (r"com\.project\.www\.dto\.PublicVerificationDto", r"com.project.www.user.dto.PublicVerificationDto"),
    
    # RoleServiceImpl fixes
    (r"com\.project\.www\.accessmanagement\.dto\.MapPermissionsRequest", r"com.project.www.accessmanagement.dto.MapPermissionsRequest"), # just in case
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

print("Done remaining fixes round 7.")
