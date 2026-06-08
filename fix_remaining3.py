import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    # Fix the JwtFilter and DatabaseSeeder imports
    (r"import\s+com\.project\.www\.entity\.Tenant\s*;", r"import com.project.www.tenant.entity.Tenant;"),
    (r"import\s+com\.project\.www\.entity\.TemplateDefinition\s*;", r"import com.project.www.tenant.entity.TemplateDefinition;"),
    (r"import\s+com\.project\.www\.entity\.Permission\s*;", r"import com.project.www.accessmanagement.entity.Permission;"),
    (r"import\s+com\.project\.www\.entity\.Role\s*;", r"import com.project.www.accessmanagement.entity.Role;"),
    (r"import\s+com\.project\.www\.entity\.User\s*;", r"import com.project.www.user.entity.User;"),

    # Fix UserExtraFieldValue package conflict
    (r"import\s+com\.project\.www\.accessmanagement\.entity\.UserExtraFieldValue\s*;", r"import com.project.www.user.entity.UserExtraFieldValue;"),
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
            
        # specifically remove import com.project.www.user.entity.UserExtraFieldValue; if inside UserExtraFieldValue.java
        if file == 'UserExtraFieldValue.java':
            content = re.sub(r"import\s+com\.project\.www\.user\.entity\.UserExtraFieldValue\s*;", "", content)
            
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed {file}")

print("Done remaining fixes round 3.")
