import requests
JAVA_URL = 'http://localhost:8081'
PYTHON_URL = 'http://localhost:8000'
tenantA_code = 'TENA_UKFEH' # From previous run
user_email = 'user_UKFEH@gmail.com'
res_userA = requests.post(f'{JAVA_URL}/auth/login', json={'email': user_email, 'password': 'password'}, headers={'X-Tenant-Code': tenantA_code})
token = res_userA.json()['token']
print("JWT:", token[:30], "...")
me = requests.get(f'{JAVA_URL}/api/auth/me', headers={'Authorization': f'Bearer {token}'})
print("/api/auth/me:", me.text)
