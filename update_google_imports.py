import os
import re

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/service/impl/GoogleOAuthServiceImpl.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove OAuthUtils import
content = re.sub(r'import com\.project\.www\.integrations\.util\.OAuthUtils;\n?', '', content)

# Add OAuthStateService import if not there
if 'import com.project.www.integrations.service.OAuthStateService;' not in content:
    content = content.replace('import com.project.www.integrations.repository.TenantIntegrationRepository;', 'import com.project.www.integrations.repository.TenantIntegrationRepository;\nimport com.project.www.integrations.service.OAuthStateService;')

# Fix OAuthConnectResponse building
content = content.replace('return new OAuthConnectResponse(url);', 'return OAuthConnectResponse.builder().authUrl(url).build();')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated GoogleOAuthServiceImpl.java imports')
