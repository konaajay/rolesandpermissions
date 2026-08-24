import re

def update_config(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    injection = '''        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        properties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
'''
    if 'CamelCaseToUnderscoresNamingStrategy' not in content:
        content = re.sub(r'(properties\.put\("hibernate\.hbm2ddl\.auto"[^\n]+)', r'\1\n' + injection, content)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")
    else:
        print(f"Already updated {filepath}")

update_config('C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/config/PrimaryPersistenceConfig.java')
update_config('C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/config/IntegrationPersistenceConfig.java')
