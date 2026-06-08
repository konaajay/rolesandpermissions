import os
import shutil
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

# Get all 0-byte java files
zero_byte_files = []
for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            if os.path.getsize(filepath) == 0:
                zero_byte_files.append(filepath)

# For each 0-byte file, try to find the actual code file in the old locations
for zero_path in zero_byte_files:
    filename = os.path.basename(zero_path)
    
    # Possible old locations
    possible_roots = [
        os.path.join(BASE_DIR, 'controller'),
        os.path.join(BASE_DIR, 'service'),
        os.path.join(BASE_DIR, 'repository'),
        os.path.join(BASE_DIR, 'dto'),
        os.path.join(BASE_DIR, 'entity'),
        os.path.join(BASE_DIR, 'mapper'),
        os.path.join(BASE_DIR, 'exception'),
        os.path.join(BASE_DIR, 'repository', 'specification'),
        os.path.join(BASE_DIR, 'service', 'impl')
    ]
    
    found_src = None
    for pr in possible_roots:
        src_path = os.path.join(pr, filename)
        if os.path.exists(src_path) and os.path.getsize(src_path) > 0:
            found_src = src_path
            break
            
    if found_src:
        print(f"Moving {found_src} to {zero_path}")
        os.remove(zero_path)
        shutil.move(found_src, zero_path)
        
        # Fix package declaration in the newly moved file
        rel_dir = os.path.relpath(os.path.dirname(zero_path), BASE_DIR).replace('\\', '/')
        new_pkg = f"com.project.www.{rel_dir.replace('/', '.')}"
        
        with open(zero_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        content = re.sub(r'package\s+com\.project\.www\.[a-zA-Z0-9_\.]+\s*;', f"package {new_pkg};", content)
        
        with open(zero_path, 'w', encoding='utf-8') as f:
            f.write(content)

print("Done moving remaining 0-byte files.")
