-- Drop the fake tenant databases
DROP DATABASE IF EXISTS tenant_klk;
DROP DATABASE IF EXISTS tenant_hii;

-- Delete tenant relations first to avoid foreign key constraints
DELETE FROM tenant_modules WHERE tenant_id IN (2, 3);
DELETE FROM tenant_integrations WHERE tenant_id IN (2, 3);
DELETE FROM tenant_settings WHERE tenant_id IN (2, 3);
DELETE FROM subscriptions WHERE tenant_id IN (2, 3);

-- Delete the fake tenants
DELETE FROM tenants WHERE id IN (2, 3);
