import os
import shutil
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

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

class_to_module = {}
for module, prefixes in MODULE_MAPPING.items():
    for prefix in prefixes:
        class_to_module[prefix] = module

def get_module_for_file(filename):
    name = filename.replace('.java', '')
    for prefix, module in class_to_module.items():
        if name.startswith(prefix) or name.replace('Impl', '').replace('Controller', '').replace('Service', '').replace('Repository', '').replace('Dto', '') == prefix:
            return module
    if 'Vendor' in name: return 'vendor'
    if 'Tenant' in name: return 'tenant'
    if 'Role' in name or 'Permission' in name: return 'accessmanagement'
    if name == 'AuthServiceImpl' or name == 'AuthController' or name == 'AuthResponse' or name == 'LoginRequest' or name == 'RegisterRequest': return 'user'
    return None

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

impl_dir = os.path.join(BASE_DIR, 'service', 'impl')
if os.path.exists(impl_dir):
    for item in os.listdir(impl_dir):
        if not item.endswith('.java'): continue
        module = get_module_for_file(item)
        if module:
            moves.append(('service/impl', item, module))

for type_dir, item, module in moves:
    src_path = os.path.join(BASE_DIR, type_dir, item)
    if type_dir == 'service/impl':
        dest_dir = os.path.join(BASE_DIR, module, 'service', 'impl')
    else:
        dest_dir = os.path.join(BASE_DIR, module, type_dir)
        
    os.makedirs(dest_dir, exist_ok=True)
    dest_path = os.path.join(dest_dir, item)
    
    # Overwrite destination file unconditionally with shutil.move (replace if exists)
    if os.path.exists(dest_path):
        os.remove(dest_path)
    shutil.move(src_path, dest_path)
    
    new_pkg = f"com.project.www.{module}.{type_dir.replace('/', '.')}"
    with open(dest_path, 'r', encoding='utf-8') as f:
        content = f.read()
    content = re.sub(r'package\s+com\.project\.www\.' + type_dir.replace('/', '\.') + r'\s*;', f"package {new_pkg};", content)
    with open(dest_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Moved files and updated packages.")
