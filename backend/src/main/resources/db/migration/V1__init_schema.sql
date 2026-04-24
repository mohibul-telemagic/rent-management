CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  preferred_language VARCHAR(2) NOT NULL DEFAULT 'en',
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  device_hint VARCHAR(200),
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expires_at);

CREATE TABLE properties (
  id BIGSERIAL PRIMARY KEY,
  property_name VARCHAR(150) NOT NULL,
  address_line1 VARCHAR(200) NOT NULL,
  address_line2 VARCHAR(200),
  thana VARCHAR(100) NOT NULL,
  district VARCHAR(100) NOT NULL,
  division VARCHAR(50) NOT NULL,
  property_type VARCHAR(40) NOT NULL,
  total_units INT NOT NULL,
  owner_notes TEXT,
  owner_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT uq_property_owner_name UNIQUE (owner_id, property_name)
);

CREATE TABLE property_units (
  id BIGSERIAL PRIMARY KEY,
  property_id BIGINT NOT NULL,
  unit_identifier VARCHAR(20) NOT NULL,
  floor_number INT,
  area_sqft DECIMAL(8,2),
  unit_type VARCHAR(20),
  occupancy_status VARCHAR(30) NOT NULL DEFAULT 'VACANT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_property_units_property FOREIGN KEY (property_id) REFERENCES properties(id),
  CONSTRAINT uq_unit_in_property UNIQUE (property_id, unit_identifier)
);

CREATE TABLE property_settings (
  id BIGSERIAL PRIMARY KEY,
  property_id BIGINT NOT NULL UNIQUE,
  invoice_due_day_of_month INT NOT NULL DEFAULT 5,
  late_fee_flat_bdt DECIMAL(12,2) NOT NULL DEFAULT 0,
  late_fee_grace_days INT NOT NULL DEFAULT 0,
  tax_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
  invoice_footer_text VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_property_settings_property FOREIGN KEY (property_id) REFERENCES properties(id)
);

CREATE TABLE utility_charge_configs (
  id BIGSERIAL PRIMARY KEY,
  property_id BIGINT NOT NULL,
  label VARCHAR(100) NOT NULL,
  is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
  display_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_utility_config_property FOREIGN KEY (property_id) REFERENCES properties(id)
);

CREATE TABLE tenants (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  phone_primary BYTEA NOT NULL,
  phone_secondary BYTEA,
  nid_number BYTEA NOT NULL,
  nid_image BYTEA NOT NULL,
  nid_image_mime_type VARCHAR(20) NOT NULL,
  date_of_birth DATE,
  permanent_address TEXT NOT NULL,
  current_address TEXT,
  emergency_contact_name VARCHAR(120) NOT NULL,
  emergency_contact_phone BYTEA NOT NULL,
  emergency_contact_relation VARCHAR(50) NOT NULL,
  lease_start_date DATE NOT NULL,
  lease_end_date DATE,
  monthly_rent_bdt DECIMAL(12,2) NOT NULL,
  security_deposit_bdt DECIMAL(12,2) NOT NULL,
  security_deposit_status VARCHAR(30) NOT NULL DEFAULT 'HELD',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  notes TEXT,
  property_unit_id BIGINT NOT NULL,
  user_id BIGINT,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tenant_unit FOREIGN KEY (property_unit_id) REFERENCES property_units(id),
  CONSTRAINT fk_tenant_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_tenant_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE tenant_unit_history (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  property_unit_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tenant_history_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT fk_tenant_history_unit FOREIGN KEY (property_unit_id) REFERENCES property_units(id)
);

CREATE TABLE invoices (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  property_unit_id BIGINT NOT NULL,
  billing_period_start DATE NOT NULL,
  billing_period_end DATE NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  base_rent_bdt DECIMAL(12,2) NOT NULL,
  utility_charges_json TEXT,
  late_fee_bdt DECIMAL(12,2) NOT NULL DEFAULT 0,
  tax_bdt DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_due_bdt DECIMAL(12,2) NOT NULL,
  balance_due_bdt DECIMAL(12,2) NOT NULL,
  sms_text TEXT,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_invoice_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT fk_invoice_unit FOREIGN KEY (property_unit_id) REFERENCES property_units(id),
  CONSTRAINT fk_invoice_creator FOREIGN KEY (created_by) REFERENCES users(id)
);
CREATE INDEX idx_invoice_tenant_period ON invoices(tenant_id, billing_period_start);

CREATE TABLE invoice_payments (
  id BIGSERIAL PRIMARY KEY,
  invoice_id BIGINT NOT NULL,
  amount_bdt DECIMAL(12,2) NOT NULL,
  payment_date DATE NOT NULL,
  payment_method VARCHAR(30) NOT NULL,
  notes VARCHAR(500),
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_invoice_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
  CONSTRAINT fk_invoice_payment_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE expenses (
  id BIGSERIAL PRIMARY KEY,
  property_id BIGINT NOT NULL,
  category VARCHAR(50) NOT NULL,
  amount_bdt DECIMAL(12,2) NOT NULL,
  expense_date DATE NOT NULL,
  description VARCHAR(500),
  receipt_blob BYTEA,
  receipt_mime_type VARCHAR(40),
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_expense_property FOREIGN KEY (property_id) REFERENCES properties(id),
  CONSTRAINT fk_expense_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE security_deposit_transactions (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  txn_type VARCHAR(30) NOT NULL,
  amount_bdt DECIMAL(12,2) NOT NULL,
  reason VARCHAR(300),
  transaction_date DATE NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_deposit_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT fk_deposit_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE export_jobs (
  id CHAR(36) PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  error_message VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_export_jobs_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  actor_user_id BIGINT NOT NULL,
  action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id VARCHAR(80) NOT NULL,
  old_state_json TEXT,
  new_state_json TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
