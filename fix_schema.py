import os

file_path = r"src\main\resources\schema.sql"
with open(file_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

# Lines are 0-indexed. Line 478 is index 477, line 479 is index 478
# 478:     UNIQUE KEY uk_onboard_tenant_role (tenant_id, role_id),
# 479:     FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,

del lines[478] # This deletes line 479
del lines[477] # This deletes line 478

with open(file_path, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("Fixed schema.sql")
