import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    # Remove duplicate @Slf4j
    (r"@lombok\.extern\.slf4j\.Slf4j\n@lombok\.extern\.slf4j\.Slf4j", r"@lombok.extern.slf4j.Slf4j"),
    (r"@Slf4j\n@lombok\.extern\.slf4j\.Slf4j", r"@Slf4j"),
    (r"@lombok\.extern\.slf4j\.Slf4j\n@Slf4j", r"@Slf4j"),

    # Fix DatabaseSeeder package
    (r"package\s+com\.project\.www\.config\s*;", r"package com.project.www.accessmanagement.config;"),

    # Fix ambiguous Order import
    (r"import\s+com\.project\.www\.entity\.Order\s*;", r""),

    # Missing TenantDatabaseService, GlobalUserRegistrySyncService, TemplateDefinitionService, RoleExtraFieldService, etc
    (r"import\s+com\.project\.www\.service\.TenantDatabaseService\s*;", r"import com.project.www.tenant.service.TenantDatabaseService;"),
    (r"import\s+com\.project\.www\.service\.GlobalUserRegistrySyncService\s*;", r"import com.project.www.user.service.GlobalUserRegistrySyncService;"),
    (r"import\s+com\.project\.www\.service\.TemplateDefinitionService\s*;", r"import com.project.www.tenant.service.TemplateDefinitionService;"),
    (r"import\s+com\.project\.www\.service\.RoleExtraFieldService\s*;", r"import com.project.www.accessmanagement.service.RoleExtraFieldService;"),
    (r"import\s+com\.project\.www\.service\.PdfGenerationService\s*;", r"import com.project.www.vendor.service.PdfGenerationService;"),

    # Missing DTOs for accessmanagement
    (r"import\s+com\.project\.www\.dto\.PermissionResponse\s*;", r"import com.project.www.accessmanagement.dto.PermissionResponse;"),
    (r"import\s+com\.project\.www\.dto\.MapPermissionsRequest\s*;", r"import com.project.www.accessmanagement.dto.MapPermissionsRequest;"),
    (r"import\s+com\.project\.www\.dto\.RoleResponse\s*;", r"import com.project.www.accessmanagement.dto.RoleResponse;"),
    (r"import\s+com\.project\.www\.dto\.RoleHierarchyResponse\s*;", r"import com.project.www.accessmanagement.dto.RoleHierarchyResponse;"),

    # Missing repositories
    (r"import\s+com\.project\.www\.repository\.UserExtraFieldValueRepository\s*;", r"import com.project.www.user.repository.UserExtraFieldValueRepository;"),
    (r"import\s+com\.project\.www\.repository\.LeadRepository\s*;", r"import com.project.www.marketing.repository.LeadRepository;"),
    (r"import\s+com\.project\.www\.repository\.LandingPageRepository\s*;", r"import com.project.www.marketing.repository.LandingPageRepository;"),
    (r"import\s+com\.project\.www\.repository\.TenantRepository\s*;", r"import com.project.www.tenant.repository.TenantRepository;"),
    (r"import\s+com\.project\.www\.repository\.GlobalUserRegistryRepository\s*;", r"import com.project.www.user.repository.GlobalUserRegistryRepository;"),
    (r"import\s+com\.project\.www\.repository\.PermissionRepository\s*;", r"import com.project.www.accessmanagement.repository.PermissionRepository;"),
    (r"import\s+com\.project\.www\.repository\.UserRepository\s*;", r"import com.project.www.user.repository.UserRepository;"),

    # Missing services
    (r"import\s+com\.project\.www\.service\.MarketingAnalyticsService\s*;", r"import com.project.www.marketing.service.MarketingAnalyticsService;"),
    (r"import\s+com\.project\.www\.service\.CampaignService\s*;", r"import com.project.www.marketing.service.CampaignService;"),
    (r"import\s+com\.project\.www\.service\.CouponService\s*;", r"import com.project.www.marketing.service.CouponService;"),
    (r"import\s+com\.project\.www\.service\.TrackedLinkService\s*;", r"import com.project.www.marketing.service.TrackedLinkService;"),
    (r"import\s+com\.project\.www\.service\.PerformanceService\s*;", r"import com.project.www.marketing.service.PerformanceService;"),
    (r"import\s+com\.project\.www\.service\.ContentService\s*;", r"import com.project.www.marketing.service.ContentService;"),
    (r"import\s+com\.project\.www\.service\.InteractionService\s*;", r"import com.project.www.marketing.service.InteractionService;"),
    (r"import\s+com\.project\.www\.service\.LandingPageService\s*;", r"import com.project.www.marketing.service.LandingPageService;"),
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

# Handle duplicate classes
def remove_duplicate_class(filepath):
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # Simplistic approach: if there's multiple "public class X", keep the first one and everything before it, 
        # or properly parse. Actually, if I moved a file over another file, they might have concatenated if my script did something weird?
        # No, move overwrites. But wait, did I use "Set-Content" in powershell that appended? No, shutil.move overwrites.
        # Let's just check if there are 2 package declarations.
        if content.count('package ') > 1:
            idx = content.find('package ', 10)
            if idx != -1:
                content = content[:idx]
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Removed duplicate class content in {filepath}")

remove_duplicate_class(os.path.join(BASE_DIR, 'config', 'WebConfig.java'))
remove_duplicate_class(os.path.join(BASE_DIR, 'user', 'entity', 'UserExtraFieldValue.java'))
