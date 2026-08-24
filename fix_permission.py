import os
import re

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/accessmanagement/entity/Permission.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('"tenantId"', '"tenant_id"')
content = content.replace('private Long tenantId;', '@Column(name = "tenant_id", nullable = false)\n    private Long tenantId;')
# Remove duplicate @Column(nullable = false) if I just prepended it
content = content.replace('@Column(nullable = false)\n    @Column(name = "tenant_id", nullable = false)', '@Column(name = "tenant_id", nullable = false)')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed Permission entity tenant_id mapping')
