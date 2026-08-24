import os

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/config/ApiKeyAuthInterceptor.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add TenantRepository import if not present
if 'import com.project.www.tenant.repository.TenantRepository;' not in content:
    content = content.replace('import com.project.www.integrations.service.ApiKeyService;', 'import com.project.www.integrations.service.ApiKeyService;\nimport com.project.www.tenant.repository.TenantRepository;')

# Add private final TenantRepository tenantRepository; to the constructor/injection
content = content.replace('private final ApiKeyService apiKeyService;', 'private final ApiKeyService apiKeyService;\n    private final TenantRepository tenantRepository;')

# Find TenantContext.setCurrentTenant(validKey.getTenantId());
old_set = 'TenantContext.setCurrentTenant(validKey.getTenantId());'
new_set = '''TenantContext.setCurrentTenant(validKey.getTenantId());
            // Resolve the actual code for DB routing
            tenantRepository.findById(validKey.getTenantId()).ifPresent(tenant -> {
                TenantContext.setCurrentTenantCode(tenant.getCode());
            });'''
content = content.replace(old_set, new_set)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed ApiKeyAuthInterceptor.java')
