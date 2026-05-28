CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  code VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT UNIQUE_tenant_role UNIQUE (tenant_id, name),
  CONSTRAINT UNIQUE_tenant_role_code UNIQUE (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS office_locations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  latitude DECIMAL(10, 7) NOT NULL,
  longitude DECIMAL(10, 7) NOT NULL,
  radius_meters DOUBLE NOT NULL DEFAULT 30.0,
  tracking_interval_sec INT NOT NULL DEFAULT 300,
  max_accuracy_meters INT NOT NULL DEFAULT 100,
  max_idle_minutes INT NOT NULL DEFAULT 30,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  KEY idx_office_location_name (name),
  KEY idx_office_location_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  module VARCHAR(255) NOT NULL,
  action VARCHAR(255) NOT NULL,
  permission_key VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT UNIQUE_tenant_permission UNIQUE (tenant_id, permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_hierarchy (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  reports_to_role_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  FOREIGN KEY (reports_to_role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT UNIQUE_role_hierarchy UNIQUE (tenant_id, role_id, reports_to_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_sequences (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  year INT NOT NULL,
  current_sequence BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT UNIQUE_tenant_year UNIQUE (tenant_id, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_settings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL UNIQUE,
  employee_id_format VARCHAR(255),
  lead_id_format VARCHAR(255),
  employee_sequence BIGINT DEFAULT 0,
  lead_sequence BIGINT DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_extra_fields (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  field_name VARCHAR(255) NOT NULL,
  field_label VARCHAR(255) NOT NULL,
  field_type VARCHAR(50) NOT NULL,
  required BOOLEAN NOT NULL DEFAULT FALSE,
  options_json TEXT,
  display_order INT DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT UNIQUE_role_field UNIQUE (role_id, field_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  employee_id VARCHAR(255),
  lead_id VARCHAR(255),
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  gender VARCHAR(50),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  role_id BIGINT,
  department VARCHAR(255),
  designation VARCHAR(255),
  phone_number VARCHAR(255),
  office_location_id BIGINT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (role_id) REFERENCES roles(id),
  FOREIGN KEY (office_location_id) REFERENCES office_locations(id) ON DELETE SET NULL,
  CONSTRAINT UNIQUE_tenant_email UNIQUE (tenant_id, email),
  CONSTRAINT UNIQUE_tenant_employee UNIQUE (tenant_id, employee_id),
  CONSTRAINT UNIQUE_tenant_lead UNIQUE (tenant_id, lead_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_extra_field_values (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  field_id BIGINT NOT NULL,
  field_value TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES role_extra_fields(id) ON DELETE CASCADE,
  CONSTRAINT UNIQUE_user_field UNIQUE (user_id, field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lead_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  lead_id VARCHAR(255) NOT NULL,
  roll_no VARCHAR(255),
  course_id BIGINT,
  created_at DATETIME,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_reporting (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  supervisor_user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (supervisor_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT UNIQUE_user_reporting UNIQUE (tenant_id, user_id, supervisor_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance_shifts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  grace_minutes INT NOT NULL DEFAULT 15,
  min_half_day_minutes INT NOT NULL DEFAULT 240,
  min_full_day_minutes INT NOT NULL DEFAULT 480,
  short_break_start_time TIME,
  short_break_end_time TIME,
  long_break_start_time TIME,
  long_break_end_time TIME,
  office_id BIGINT NOT NULL,
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  FOREIGN KEY (office_id) REFERENCES office_locations(id) ON DELETE CASCADE,
  KEY idx_shift_name (name),
  KEY idx_shift_tenant (tenant_id),
  KEY idx_shift_office (office_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pipeline_stages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  status_value VARCHAR(255) NOT NULL,
  label VARCHAR(255) NOT NULL,
  color VARCHAR(50),
  analytic_bucket VARCHAR(255),
  order_index INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,

  require_note BOOLEAN NOT NULL DEFAULT FALSE,
  require_date BOOLEAN NOT NULL DEFAULT FALSE,
  create_task BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  KEY idx_pipeline_tenant (tenant_id),
  KEY idx_pipeline_order (tenant_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
