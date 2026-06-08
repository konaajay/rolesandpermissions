import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"
MODULES = ['vendor', 'user', 'tenant', 'accessmanagement']
TYPES = ['controller', 'service', 'repository', 'dto', 'entity', 'mapper', 'exception']

import_updates = {}
old_pkg_pattern = re.compile(r'package\s+com\.project\.www\.[a-z]+\s*;')

# 1. Update package declarations for all files in module subdirectories
for module in MODULES:
    for type_dir in TYPES:
        module_type_dir = os.path.join(BASE_DIR, module, type_dir)
        if not os.path.exists(module_type_dir): continue
        
        for root, dirs, files in os.walk(module_type_dir):
            for file in files:
                if not file.endswith('.java'): continue
                filepath = os.path.join(root, file)
                
                # Determine expected package
                rel_path = os.path.relpath(root, BASE_DIR).replace('\\', '/')
                expected_pkg = f"com.project.www.{rel_path.replace('/', '.')}"
                
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Find current package
                pkg_match = re.search(r'package\s+(.*?);', content)
                if pkg_match:
                    current_pkg = pkg_match.group(1).strip()
                    if current_pkg != expected_pkg:
                        # Update package
                        content = content.replace(f"package {current_pkg};", f"package {expected_pkg};")
                        with open(filepath, 'w', encoding='utf-8') as f:
                            f.write(content)
                        print(f"Updated package for {file}: {current_pkg} -> {expected_pkg}")
                        
                        # Add to import updates
                        class_name = file.replace('.java', '')
                        import_updates[f"{current_pkg}.{class_name}"] = f"{expected_pkg}.{class_name}"

# Add Auditable mapping (seems it's still in entity, but we need to ensure it's imported)
import_updates['com.project.www.entity.Auditable'] = 'com.project.www.entity.Auditable' # Just to keep in mind, wait, Auditable was in root entity. If some files moved to subpackages, they need to import it if they don't share the package anymore.

# Find any classes that moved from root to modules, their imports might be missing.
# If a file is in com.project.www.tenant.entity and uses Auditable, it needs to import com.project.www.entity.Auditable since they are no longer in the same package.
missing_imports_to_add = {
    'Auditable': 'com.project.www.entity.Auditable',
    'User': 'com.project.www.user.entity.User',
    'Tenant': 'com.project.www.tenant.entity.Tenant',
    'Role': 'com.project.www.accessmanagement.entity.Role',
    'Permission': 'com.project.www.accessmanagement.entity.Permission',
    'OfficeLocation': 'com.project.www.tenant.entity.OfficeLocation',
    'CompanyProfile': 'com.project.www.tenant.entity.CompanyProfile',
    'GlobalUserRegistry': 'com.project.www.user.entity.GlobalUserRegistry',
    'PlatformUser': 'com.project.www.user.entity.PlatformUser',
    'Vendor': 'com.project.www.vendor.entity.Vendor'
}

print(f"Import updates found: {len(import_updates)}")

# 2. Update imports globally
for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                
            original_content = content
            
            # 1. Update explicitly changed packages
            for old_import, new_import in import_updates.items():
                content = re.sub(r'import\s+' + old_import.replace('.', r'\.') + r'\s*;', f"import {new_import};", content)
                
            # 2. Add missing cross-module imports (naive approach)
            pkg_match = re.search(r'package\s+(.*?);', content)
            if pkg_match:
                pkg = pkg_match.group(1).strip()
                # Check for class usages that might need an import
                for class_name, import_path in missing_imports_to_add.items():
                    if import_path.startswith(pkg): continue # Same package
                    
                    # If class is used and not imported
                    if re.search(r'\b' + class_name + r'\b', content) and f"import {import_path};" not in content:
                        # Add import after package
                        content = content.replace(f"package {pkg};", f"package {pkg};\n\nimport {import_path};")
                        
            if content != original_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated imports for {filepath}")

# Let's also check for lombok @Slf4j error
# "cannot find symbol variable log" in DatabaseSeeder.java. It means @Slf4j is missing.
seeder_path = os.path.join(BASE_DIR, 'accessmanagement', 'config', 'DatabaseSeeder.java')
if os.path.exists(seeder_path):
    with open(seeder_path, 'r', encoding='utf-8') as f:
        c = f.read()
    if '@Slf4j' not in c and 'import lombok.extern.slf4j.Slf4j;' not in c:
        c = c.replace('public class DatabaseSeeder', '@lombok.extern.slf4j.Slf4j\npublic class DatabaseSeeder')
        with open(seeder_path, 'w', encoding='utf-8') as f:
            f.write(c)

