import os
import re

directory = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations'

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Replace @Transactional(readOnly = true) with @Transactional(value = "integrationTransactionManager", readOnly = true)
            new_content = re.sub(r'@Transactional\s*\(\s*readOnly\s*=\s*true\s*\)', '@Transactional(value = "integrationTransactionManager", readOnly = true)', content)
            
            # Replace @Transactional (without parenthesis) with @Transactional("integrationTransactionManager")
            new_content = re.sub(r'@Transactional(?!\()', '@Transactional("integrationTransactionManager")', new_content)
            
            # Replace @Transactional(propagation = Propagation.REQUIRES_NEW) etc if any
            new_content = re.sub(r'@Transactional\s*\(\s*propagation', '@Transactional(value = "integrationTransactionManager", propagation', new_content)

            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f'Updated {filepath}')
