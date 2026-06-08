import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

fixes = {
    r"security\CustomUserDetails.java": [
        (r'com\.project\.www\.entity\.User', r'com.project.www.user.entity.User')
    ],
    r"security\ModuleEvaluator.java": [
        (r'com\.project\.www\.repository\.TenantModuleRepository', r'com.project.www.tenant.repository.TenantModuleRepository')
    ],
    r"service\AuthService.java": [
        (r'com\.project\.www\.dto\.LoginRequest', r'com.project.www.user.dto.LoginRequest'),
        (r'com\.project\.www\.dto\.RegisterRequest', r'com.project.www.user.dto.RegisterRequest'),
        (r'com\.project\.www\.dto\.AuthResponse', r'com.project.www.user.dto.AuthResponse')
    ],
    r"service\SecurityService.java": [
        (r'com\.project\.www\.repository\.UserRepository', r'com.project.www.user.repository.UserRepository'),
        (r'com\.project\.www\.entity\.User', r'com.project.www.user.entity.User')
    ],
    r"service\impl\CertificateServiceImpl.java": [
        (r'com\.project\.www\.repository\.UserRepository', r'com.project.www.user.repository.UserRepository')
    ],
    r"tenant\service\CompanyProfileService.java": [
        (r'com\.project\.www\.repository\.TenantRepository', r'com.project.www.tenant.repository.TenantRepository')
    ],
    r"tenant\service\TemplateDefinitionService.java": [
        (r'com\.project\.www\.repository\.UserRepository', r'com.project.www.user.repository.UserRepository'),
        (r'com\.project\.www\.repository\.CompanyProfileRepository', r'com.project.www.tenant.repository.CompanyProfileRepository'),
        (r'com\.project\.www\.entity\.CompanyProfile', r'com.project.www.tenant.entity.CompanyProfile'),
        (r'com\.project\.www\.entity\.User', r'com.project.www.user.entity.User')
    ],
    r"vendor\service\impl\VendorComplaintServiceImpl.java": [
        (r'com\.project\.www\.repository\.VendorRepository', r'com.project.www.vendor.repository.VendorRepository')
    ],
}

for rel_path, replacements in fixes.items():
    filepath = os.path.join(BASE_DIR, rel_path)
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original = content
        for pattern, replacement in replacements:
            content = re.sub(pattern, replacement, content)
            
        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed fully qualified names in {rel_path}")

