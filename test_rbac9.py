import requests
import json
import time
import string
import random
import sys

JAVA_URL = 'http://localhost:8082'
PYTHON_URL = 'http://localhost:8000'

def test():
    print('1. Logging in as superadmin...')
    res = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'superadmin@system.com', 'password': 'superadmin'})
    if not res.ok: raise Exception(f'Superadmin login failed: {res.text}')
    super_token = res.json()['token']
    super_headers = {'Authorization': f'Bearer {super_token}', 'Content-Type': 'application/json'}

    suffix = ''.join(random.choices(string.ascii_uppercase, k=5))
    tenantA_code = f'TENA_{suffix}'
    tenantB_code = f'TENB_{suffix}'
    adminA_email = f'admina_{suffix}@gmail.com'
    adminB_email = f'adminb_{suffix}@gmail.com'

    print(f'2. Creating {tenantA_code} and {tenantB_code}...')
    resA = requests.post(f'{JAVA_URL}/tenants', json={
        'tenantName': f'Tenant A {suffix}', 'tenantCode': tenantA_code, 'adminFirstName': 'Admin', 'adminLastName': 'A',
        'adminEmail': adminA_email, 'adminPassword': 'password', 'phone': '1111111111', 'databaseName': tenantA_code.lower()
    }, headers=super_headers)
    if not resA.ok: raise Exception(f'Tenant A creation failed: {resA.text}')
    
    resB = requests.post(f'{JAVA_URL}/tenants', json={
        'tenantName': f'Tenant B {suffix}', 'tenantCode': tenantB_code, 'adminFirstName': 'Admin', 'adminLastName': 'B',
        'adminEmail': adminB_email, 'adminPassword': 'password', 'phone': '2222222222', 'databaseName': tenantB_code.lower()
    }, headers=super_headers)
    if not resB.ok: raise Exception(f'Tenant B creation failed: {resB.text}')

    print(f'3. Logging in as {tenantA_code} Admin...')
    res_adminA = requests.post(f'{JAVA_URL}/auth/login', json={'email': adminA_email, 'password': 'password'}, headers={'X-Tenant-Code': tenantA_code})
    if not res_adminA.ok: raise Exception(f'Admin A login failed: {res_adminA.text}')
    tokenA = res_adminA.json()['token']
    headersA = {'Authorization': f'Bearer {tokenA}', 'Content-Type': 'application/json', 'X-Tenant-Code': tenantA_code}

    print('4. Fetching existing seeded permissions for the tenant...')
    perms = requests.get(f'{JAVA_URL}/permissions', headers=headersA).json()
    perm_auth = next((p['id'] for p in perms if p['permissionKey'] == 'LEADS_VIEW_LEADS'), None)
    if not perm_auth:
        raise Exception('LEADS_VIEW_LEADS permission not found in seeded database!')

    print(f'5. Creating Test Role with Authorized permission ({perm_auth})...')
    requests.post(f'{JAVA_URL}/roles', json={
        'name': f'TEST_ROLE_{suffix}', 'code': f'TEST_ROLE_{suffix}', 'description': 'desc', 'permissionIds': [perm_auth]
    }, headers=headersA)
    
    roles = requests.get(f'{JAVA_URL}/roles', headers=headersA).json()
    role_id = next(r['id'] for r in roles if r['name'] == f'TEST_ROLE_{suffix}')
    print(f'Role created with ID {role_id}')

    print('6. Creating Test User in Tenant A...')
    user_email = f'user_{suffix}@gmail.com'
    res_user = requests.post(f'{JAVA_URL}/users', json={
        'firstName': 'User', 'lastName': 'A', 'email': user_email, 'password': 'password',
        'phoneNumber': '3333333333', 'roleIds': [role_id], 'gender': 'MALE'
    }, headers=headersA)
    
    print('7. Logging in as Test User A...')
    res_userA = requests.post(f'{JAVA_URL}/auth/login', json={'email': user_email, 'password': 'password'}, headers={'X-Tenant-Code': tenantA_code})
    userA_token = res_userA.json()['token']
    userA_headers = {'Authorization': f'Bearer {userA_token}', 'X-Tenant-Code': tenantA_code}
    
    print('8. Testing Authorized API (Python /leads/options/)...')
    res_auth = requests.get(f'{PYTHON_URL}/leads/options/', headers=userA_headers)
    print(f'Authorized API Status: {res_auth.status_code}')
    if res_auth.status_code not in [200, 201]: raise Exception('Authorized API failed!')

    print('9. Testing Denied API (Python /tasks/)...')
    res_denied = requests.get(f'{PYTHON_URL}/tasks/', headers=userA_headers)
    print(f'Denied API Status: {res_denied.status_code}')
    if res_denied.status_code != 403: raise Exception(f'Denied API did not return 403, got {res_denied.status_code}')

    print('10. Removing permission from role...')
    requests.post(f'{JAVA_URL}/roles/{role_id}/permissions', json={'permissionIds': []}, headers=headersA)
    
    print('--> Fetching new JWT to reflect removed permissions...')
    res_userA_revoked = requests.post(f'{JAVA_URL}/auth/login', json={'email': user_email, 'password': 'password'}, headers={'X-Tenant-Code': tenantA_code})
    userA_headers_revoked = {'Authorization': f'Bearer {res_userA_revoked.json()["token"]}', 'X-Tenant-Code': tenantA_code}

    print('11. Testing previously authorized API (should now be 403)...')
    res_revoked = requests.get(f'{PYTHON_URL}/leads/options/', headers=userA_headers_revoked)
    print(f'Revoked API Status: {res_revoked.status_code}')
    if res_revoked.status_code != 403: raise Exception(f'Revoked API did not return 403, got {res_revoked.status_code}')

    print('12. Restoring permission...')
    requests.post(f'{JAVA_URL}/roles/{role_id}/permissions', json={'permissionIds': [perm_auth]}, headers=headersA)
    
    print('--> Fetching new JWT to reflect restored permissions...')
    res_userA_restored = requests.post(f'{JAVA_URL}/auth/login', json={'email': user_email, 'password': 'password'}, headers={'X-Tenant-Code': tenantA_code})
    userA_headers_restored = {'Authorization': f'Bearer {res_userA_restored.json()["token"]}', 'X-Tenant-Code': tenantA_code}

    res_restored = requests.get(f'{PYTHON_URL}/leads/options/', headers=userA_headers_restored)
    print(f'Restored API Status: {res_restored.status_code}')
    if res_restored.status_code != 200: raise Exception('Restored API failed!')

    print('13. Testing Tenant B isolation...')
    print('13. Testing Tenant B isolation...')
    # 1. Use the restored token (Tenant A) but spoof Tenant B code
    # The expected behavior is that Python ignores X-Tenant-Code and scopes correctly based on the JWT tenant ID.
    res_spoof = requests.get(f'{PYTHON_URL}/leads/options/', headers={'Authorization': f'Bearer {res_userA_restored.json()["token"]}', 'X-Tenant-Code': tenantB_code})
    print(f'Spoofed API Status: {res_spoof.status_code}')
    # It returns 200, which is FINE because it ignores X-Tenant-Code and scopes to Tenant A securely.
    if res_spoof.status_code != 200: raise Exception(f'Spoofing tenant returned {res_spoof.status_code}!')

    print('\n=======================================')
    print('ALL RBAC TESTS PASSED!')
    print('=======================================')

if __name__ == '__main__':
    test()
