import os
import re

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/service/impl/ZoomServiceImpl.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add OAuthStateService import if not there
if 'import com.project.www.integrations.service.OAuthStateService;' not in content:
    content = content.replace('import com.project.www.integrations.repository.TenantIntegrationRepository;', 'import com.project.www.integrations.repository.TenantIntegrationRepository;\nimport com.project.www.integrations.service.OAuthStateService;')

# Remove OAuthUtils import
content = re.sub(r'import com\.project\.www\.integrations\.util\.OAuthUtils;\n?', '', content)

# Add private final OAuthStateService oauthStateService;
content = re.sub(r'(private final RestTemplate restTemplate;)', r'\1\n    private final OAuthStateService oauthStateService;', content)

# Replace buildConnectUrl logic
old_build = '''        String state = tenantId + ":" + System.currentTimeMillis();'''
new_build = '''        String state = oauthStateService.generateState(tenantId, ZOOM_CODE);'''
content = content.replace(old_build, new_build)

# Replace handleCallback logic
old_handle = '''        Long tenantIdFromState = OAuthUtils.extractTenantId(state);'''
new_handle = '''        Long tenantIdFromState = oauthStateService.validateAndExtractTenantId(state, ZOOM_CODE);'''
content = content.replace(old_handle, new_handle)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated ZoomServiceImpl.java')
