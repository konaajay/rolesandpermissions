import os
import shutil
import re
from pathlib import Path

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

# Map prefixes or exact class names to modules
MODULE_MAPPING = {
    'vendor': [
        'Vendor', 'PurchaseOrder', 'PurchaseOrderItem', 'Requirement', 'RequirementItem', 'Asset'
    ],
    'user': [
        'User', 'GlobalUserRegistry', 'PlatformUser', 'AttendanceShift', 
        'EmployeeCertificate', 'WfhRequest', 'UserReporting'
    ],
    'tenant': [
        'Tenant', 'TenantModule', 'TenantSequence', 'TenantSettings', 'Subscription', 
        'CompanyProfile', 'OfficeLocation', 'OnboardingConfig', 'TemplateDefinition', 'IdFormatSetting'
    ],
    'accessmanagement': [
        'Role', 'RoleHierarchy', 'RoleExtraField', 'Permission', 'UserExtraFieldValue'
    ]
}

TYPES = ['controller', 'service', 'repository', 'dto', 'entity', 'mapper', 'exception']

moved_files = []
deleted_files = []

# Build exact mapping of ClassName -> module
class_to_module = {}
for module, prefixes in MODULE_MAPPING.items():
    for prefix in prefixes:
        class_to_module[prefix] = module

def get_module_for_file(filename):
    name = filename.replace('.java', '')
    # Exact match or starts with
    for prefix, module in class_to_module.items():
        if name.startswith(prefix) or name.replace('Impl', '').replace('Controller', '').replace('Service', '').replace('Repository', '').replace('Dto', '') == prefix:
            return module
    # Special cases
    if 'Vendor' in name: return 'vendor'
    if 'Tenant' in name: return 'tenant'
    if 'Role' in name or 'Permission' in name: return 'accessmanagement'
    if name == 'AuthServiceImpl' or name == 'AuthController' or name == 'AuthResponse' or name == 'LoginRequest' or name == 'RegisterRequest': return 'user'
    return None

# Find all Java files in root packages that belong to a module
moves = []
for type_dir in TYPES:
    src_dir = os.path.join(BASE_DIR, type_dir)
    if not os.path.exists(src_dir): continue
    
    for item in os.listdir(src_dir):
        if not item.endswith('.java'): continue
        if type_dir == 'service' and item == 'impl': continue
        
        module = get_module_for_file(item)
        if module:
            moves.append((type_dir, item, module))

# Also check service/impl
impl_dir = os.path.join(BASE_DIR, 'service', 'impl')
if os.path.exists(impl_dir):
    for item in os.listdir(impl_dir):
        if not item.endswith('.java'): continue
        module = get_module_for_file(item)
        if module:
            moves.append(('service/impl', item, module))

import_updates = {}

for type_dir, item, module in moves:
    src_path = os.path.join(BASE_DIR, type_dir, item)
    if type_dir == 'service/impl':
        dest_dir = os.path.join(BASE_DIR, module, 'service', 'impl')
    else:
        dest_dir = os.path.join(BASE_DIR, module, type_dir)
        
    os.makedirs(dest_dir, exist_ok=True)
    dest_path = os.path.join(dest_dir, item)
    
    if os.path.exists(dest_path) and os.path.abspath(src_path) != os.path.abspath(dest_path):
        # Already copied to dest, but duplicate still exists in root
        deleted_files.append(src_path)
        os.remove(src_path)
    else:
        shutil.move(src_path, dest_path)
        moved_files.append((src_path, dest_path))
        
    # Track for import updates
    class_name = item.replace('.java', '')
    old_pkg = f"com.project.www.{type_dir.replace('/', '.')}"
    new_pkg = f"com.project.www.{module}.{type_dir.replace('/', '.')}"
    import_updates[f"{old_pkg}.{class_name}"] = f"{new_pkg}.{class_name}"
    
    # Update package declaration in the moved file
    with open(dest_path, 'r', encoding='utf-8') as f:
        content = f.read()
    new_content = re.sub(r'package\s+com\.project\.www\.' + type_dir.replace('/', '\.') + r'\s*;', f"package {new_pkg};", content)
    with open(dest_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

# Update imports in all .java files
updated_imports_count = 0
for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                
            original_content = content
            for old_import, new_import in import_updates.items():
                content = re.sub(r'import\s+' + old_import.replace('.', r'\.') + r'\s*;', f"import {new_import};", content)
                
            if content != original_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                updated_imports_count += 1

print(f"REPORT:")
print(f"Files Moved: {len(moved_files)}")
for src, dest in moved_files:
    print(f"Moved: {os.path.basename(src)} -> {dest}")
print(f"\nFiles Deleted (Duplicates Removed): {len(deleted_files)}")
for f in deleted_files:
    print(f"Deleted: {f}")
print(f"\nFiles Updated with new imports: {updated_imports_count}")
