import requests

JAVA_URL = 'http://localhost:8080'

print('Logging in as Tenant A Admin...')
res = requests.post(f'{JAVA_URL}/auth/login', json={'email': 'adminA@gmail.com', 'password': 'password'}, headers={'X-Tenant-Code': 'TENANTA'})
tokenA = res['token'] if type(res) is dict else res.json()['token']
headersA = {'Authorization': f'Bearer {tokenA}', 'Content-Type': 'application/json', 'X-Tenant-Code': 'TENANTA'}

print('Creating LEAD_VIEW permission...')
res = requests.post(f'{JAVA_URL}/permissions', json={'module': 'LEAD', 'action': 'VIEW', 'description': 'View Leads'}, headers=headersA)
print('Response:', res.text)
