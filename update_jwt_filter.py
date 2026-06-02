import os

file_path = r"src\main\java\com\project\www\security\JwtFilter.java"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("import com.project.www.config.TenantRoutingDataSource;", "")
content = content.replace("private final DataSource routingDataSource;", "")
content = content.replace("TenantRoutingDataSource rds = (TenantRoutingDataSource) routingDataSource;\n                if (tenantCode != null && rds.containsDataSource(tenantCode)) {", "if (tenantCode != null && tenantRepository.existsByCode(tenantCode)) {")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("JwtFilter updated")
