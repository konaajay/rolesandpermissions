import os

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

fixes = {
    r"security\CustomUserDetails.java": ["import com.project.www.user.entity.User;"],
    r"security\ModuleEvaluator.java": ["import com.project.www.tenant.repository.TenantModuleRepository;"],
    r"service\AuthService.java": ["import com.project.www.user.dto.LoginRequest;", "import com.project.www.user.dto.RegisterRequest;", "import com.project.www.user.dto.AuthResponse;"],
    r"service\SecurityService.java": ["import com.project.www.user.repository.UserRepository;", "import com.project.www.user.entity.User;"],
    r"service\impl\CertificateServiceImpl.java": ["import com.project.www.user.repository.UserRepository;"],
    r"tenant\service\CompanyProfileService.java": ["import com.project.www.tenant.repository.TenantRepository;"],
    r"tenant\service\TemplateDefinitionService.java": [
        "import com.project.www.user.repository.UserRepository;", 
        "import com.project.www.tenant.repository.CompanyProfileRepository;", 
        "import com.project.www.tenant.entity.CompanyProfile;"
    ],
    r"vendor\service\impl\VendorComplaintServiceImpl.java": ["import com.project.www.vendor.repository.VendorRepository;"],
}

for rel_path, imports in fixes.items():
    filepath = os.path.join(BASE_DIR, rel_path)
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Determine package
        import re
        pkg_match = re.search(r'package\s+(.*?);', content)
        if pkg_match:
            pkg = pkg_match.group(0)
            imports_str = "\n".join(imports) + "\n"
            content = content.replace(pkg, pkg + "\n\n" + imports_str)
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed {rel_path}")

# Fix DatabaseSeeder log error
ds_path = os.path.join(BASE_DIR, r"accessmanagement\config\DatabaseSeeder.java")
if os.path.exists(ds_path):
    with open(ds_path, 'r', encoding='utf-8') as f:
        content = f.read()
    if '@Slf4j' not in content:
        content = content.replace("public class DatabaseSeeder", "@lombok.extern.slf4j.Slf4j\npublic class DatabaseSeeder")
        with open(ds_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Fixed DatabaseSeeder @Slf4j")
