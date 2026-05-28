# Enterprise Multi-Tenant SaaS Architecture

This document details the multi-tenant architecture with dynamic, per-tenant database isolation, dynamic routing, and Role-Based Access Control (RBAC).

---

## 1. High-Level Architecture Overview

The system uses a **Database-per-Tenant** isolation strategy. 
* A **Master Database** (`rbac_db`) stores global configurations, tenant metadata, and administrative information.
* Each registered tenant (e.g., `TenantA`, `Acme Corp`) has its own **isolated physical database** (e.g., `tenant_ten`, `tenant_acm`).
* Switch of databases is done dynamically at runtime on a per-request basis.

```
                          +-------------------------+
                          |   Client Request / UI   |
                          +-------------------------+
                                       |
                   (X-Tenant Header / Subdomain / JWT Claim)
                                       v
                          +-------------------------+
                          |   TenantResolver /      |
                          |      JwtFilter          |
                          +-------------------------+
                                       |
                       (Set Context tenantId & tenantCode)
                                       v
                          +-------------------------+
                          |      TenantContext      |
                          +-------------------------+
                                       |
                        (Look up Current routing key)
                                       v
                          +-------------------------+
                          | TenantRoutingDataSource |
                          +-------------------------+
                                       |
                +----------------------+----------------------+
                |                                             |
                v                                             v
     +--------------------+                       +--------------------+
     |   Master DB        |                       | Tenant Database    |
     | (Metadata/Tenants) |                       | (Users/Roles/etc)  |
     +--------------------+                       +--------------------+
```

---

## 2. Dynamic Datasource Routing

### ThreadLocal Context
* **`TenantContext`**: Stores the current request context (`tenantId` and `tenantCode`) in `ThreadLocal` variables. This ensures thread-isolated database contexts for concurrent web requests.

### Routing Datasource
* **`TenantRoutingDataSource`**: Extends Spring's `AbstractRoutingDataSource`. Overrides `determineCurrentLookupKey()` to retrieve the `tenantCode` from the `TenantContext`.
* **Datasource Cache & Thread Safety**: Datasources are cached in a thread-safe `ConcurrentHashMap`. The registration method `addDataSource(...)` is synchronized and guarded to check if a tenant pool is already registered. This prevents connection pool duplication, connection leaks, and memory waste.

---

## 3. Tenant Resolution Flow

When an HTTP request enters the backend:

1. **`TenantResolver`**:
   * Evaluates the HTTP request to extract the tenant identifier.
   * Checks the `X-Tenant` header (looks for tenant code).
   * Checks the URL subdomain (e.g., `acme.localhost` or `acme.domain.com`).

2. **`JwtFilter`**:
   * For authenticated requests, parses the JWT token.
   * Extracts the `email`, `tenantId`, and `tenantCode` claims.
   * **Fallback Mechanism**: If the client provides a token without a `tenantCode` (e.g., legacy session tokens), the filter temporarily routes to the `"master"` database to resolve the tenant code from the `tenants` table dynamically.
   * Binds the resolved values to the `TenantContext`.

---

## 4. Tenant-Aware Login & Authentication Flow

1. **Client Request**: Client sends email and password, optionally sending the `X-Tenant` header or calling the request from a specific tenant subdomain.
2. **Tenant Resolution**: The server resolves the target tenant from the request (using request body `tenantId` or header-based `X-Tenant`/subdomain via `TenantResolver`).
3. **Database Context Switch**: The server switches the connection pool to the target tenant's database.
4. **Credential Verification**: The server verifies the credentials against the isolated `users` table of the tenant's database.
5. **Token Generation**: Generates a JWT containing:
   * `tenantId`
   * `tenantCode`
   * User role
   * Dynamic permissions
6. **Subsequent Calls**: The client sends the JWT in the `Authorization` header. Every subsequent API call is automatically routed to the correct tenant database.

---

## 5. Automated Onboarding Flow (Dynamic Provisioning)

When a new tenant is created via `POST /tenants`:
1. **Dynamic Database Creation**: A physical database (`tenant_<code_lowercase>`) is created dynamically on the MySQL server.
2. **Schema Automation**: A `schema.sql` script is executed against the new database to initialize isolated tables (`users`, `roles`, `permissions`, `role_permissions`, `tenant_sequences`).
3. **Seeding Defaults**:
   * Core permissions (`USER_CREATE`, `USER_VIEW`, `ROLE_CREATE`, etc.) are batch-saved via `saveAll()`.
   * Standard roles (`ADMIN`, `EMPLOYEE`) are created.
   * An Administrative User account is created and bound to the `ADMIN` role.
   * A year-based sequence is initialized for generating dynamic, company-prefixed employee IDs (e.g., `ACM-2026-001`).
4. **Registration**: The new connection pool is registered in the `TenantRoutingDataSource` cache for immediate routing availability.
