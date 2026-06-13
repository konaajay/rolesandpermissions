import requests
res = requests.post("http://localhost:8080/auth/register-company", json={
    "tenantName":"testtenant2", 
    "adminFirstName":"test", 
    "adminLastName":"test", 
    "adminEmail":"test2@test.com", 
    "adminPassword":"password123"
})
print("STATUS:", res.status_code)
print("BODY:", res.text)
