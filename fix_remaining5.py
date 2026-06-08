import os
import re

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"

replacements = [
    (r"import\s+com\.project\.www\.accessmanagement\.repository\.UserExtraFieldValueRepository\s*;", r"import com.project.www.user.repository.UserExtraFieldValueRepository;"),
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
            
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed {file}")

print("Done remaining fixes round 5.")
