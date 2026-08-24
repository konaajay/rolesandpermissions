import urllib.request
import urllib.parse
import json

def test():
    try:
        # 1. Test Java Backend Login
        # Wait, I don't know the default user credentials!
        # Do I know them from DatabaseSeeder? Yes! 'superadmin@system.com' / 'superadmin'
        print("Testing Java Login...")
        req = urllib.request.Request("http://localhost:8080/auth/login", method="POST", headers={'Content-Type': 'application/json'}, data=json.dumps({"email": "superadmin@system.com", "password": "superadmin"}).encode('utf-8'))
        with urllib.request.urlopen(req) as res:
            login_data = json.loads(res.read())
            print(f"Login success! Token: {login_data.get('token')[:20]}...")
            token = login_data.get('token')
            
        # 2. Test Python Backend with Token
        print("Testing Python Backend (e.g. /lap-api/leads/ or /lap-api/tasks/)...")
        # Let's hit http://localhost:8000/api/leads/
        # Wait, does Python mount at /api/leads or /lap-api/?
        # The frontend proxy rewrote /api/leads to /leads on 8000! (Wait, rewrite: (path) => path.replace(/^\/api/, '')). So it rewrote to /leads!
        # Let's hit http://localhost:8000/leads/
        req = urllib.request.Request("http://localhost:8000/leads/", headers={'Authorization': f'Bearer {token}'})
        with urllib.request.urlopen(req) as res:
            data = json.loads(res.read())
            print(f"Python Leads API success: {len(data)} items returned.")
            
        return True
    except Exception as e:
        print(f"Error: {e}")
        try:
            print("Response:", e.read().decode())
        except:
            pass
        return False
        
test()
