import os

master_schema_path = r"src\main\resources\master-schema.sql"
schema_path = r"src\main\resources\schema.sql"

with open(schema_path, "r", encoding="utf-8") as f:
    tenant_schema_content = f.read()

with open(master_schema_path, "a", encoding="utf-8") as f:
    f.write("\n\n-- Merged from schema.sql --\n\n")
    f.write(tenant_schema_content)

os.remove(schema_path)
print("Merged schema.sql into master-schema.sql and deleted schema.sql")
