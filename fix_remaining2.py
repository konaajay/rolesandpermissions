import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    # Remove duplicate @Slf4j more aggressively
    (r"(@lombok\.extern\.slf4j\.Slf4j\s*)+", r"@lombok.extern.slf4j.Slf4j\n"),
    (r"(@Slf4j\s*)+", r"@Slf4j\n"),
    
    (r"@lombok\.extern\.slf4j\.Slf4j\n@Slf4j", r"@Slf4j"),
    (r"@Slf4j\n@lombok\.extern\.slf4j\.Slf4j", r"@Slf4j"),
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

# Specific missing imports from latest log:
specific_fixes = [
    (r"tenant\\service\\impl\\TenantModuleServiceImpl\.java", r"import com\.project\.www\.dto\.TenantModuleUpdateRequest\s*;", r"import com.project.www.tenant.dto.TenantModuleUpdateRequest;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.repository\.GlobalUserRegistryRepository\s*;", r"import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.service\.TemplateDefinitionService\s*;", r"import com.project.www.tenant.service.TemplateDefinitionService;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.service\.GlobalUserRegistrySyncService\s*;", r"import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"tenant\\service\\impl\\TenantServiceImpl\.java", r"import com\.project\.www\.service\.TenantDatabaseService\s*;", r"import com.project.www.tenant.service.TenantDatabaseService;"),
    (r"user\\controller\\AuthController\.java", r"import com\.project\.www\.service\.TenantService\s*;", r"import com.project.www.tenant.service.TenantService;"),
    (r"user\\controller\\AuthController\.java", r"import com\.project\.www\.dto\.CreateTenantRequest\s*;", r"import com.project.www.tenant.dto.CreateTenantRequest;"),
    (r"user\\controller\\PublicVerificationController\.java", r"import com\.project\.www\.repository\.TenantRepository\s*;", r"import com.project.www.tenant.repository.TenantRepository;"),
    (r"user\\service\\GlobalUserRegistrySyncService\.java", r"import com\.project\.www\.repository\.UserRepository\s*;", r"import com.project.www.user.repository.UserRepository;"),
    (r"user\\service\\impl\\AuthServiceImpl\.java", r"import com\.project\.www\.repository\.PermissionRepository\s*;", r"import com.project.www.accessmanagement.repository.PermissionRepository;"),
    (r"user\\service\\impl\\AuthServiceImpl\.java", r"import com\.project\.www\.repository\.GlobalUserRegistryRepository\s*;", r"import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"user\\service\\impl\\AuthServiceImpl\.java", r"import com\.project\.www\.service\.GlobalUserRegistrySyncService\s*;", r"import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"user\\service\\impl\\UserServiceImpl\.java", r"import com\.project\.www\.service\.RoleExtraFieldService\s*;", r"import com.project.www.accessmanagement.service.RoleExtraFieldService;"),
    (r"user\\service\\impl\\UserServiceImpl\.java", r"import com\.project\.www\.service\.GlobalUserRegistrySyncService\s*;", r"import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"user\\service\\impl\\UserServiceImpl\.java", r"import com\.project\.www\.repository\.GlobalUserRegistryRepository\s*;", r"import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"user\\service\\impl\\UserServiceImpl\.java", r"import com\.project\.www\.repository\.TenantRepository\s*;", r"import com.project.www.tenant.repository.TenantRepository;"),
    (r"vendor\\service\\impl\\VendorInvoiceServiceImpl\.java", r"import com\.project\.www\.service\.PdfGenerationService\s*;", r"import com.project.www.vendor.service.PdfGenerationService;"),
]

for rel_path, old_pattern, new_str in specific_fixes:
    filepath = os.path.join(BASE_DIR, rel_path)
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        new_content = re.sub(old_pattern, new_str, content)
        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)

# Add missing slf4j imports for DatabaseSeeder
seeder_path = os.path.join(BASE_DIR, r"accessmanagement\config\DatabaseSeeder.java")
if os.path.exists(seeder_path):
    with open(seeder_path, 'r', encoding='utf-8') as f:
        content = f.read()
    if 'import lombok.extern.slf4j.Slf4j;' not in content:
        content = content.replace("package com.project.www.accessmanagement.config;", "package com.project.www.accessmanagement.config;\n\nimport lombok.extern.slf4j.Slf4j;")
    with open(seeder_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Done remaining fixes round 2.")
