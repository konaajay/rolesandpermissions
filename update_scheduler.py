import os
import re

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/scheduler/OAuthTokenRefreshScheduler.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove @Value injected fields
content = re.sub(r'\s*@Value\("\$\{google\.client\.id:\}"\)\s*private String googleClientId;\s*', '', content)
content = re.sub(r'\s*@Value\("\$\{google\.client\.secret:\}"\)\s*private String googleClientSecret;\s*', '', content)
content = re.sub(r'\s*@Value\("\$\{zoom\.client\.id:\}"\)\s*private String zoomClientId;\s*', '', content)
content = re.sub(r'\s*@Value\("\$\{zoom\.client\.secret:\}"\)\s*private String zoomClientSecret;\s*', '', content)

# Update refreshGoogle
old_google = '''    private void refreshGoogle(IntegrationCredential cred, Long tiId, Long tenantId) {
        if (googleClientId.isBlank() || googleClientSecret.isBlank()) return;'''

new_google = '''    private void refreshGoogle(IntegrationCredential cred, Long tiId, Long tenantId) {
        String clientId = credentialService.getDecryptedClientId(tiId);
        String clientSecret = credentialService.getDecryptedClientSecret(tiId);
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) return;'''
content = content.replace(old_google, new_google)

# Replace googleClientId with clientId
content = re.sub(r'googleClientId(?!.*\b)', 'clientId', content)
# Replace googleClientSecret with clientSecret
content = re.sub(r'googleClientSecret(?!.*\b)', 'clientSecret', content)

# Update refreshZoom
old_zoom = '''    private void refreshZoom(IntegrationCredential cred, Long tiId, Long tenantId) {
        if (zoomClientId.isBlank() || zoomClientSecret.isBlank()) return;'''

new_zoom = '''    private void refreshZoom(IntegrationCredential cred, Long tiId, Long tenantId) {
        String clientId = credentialService.getDecryptedClientId(tiId);
        String clientSecret = credentialService.getDecryptedClientSecret(tiId);
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) return;'''
content = content.replace(old_zoom, new_zoom)

# Replace zoomClientId with clientId
content = re.sub(r'zoomClientId(?!.*\b)', 'clientId', content)
# Replace zoomClientSecret with clientSecret
content = re.sub(r'zoomClientSecret(?!.*\b)', 'clientSecret', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated OAuthTokenRefreshScheduler.java')
