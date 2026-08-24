import requests
import json
import time

JAVA_URL = 'http://localhost:8080'
PYTHON_URL = 'http://localhost:8000'

def test():
    # 1. Login as superadmin
    print('Logging in as superadmin...')
    res = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'superadmin@system.com', 'password': 'superadmin'})
    super_token = res.json()['token']
    super_headers = {'Authorization': f'Bearer {super_token}', 'Content-Type': 'application/json'}

    # 2. Create Tenant A & Tenant B
    print('Creating Tenant A and B...')
    resA = requests.post(f'{JAVA_URL}/tenants', json={
        'tenantName': 'Tenant A', 'tenantCode': 'TENANTA', 'adminFirstName': 'Admin', 'adminLastName': 'A',
        'adminEmail': 'adminA@gmail.com', 'adminPassword': 'password', 'phone': '1111111111', 'databaseName': 'tenant_a'
    }, headers=super_headers)
    
    resB = requests.post(f'{JAVA_URL}/tenants', json={
        'tenantName': 'Tenant B', 'tenantCode': 'TENANTB', 'adminFirstName': 'Admin', 'adminLastName': 'B',
        'adminEmail': 'adminB@gmail.com', 'adminPassword': 'password', 'phone': '2222222222', 'databaseName': 'tenant_b'
    }, headers=super_headers)

    # 3. Login as Tenant A Admin
    print('Logging in as Tenant A Admin...')
    res_adminA = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'adminA@gmail.com', 'password': 'password'}, headers={'X-Tenant-Code': 'TENANTA'})
    tokenA = res_adminA.json()['token']
    headersA = {'Authorization': f'Bearer {tokenA}', 'Content-Type': 'application/json', 'X-Tenant-Code': 'TENANTA'}

    # 4. Create Test Permission
    print('Creating Test Permission...')
    res = requests.post(f'{JAVA_URL}/permissions', json={'module': 'CUSTOM', 'action': 'VIEW', 'description': 'Custom View'}, headers=headersA)
    perm_id = res.json().get('id') if res.ok else None
    
    if not perm_id:
        print('Fetching existing permissions as fallback...')
        perms = requests.get(f'{JAVA_URL}/permissions', headers=headersA).json()
        perm_id = next((p['id'] for p in perms if p['permissionKey'] == 'LEAD_VIEW'), None)
        perm_unauth_id = next((p['id'] for p in perms if p['permissionKey'] == 'REPORT_VIEW'), None)
    
    # 5. Create Test Role and map permission
    print(f'Creating Test Role with permission {perm_id}...')
    res = requests.post(f'{JAVA_URL}/roles', json={
        'name': 'TEST_ROLE', 'code': 'TEST_ROLE', 'description': 'desc', 'permissionIds': [perm_id]
    }, headers=headersA)
    role_id = res.json() # Returns string or ID? Usually ID if not string
    if type(role_id) is str and 'successfully' in role_id:
        roles = requests.get(f'{JAVA_URL}/roles', headers=headersA).json()
        role_id = next(r['id'] for r in roles if r['name'] == 'TEST_ROLE')
    elif type(role_id) is dict:
        role_id = role_id.get('id')
        
    print(f'Role created with ID {role_id}')

    # 6. Create Test Tenant User & assign role
    print('Creating User in Tenant A...')
    res = requests.post(f'{JAVA_URL}/users', json={
        'firstName': 'User', 'lastName': 'A', 'email': 'usera@gmail.com', 'password': 'password',
        'phoneNumber': '3333333333', 'roleIds': [role_id], 'gender': 'MALE'
    }, headers=headersA)
    
    # 7. Login as User A
    print('Logging in as User A...')
    res_userA = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'usera@gmail.com', 'password': 'password'}, headers={'X-Tenant-Code': 'TENANTA'})
    userA_token = res_userA.json()['token']
    userA_headers = {'Authorization': f'Bearer {userA_token}', 'X-Tenant-Code': 'TENANTA'}
    
    # 8. Test Authorized API (Python Leads API)
    print('Testing Authorized API...')
    res_auth = requests.get(f'{PYTHON_URL}/leads/', headers=userA_headers)
    print(f'Authorized API Status: {res_auth.status_code}')
    if res_auth.status_code not in [200, 201]: raise Exception('Authorized API failed!')

    # 9. Test Denied API (Python Reports API)
    print('Testing Denied API (Reports)...')
    res_denied = requests.get(f'{PYTHON_URL}/reports/analytics/', headers=userA_headers)
    print(f'Denied API Status: {res_denied.status_code}')
    if res_denied.status_code != 403: raise Exception(f'Denied API did not return 403, got {res_denied.status_code}')

    # 10. Remove permission from role
    print('Removing permission from role...')
    requests.post(f'{JAVA_URL}/roles/{role_id}/permissions', json={'permissionIds': []}, headers=headersA)
    
    # Wait a bit for caches to clear or tokens to sync? Python checks Java live!
    print('Testing previously authorized API (should now be 403)...')
    res_revoked = requests.get(f'{PYTHON_URL}/leads/', headers=userA_headers)
    print(f'Revoked API Status: {res_revoked.status_code}')
    if res_revoked.status_code != 403: raise Exception(f'Revoked API did not return 403, got {res_revoked.status_code}')

    # 11. Restore permission
    print('Restoring permission...')
    requests.post(f'{JAVA_URL}/roles/{role_id}/permissions', json={'permissionIds': [perm_id]}, headers=headersA)
    res_restored = requests.get(f'{PYTHON_URL}/leads/', headers=userA_headers)
    print(f'Restored API Status: {res_restored.status_code}')
    if res_restored.status_code != 200: raise Exception('Restored API failed!')

    # 12. Test Tenant Isolation
    print('Testing Tenant B isolation...')
    # Try to access Tenant B's data using Tenant A's token by spoofing X-Tenant-Code
    res_spoof = requests.get(f'{PYTHON_URL}/leads/', headers={'Authorization': f'Bearer {userA_token}', 'X-Tenant-Code': 'TENANTB'})
    print(f'Spoofed API Status: {res_spoof.status_code}')
    # It should either reject the tenant code or return 403
    if res_spoof.status_code not in [401, 403]: raise Exception(f'Spoofing tenant returned {res_spoof.status_code}!')

    print('ALL RBAC TESTS PASSED!')

if __name__ == '__main__':
    test()
