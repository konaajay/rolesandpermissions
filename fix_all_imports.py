import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"
MODULES = ['vendor', 'user', 'tenant', 'accessmanagement', 'marketing']
TYPES = ['controller', 'service', 'repository', 'dto', 'entity', 'mapper', 'exception']

# 1. Build authoritative class to package map
class_package_map = {}
for module in MODULES:
    for type_dir in TYPES:
        module_type_dir = os.path.join(BASE_DIR, module, type_dir)
        if not os.path.exists(module_type_dir): continue
        
        for root, dirs, files in os.walk(module_type_dir):
            for file in files:
                if not file.endswith('.java'): continue
                class_name = file.replace('.java', '')
                rel_path = os.path.relpath(root, BASE_DIR).replace('\\', '/')
                pkg = f"com.project.www.{rel_path.replace('/', '.')}"
                class_package_map[class_name] = pkg

# Also add impl packages
for module in MODULES:
    impl_dir = os.path.join(BASE_DIR, module, 'service', 'impl')
    if not os.path.exists(impl_dir): continue
    for file in os.listdir(impl_dir):
        if not file.endswith('.java'): continue
        class_name = file.replace('.java', '')
        class_package_map[class_name] = f"com.project.www.{module}.service.impl"

# Also scan root directories to find where things currently live if not moved
for type_dir in TYPES + ['service/impl']:
    root_type_dir = os.path.join(BASE_DIR, type_dir)
    if not os.path.exists(root_type_dir): continue
    for file in os.listdir(root_type_dir):
        if not file.endswith('.java'): continue
        class_name = file.replace('.java', '')
        if class_name not in class_package_map:
            class_package_map[class_name] = f"com.project.www.{type_dir.replace('/', '.')}"

# Ensure Auditable is correctly registered
if 'Auditable' not in class_package_map:
    class_package_map['Auditable'] = 'com.project.www.entity'

# 2. Go through every java file and fix imports
for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if not file.endswith('.java'): continue
        filepath = os.path.join(root, file)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        original_content = content
        
        # Determine package of the current file
        pkg_match = re.search(r'package\s+(.*?);', content)
        if not pkg_match: continue
        current_pkg = pkg_match.group(1).strip()
        
        # Check all known classes
        for class_name, correct_pkg in class_package_map.items():
            if current_pkg == correct_pkg: continue # No import needed
            
            # If the class is explicitly imported from the WRONG package
            wrong_import_pattern = r'import\s+com\.project\.www\.(entity|repository|service|controller|dto|mapper|exception)\.' + class_name + r'\s*;'
            if re.search(wrong_import_pattern, content):
                content = re.sub(wrong_import_pattern, f"import {correct_pkg}.{class_name};", content)
                continue
                
            # If the class is used in the file but not imported, and not same package
            # (Basic check: regex whole word match)
            if re.search(r'\b' + class_name + r'\b', content):
                # Is it already imported correctly?
                if f"import {correct_pkg}.{class_name};" not in content and f"import {correct_pkg}.*;" not in content:
                    # Insert the import after package declaration
                    content = content.replace(f"package {current_pkg};", f"package {current_pkg};\n\nimport {correct_pkg}.{class_name};")
                    
        # Check for SLF4J log errors (usually missing import lombok.extern.slf4j.Slf4j;)
        if 'log.error' in content or 'log.info' in content or 'log.debug' in content or 'log.warn' in content:
            if '@Slf4j' not in content and 'import lombok.extern.slf4j.Slf4j;' not in content:
                content = content.replace('public class', '@lombok.extern.slf4j.Slf4j\npublic class')

        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated imports in {file}")

print("Done updating imports.")
