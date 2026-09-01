import urllib.request
import time

def test_url(url):
    start = time.time()
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req, timeout=3) as resp:
            print(url, "Status:", resp.status, "Time:", round(time.time() - start, 2), "s")
    except urllib.error.HTTPError as e:
        print(url, "HTTP Status:", e.code, "Time:", round(time.time() - start, 2), "s")
    except Exception as e:
        print(url, "Error:", str(e), "Time:", round(time.time() - start, 2), "s")

test_url("http://localhost:8082/api/users")
test_url("http://localhost:8082/vendors/received-products")
