import os

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/tenant/repository/SubscriptionRepository.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import java.util.List;', 'import java.util.List;\nimport java.util.Optional;')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed SubscriptionRepository imports')
