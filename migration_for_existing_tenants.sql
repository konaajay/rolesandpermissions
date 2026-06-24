-- SQL Migration Script for Existing Tenant Databases
-- Apply this script sequentially to EACH existing tenant database (and the master DB if not using ddl-auto=update)

-- 1. Create Core Structural Tables
CREATE TABLE IF NOT EXISTS designations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, 
  tenant_id BIGINT NOT NULL, 
  name VARCHAR(255) NOT NULL, 
  description VARCHAR(1000), 
  active BOOLEAN NOT NULL DEFAULT TRUE, 
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE, 
  created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, 
  tenant_id BIGINT NOT NULL, 
  name VARCHAR(255) NOT NULL, 
  description VARCHAR(1000), 
  active BOOLEAN NOT NULL DEFAULT TRUE, 
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE, 
  created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS work_modes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, 
  tenant_id BIGINT NOT NULL, 
  name VARCHAR(255) NOT NULL, 
  description VARCHAR(1000), 
  active BOOLEAN NOT NULL DEFAULT TRUE, 
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE, 
  created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS office_locations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, 
  tenant_id BIGINT NOT NULL, 
  name VARCHAR(100) NOT NULL, 
  latitude DECIMAL(10,7) NOT NULL, 
  longitude DECIMAL(10,7) NOT NULL, 
  radius_meters DOUBLE NOT NULL DEFAULT 30.0, 
  tracking_interval_sec INT NOT NULL DEFAULT 300, 
  max_accuracy_meters INT NOT NULL DEFAULT 100, 
  max_idle_minutes INT NOT NULL DEFAULT 30, 
  created_at DATETIME NOT NULL, 
  updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS business_entities (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, 
  tenant_id BIGINT NOT NULL, 
  entity_code VARCHAR(100) NOT NULL,
  company_name VARCHAR(255) NOT NULL, 
  description VARCHAR(1000), 
  active BOOLEAN NOT NULL DEFAULT TRUE, 
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE, 
  created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, 
  tenant_id BIGINT NOT NULL, 
  dept_code VARCHAR(100) NOT NULL,
  dept_name VARCHAR(255) NOT NULL, 
  description VARCHAR(1000), 
  entity_id BIGINT,
  active BOOLEAN NOT NULL DEFAULT TRUE, 
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE, 
  created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Modify Users Table
ALTER TABLE users ADD COLUMN IF NOT EXISTS designation_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS employee_type_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS office_location_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS work_mode_id BIGINT;

-- 3. Add Many-To-Many Join Tables
CREATE TABLE IF NOT EXISTS user_entities (
  user_id BIGINT NOT NULL, 
  entity_id BIGINT NOT NULL, 
  PRIMARY KEY (user_id, entity_id), 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, 
  FOREIGN KEY (entity_id) REFERENCES business_entities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_departments (
  user_id BIGINT NOT NULL, 
  department_id BIGINT NOT NULL, 
  PRIMARY KEY (user_id, department_id), 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, 
  FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

