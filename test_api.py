import urllib.request
import json

req = urllib.request.Request("http://localhost:8082/vendors/received-products/assignments")
try:
    with urllib.request.urlopen(req) as resp:
        print("Status:", resp.status)
        print("Body:", resp.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print("HTTP Error:", e.code)
    print("Body:", e.read().decode('utf-8'))
except Exception as e:
    print("Error:", str(e))
