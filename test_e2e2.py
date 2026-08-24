import urllib.request
import urllib.parse
import json

def test():
    try:
        print("1. Testing Java Login (Superadmin)...")
        req = urllib.request.Request("http://localhost:8080/auth/login", method="POST", headers={'Content-Type': 'application/json'}, data=json.dumps({"email": "superadmin@system.com", "password": "superadmin"}).encode('utf-8'))
        with urllib.request.urlopen(req) as res:
            login_data = json.loads(res.read())
            print(f"Login success! Token: {login_data.get('token')[:20]}...")
            token = login_data.get('token')
            
        print("2. Testing Python Backend using JWT...")
        req = urllib.request.Request("http://localhost:8000/leads/", headers={'Authorization': f'Bearer {token}'})
        with urllib.request.urlopen(req) as res:
            data = json.loads(res.read())
            print(f"Python Leads API success: {len(data)} items returned.")
            
        print("3. Testing Tenant Creation in Java...")
        payload = {
            "tenantName": "Test Tenant E2E",
            "tenantCode": "E2ETENANT",
            "adminFirstName": "John",
            "adminLastName": "Doe",
            "adminEmail": "testadmine2e@gmail.com",
            "adminPassword": "password123",
            "phone": "9999999999",
            "databaseName": "tenant_e2e"
        }
        req = urllib.request.Request("http://localhost:8080/tenants", method="POST", headers={'Content-Type': 'application/json', 'Authorization': f'Bearer {token}'}, data=json.dumps(payload).encode('utf-8'))
        with urllib.request.urlopen(req) as res:
            tenant_data = json.loads(res.read())
            print(f"Tenant creation success! ID: {tenant_data.get('id')}")
            
        print("4. Testing Tenant Admin Login...")
        req = urllib.request.Request("http://localhost:8080/auth/login", method="POST", headers={'Content-Type': 'application/json', 'X-Tenant-Code': 'E2ETENANT'}, data=json.dumps({"email": "testadmine2e@gmail.com", "password": "password123"}).encode('utf-8'))
        with urllib.request.urlopen(req) as res:
            tenant_login_data = json.loads(res.read())
            tenant_token = tenant_login_data.get('token')
            print(f"Tenant Login success! Token: {tenant_token[:20]}...")
            
        print("5. Testing Python Backend with Tenant JWT...")
        req = urllib.request.Request("http://localhost:8000/leads/", headers={'Authorization': f'Bearer {tenant_token}', 'X-Tenant-Code': 'E2ETENANT'})
        with urllib.request.urlopen(req) as res:
            data = json.loads(res.read())
            print(f"Python Leads API success (Tenant context): {len(data)} items returned.")
            
        print("ALL TESTS PASSED!")
        return True
    except Exception as e:
        print(f"Error: {e}")
        try:
            print("Response:", e.read().decode())
        except:
            pass
        return False
        
test()
