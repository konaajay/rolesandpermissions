import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"
config_dir = os.path.join(BASE_DIR, 'config')
access_config_dir = os.path.join(BASE_DIR, 'accessmanagement', 'config')

for file in os.listdir(config_dir):
    if file.endswith('.java'):
        access_path = os.path.join(access_config_dir, file)
        orig_path = os.path.join(config_dir, file)
        
        # If it exists in accessmanagement/config, delete the original
        if os.path.exists(access_path):
            os.remove(orig_path)
            print(f"Removed duplicate config: {orig_path}")
        else:
            # If it only exists in config, change its package back to com.project.www.config
            with open(orig_path, 'r', encoding='utf-8') as f:
                content = f.read()
            new_content = re.sub(r"package\s+com\.project\.www\.accessmanagement\.config\s*;", r"package com.project.www.config;", content)
            if new_content != content:
                with open(orig_path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Restored package for: {orig_path}")

print("Done fixing config directory.")
