import os
import glob

project_dir = r'e:\ROLES AND PERMISSIONS\Project\src\main\java'

# Replace 'com.project.www.user' with 'com.project.www.accessmanagement'
# Also replace 'com.project.www.user.' with 'com.project.www.accessmanagement.'

for root, dirs, files in os.walk(project_dir):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content.replace('com.project.www.user', 'com.project.www.accessmanagement')
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
