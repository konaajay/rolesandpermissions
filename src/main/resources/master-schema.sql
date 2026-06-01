CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    db_name VARCHAR(255) NOT NULL,
    db_user VARCHAR(255),
    db_password VARCHAR(255),
    admin_email VARCHAR(255),
    super_admin_name VARCHAR(255),
    phone VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    subscription_type VARCHAR(50),
    subscription_start_date DATE,
    subscription_end_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tenant_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    amount DOUBLE,
    payment_method VARCHAR(255),
    special_requirements TEXT,
    extra_charges DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT UNIQUE_tenant_module UNIQUE (tenant_id, module_name)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    plan_name VARCHAR(255),
    amount DOUBLE,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50),
    payment_reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);
