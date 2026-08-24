import os

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/scheduler/OAuthTokenRefreshScheduler.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('body.add("client_id", googleClientId);', 'body.add("client_id", clientId);')
content = content.replace('body.add("client_secret", googleClientSecret);', 'body.add("client_secret", clientSecret);')
content = content.replace('headers.setBasicAuth(zoomClientId, zoomClientSecret);', 'headers.setBasicAuth(clientId, clientSecret);')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed OAuthTokenRefreshScheduler.java')
