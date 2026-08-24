import re

def update_config(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Remove the bad line
    content = re.sub(r'properties\.put\("hibernate\.implicit_naming_strategy", "org\.springframework\.boot\.orm\.jpa\.hibernate\.SpringImplicitNamingStrategy"\);\n?', '', content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Updated {filepath}")

update_config('C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/config/PrimaryPersistenceConfig.java')
update_config('C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/config/IntegrationPersistenceConfig.java')
