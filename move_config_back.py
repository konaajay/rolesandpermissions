import os
import shutil
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"
config_dir = os.path.join(BASE_DIR, 'config')
access_config_dir = os.path.join(BASE_DIR, 'accessmanagement', 'config')

os.makedirs(config_dir, exist_ok=True)

for file in os.listdir(access_config_dir):
    if file.endswith('.java'):
        src = os.path.join(access_config_dir, file)
        dest = os.path.join(config_dir, file)
        
        # Move file back
        shutil.move(src, dest)
        
        # Fix package declaration
        with open(dest, 'r', encoding='utf-8') as f:
            content = f.read()
        
        content = re.sub(r"package\s+com\.project\.www\.accessmanagement\.config\s*;", r"package com.project.www.config;", content)
        
        with open(dest, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"Moved back and restored package for: {file}")

print("Done moving config files back.")
