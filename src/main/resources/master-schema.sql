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
    amount_paid DOUBLE DEFAULT 0.0,
    amount_pending DOUBLE DEFAULT 0.0,
    payment_history TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS global_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    tenant_code VARCHAR(255),
    CONSTRAINT uk_email_tenant UNIQUE (email, tenant_code),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);




-- Merged from schema.sql --

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  code VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE,
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

CREATE TABLE IF NOT EXISTS employee_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  show_in_user_form BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS designations (
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

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  gender VARCHAR(50),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  role_id BIGINT,
  phone_number VARCHAR(255),
  office_location_id BIGINT,
  employee_id VARCHAR(100),
  date_of_birth DATE,
  joining_date DATE,
  employee_type_id BIGINT,
  designation_id BIGINT,
  work_mode_id BIGINT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  FOREIGN KEY (role_id) REFERENCES roles(id),
  FOREIGN KEY (office_location_id) REFERENCES office_locations(id) ON DELETE SET NULL,
  FOREIGN KEY (employee_type_id) REFERENCES employee_types(id) ON DELETE SET NULL,
  FOREIGN KEY (designation_id) REFERENCES designations(id) ON DELETE SET NULL,
  FOREIGN KEY (work_mode_id) REFERENCES work_modes(id) ON DELETE SET NULL,
  CONSTRAINT UNIQUE_tenant_email UNIQUE (tenant_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_permissions (
  user_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, permission_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_modules (
  user_id BIGINT NOT NULL,
  module_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (user_id, module_name),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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

CREATE TABLE IF NOT EXISTS id_format_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    prefix VARCHAR(50) NOT NULL,
    padding_length INT NOT NULL DEFAULT 7,
    next_sequence BIGINT NOT NULL DEFAULT 1,
    include_year BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE KEY uk_id_format_tenant_entity (tenant_id, entity_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS template_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    content_html TEXT NOT NULL,
    background_image_url VARCHAR(500),
    is_system_template BOOLEAN NOT NULL DEFAULT FALSE,
    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE KEY uk_template_tenant_code (tenant_id, template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS onboarding_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    auto_generate_id BOOLEAN NOT NULL DEFAULT TRUE,
    send_welcome_email BOOLEAN NOT NULL DEFAULT TRUE,
    generate_document BOOLEAN NOT NULL DEFAULT FALSE,
    document_template_id BIGINT,
    generate_certificate BOOLEAN NOT NULL DEFAULT FALSE,
    certificate_template_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE KEY uk_onboard_tenant_role (tenant_id, role_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (document_template_id) REFERENCES template_definitions(id) ON DELETE SET NULL,
    FOREIGN KEY (certificate_template_id) REFERENCES template_definitions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS company_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    company_code VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    website VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pincode VARCHAR(50),
    gst_number VARCHAR(100),
    pan_number VARCHAR(100),
    registration_number VARCHAR(100),
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    stamp_url VARCHAR(500),
    signature_url VARCHAR(500),
    header_image_url VARCHAR(500),
    footer_image_url VARCHAR(500),
    timezone VARCHAR(100),
    currency VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee_certificates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certificate_no VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    issued_date DATETIME NOT NULL,
    expiry_date DATETIME,
    verification_token VARCHAR(255) NOT NULL UNIQUE,
    pdf_url VARCHAR(500),
    custom_html TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE KEY uk_cert_tenant_no (tenant_id, certificate_no),
    FOREIGN KEY (template_id) REFERENCES template_definitions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendor_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE KEY uk_vc_tenant_name (tenant_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    vendor_code VARCHAR(100) NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    category_id BIGINT,
    contact_person VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    alternate_mobile_number VARCHAR(20),
    company_name VARCHAR(255),
    gst_number VARCHAR(50),
    pan_number VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    status VARCHAR(50),
    rating DOUBLE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    document_url TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (category_id) REFERENCES vendor_categories(id) ON DELETE SET NULL,
    UNIQUE KEY uk_vendor_code (tenant_id, vendor_code),
    UNIQUE KEY uk_vendor_email (tenant_id, email),
    UNIQUE KEY uk_vendor_mobile (tenant_id, mobile_number),
    UNIQUE KEY uk_vendor_gst (tenant_id, gst_number),
    UNIQUE KEY uk_vendor_pan (tenant_id, pan_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    po_number VARCHAR(50) NOT NULL,
    vendor_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    order_date VARCHAR(255),
    delivery_date VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    UNIQUE KEY uk_po_number (po_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    item_description VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2),
    total_price DECIMAL(15,2),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendor_contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    vendor_id BIGINT NOT NULL,
    amount DECIMAL(15,2),
    start_date VARCHAR(255),
    expires VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    document_url TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendor_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL,
    vendor_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    po_ref VARCHAR(255),
    invoice_date VARCHAR(255),
    due_date VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    receipt_url TEXT,
    requirement_id BIGINT,
    amount_paid DECIMAL(15,2) DEFAULT 0.00,
    amount_pending DECIMAL(15,2) DEFAULT 0.00,
    payment_history TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    UNIQUE KEY uk_invoice_number (invoice_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendor_audits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    audit_date VARCHAR(255),
    next_audit VARCHAR(255),
    auditor VARCHAR(255),
    findings TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendor_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT,
    vendor_id BIGINT NOT NULL,
    description TEXT,
    required_date DATE,
    status VARCHAR(50) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS requirement_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    quantity INT NOT NULL,
    unit VARCHAR(50),
    FOREIGN KEY (requirement_id) REFERENCES vendor_requirements(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_departments (
  user_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, department_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_entities (
  user_id BIGINT NOT NULL,
  entity_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, entity_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (entity_id) REFERENCES business_entities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_invoice_installments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_id BIGINT NOT NULL,
  installment_no INT NOT NULL,
  amount DOUBLE NOT NULL,
  due_date DATE NOT NULL,
  paid BOOLEAN NOT NULL DEFAULT FALSE,
  paid_date DATE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (invoice_id) REFERENCES tenant_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE IF NOT EXISTS modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_code VARCHAR(100) NOT NULL UNIQUE,
    module_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(100) NOT NULL UNIQUE,
    plan_name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    billing_cycle VARCHAR(50) NOT NULL,
    trial_days INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plan_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_modules_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_modules_module FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_invoices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  invoice_number VARCHAR(255) NOT NULL UNIQUE,
  amount DOUBLE NOT NULL,
  subtotal DOUBLE,
  tax_amount DOUBLE,
  status VARCHAR(255) NOT NULL,
  due_date DATE,
  invoice_date DATE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_invoice_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_id BIGINT NOT NULL,
  module_name VARCHAR(255) NOT NULL,
  amount DOUBLE,
  extra_charges DOUBLE,
  start_date DATE,
  expiry_date DATE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (invoice_id) REFERENCES tenant_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_invoice_installments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_id BIGINT NOT NULL,
  installment_no INT NOT NULL,
  amount DOUBLE NOT NULL,
  due_date DATE NOT NULL,
  paid BOOLEAN NOT NULL DEFAULT FALSE,
  paid_date DATE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (invoice_id) REFERENCES tenant_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(255),
    value VARCHAR(255),
    status VARCHAR(255),
    assigned_to VARCHAR(255),
    notes TEXT,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    body TEXT,
    author VARCHAR(255),
    status VARCHAR(255),
    published_date DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS integration_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    integration_name VARCHAR(255),
    client_id VARCHAR(255),
    client_secret VARCHAR(255),
    access_token VARCHAR(255),
    refresh_token VARCHAR(255),
    expires_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS integration_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    api_endpoint VARCHAR(255),
    auth_type VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS marketing_leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    source VARCHAR(255),
    status VARCHAR(255),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    email VARCHAR(255),
    password_hash VARCHAR(255),
    role VARCHAR(255),
    status VARCHAR(255),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS push_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    message TEXT,
    recipient VARCHAR(255),
    status VARCHAR(255),
    sent_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price DOUBLE,
    billing_cycle VARCHAR(255),
    features TEXT,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vendor_complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT,
    vendor_id BIGINT,
    subject VARCHAR(255),
    description TEXT,
    status VARCHAR(255),
    date_reported VARCHAR(255),
    resolved_date VARCHAR(255),
    resolution TEXT,
    deleted BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wfh_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    start_date DATE,
    end_date DATE,
    reason TEXT,
    status VARCHAR(255),
    admin_notes TEXT,
    requested_days INT DEFAULT 0,
    created_at DATETIME,
    responded_at DATETIME,
    responded_by_id BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
