import urllib.request
import time

start = time.time()
try:
    req = urllib.request.Request("http://localhost:8082/api/vendors/received-products")
    with urllib.request.urlopen(req, timeout=5) as resp:
        print("Status:", resp.status)
except urllib.error.HTTPError as e:
    print("HTTP Status:", e.code, "Time taken:", round(time.time() - start, 2), "s")
except Exception as e:
    print("Error:", str(e), "Time taken:", round(time.time() - start, 2), "s")
