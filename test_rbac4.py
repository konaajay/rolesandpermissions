import requests
import json
import time

JAVA_URL = 'http://localhost:8080'
PYTHON_URL = 'http://localhost:8000'

def test():
    print('Logging in as superadmin...')
    res = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'superadmin@system.com', 'password': 'superadmin'})
    super_token = res.json()['token']
    super_headers = {'Authorization': f'Bearer {super_token}', 'Content-Type': 'application/json'}

    print('Logging in as Tenant A Admin...')
    res_adminA = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'adminA@gmail.com', 'password': 'password'}, headers={'X-Tenant-Code': 'TENANTA'})
    tokenA = res_adminA.json()['token']
    headersA = {'Authorization': f'Bearer {tokenA}', 'Content-Type': 'application/json', 'X-Tenant-Code': 'TENANTA'}

    print('Creating LEAD_VIEW permission...')
    requests.post(f'{JAVA_URL}/permissions', json={'module': 'LEAD', 'action': 'VIEW', 'description': 'View Leads'}, headers=headersA)
    print('Creating REPORT_VIEW permission...')
    requests.post(f'{JAVA_URL}/permissions', json={'module': 'REPORT', 'action': 'VIEW', 'description': 'View Reports'}, headers=headersA)

    print('Fetching existing permissions...')
    perms = requests.get(f'{JAVA_URL}/permissions', headers=headersA).json()
    perm_auth = next((p['id'] for p in perms if p['permissionKey'] == 'LEAD_VIEW'), None)
    perm_denied = next((p['id'] for p in perms if p['permissionKey'] == 'REPORT_VIEW'), None)
    print(f'Auth permission ID: {perm_auth}, Denied permission ID: {perm_denied}')
    
    print('Creating Test Role with Authorized permission...')
    requests.post(f'{JAVA_URL}/roles', json={
        'name': 'TEST_ROLE', 'code': 'TEST_ROLE', 'description': 'desc', 'permissionIds': [perm_auth]
    }, headers=headersA)
    
    roles = requests.get(f'{JAVA_URL}/roles', headers=headersA).json()
    role_id = next(r['id'] for r in roles if r['name'] == 'TEST_ROLE')
    print(f'Role created with ID {role_id}')

    print('Creating User in Tenant A...')
    res_user = requests.post(f'{JAVA_URL}/users', json={
        'firstName': 'User', 'lastName': 'A', 'email': 'usera@gmail.com', 'password': 'password',
        'phoneNumber': '3333333333', 'roleIds': [role_id], 'gender': 'MALE'
    }, headers=headersA)
    print(res_user.text)
    
    print('Logging in as User A...')
    res_userA = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'usera@gmail.com', 'password': 'password'}, headers={'X-Tenant-Code': 'TENANTA'})
    userA_token = res_userA.json()['token']
    userA_headers = {'Authorization': f'Bearer {userA_token}', 'X-Tenant-Code': 'TENANTA'}
    
    print('Testing Authorized API...')
    res_auth = requests.get(f'{PYTHON_URL}/leads/', headers=userA_headers)
    print(f'Authorized API Status: {res_auth.status_code}')
    if res_auth.status_code not in [200, 201]: raise Exception('Authorized API failed!')

    print('Testing Denied API (Reports)...')
    res_denied = requests.get(f'{PYTHON_URL}/reports/analytics/', headers=userA_headers)
    print(f'Denied API Status: {res_denied.status_code}')
    if res_denied.status_code != 403: raise Exception(f'Denied API did not return 403, got {res_denied.status_code}')

    print('Removing permission from role...')
    requests.post(f'{JAVA_URL}/roles/{role_id}/permissions', json={'permissionIds': []}, headers=headersA)
    
    print('Testing previously authorized API (should now be 403)...')
    res_revoked = requests.get(f'{PYTHON_URL}/leads/', headers=userA_headers)
    print(f'Revoked API Status: {res_revoked.status_code}')
    if res_revoked.status_code != 403: raise Exception(f'Revoked API did not return 403, got {res_revoked.status_code}')

    print('Restoring permission...')
    requests.post(f'{JAVA_URL}/roles/{role_id}/permissions', json={'permissionIds': [perm_auth]}, headers=headersA)
    res_restored = requests.get(f'{PYTHON_URL}/leads/', headers=userA_headers)
    print(f'Restored API Status: {res_restored.status_code}')
    if res_restored.status_code != 200: raise Exception('Restored API failed!')

    print('Testing Tenant B isolation...')
    res_spoof = requests.get(f'{PYTHON_URL}/leads/', headers={'Authorization': f'Bearer {userA_token}', 'X-Tenant-Code': 'TENANTB'})
    print(f'Spoofed API Status: {res_spoof.status_code}')
    if res_spoof.status_code not in [401, 403, 500, 404]: raise Exception(f'Spoofing tenant returned {res_spoof.status_code}!')

    print('ALL RBAC TESTS PASSED!')

if __name__ == '__main__':
    test()
