import requests
res = requests.post("http://localhost:8080/auth/login", json={
    "email":"test2@test.com", 
    "password":"password123"
})
print("STATUS:", res.status_code)
print("BODY:", res.text)
