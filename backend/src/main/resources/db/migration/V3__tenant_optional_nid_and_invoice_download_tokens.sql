ALTER TABLE tenants
  ALTER COLUMN nid_image DROP NOT NULL,
  ALTER COLUMN nid_image_mime_type DROP NOT NULL;

CREATE TABLE invoice_download_tokens (
  id BIGSERIAL PRIMARY KEY,
  invoice_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  consumed_at TIMESTAMP NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_invoice_download_token_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
  CONSTRAINT fk_invoice_download_token_creator FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT uq_invoice_download_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_invoice_download_token_invoice_created ON invoice_download_tokens(invoice_id, created_at);
CREATE INDEX idx_invoice_download_token_expires ON invoice_download_tokens(expires_at);
