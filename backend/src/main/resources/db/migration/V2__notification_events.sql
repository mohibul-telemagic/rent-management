CREATE TABLE notification_events (
  id BIGSERIAL PRIMARY KEY,
  owner_user_id BIGINT NOT NULL,
  tenant_id BIGINT,
  event_type VARCHAR(50) NOT NULL,
  channel VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  destination VARCHAR(180),
  message TEXT NOT NULL,
  related_entity_type VARCHAR(80) NOT NULL,
  related_entity_id VARCHAR(80) NOT NULL,
  error_message VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at TIMESTAMP NULL,
  CONSTRAINT fk_notification_owner FOREIGN KEY (owner_user_id) REFERENCES users(id),
  CONSTRAINT fk_notification_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_notification_owner_created ON notification_events(owner_user_id, created_at);
CREATE INDEX idx_notification_tenant_created ON notification_events(tenant_id, created_at);
