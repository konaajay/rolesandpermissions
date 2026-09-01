import urllib.request
import urllib.error
import json

req = urllib.request.Request("http://localhost:8082/api/vendors/received-products")
try:
    with urllib.request.urlopen(req) as response:
        print("Success:", response.read().decode())
except urllib.error.HTTPError as e:
    print(f"HTTP {e.code}")
    print("Body:", e.read().decode())
except Exception as e:
    print("Error:", str(e))
