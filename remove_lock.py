with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

# Replace findByIdForUpdate with findByIdAndTenantIdAndDeletedFalse
content = content.replace(
    "ReceivedProduct rp = receivedProductRepository.findByIdForUpdate(receivedProductId, tenantId)",
    "ReceivedProduct rp = receivedProductRepository.findByIdAndTenantIdAndDeletedFalse(receivedProductId, tenantId)"
)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.write(content)

print("Removed PESSIMISTIC_WRITE lock")
