# RentEase BD v1.0 Technical Specification

Source of truth: `RentEaseBD_PRD_v1.1.md`.
This document translates product requirements into build-ready engineering constraints.
Execution sequence: `implementation-steps.md`.

## 1. System Context
- Product: rent management web app for Bangladesh property owners/managers.
- Currency: BDT only.
- Timezone: Asia/Dhaka (UTC+6) end-to-end.
- Platforms: web only in v1.0; API must remain mobile-consumable.
- Storage: MySQL 8.x primary, SQLite local/dev compatibility profile.

## 2. Architecture
- Frontend: Vue 3 + Vite + Pinia + Vue Router + Axios.
- Backend: Spring Boot 3.x, Java 21, Spring Security, Spring Data JPA.
- API: REST, JSON, versioned under `/api/v1`.
- Files: NID and receipts stored as DB blobs (no object store).
- Reporting: server-side PDF and streamed CSV.

## 3. Roles and Authorization
Roles:
- `OWNER`
- `MANAGER`
- `TENANT` (model prepared; not active in v1.0)

Access control requirements:
- Owner full access including finance, user management, audit logs.
- Manager operational access (tenant/property ops, invoices, expenses) without financial dashboard or user management.
- Endpoint authorization enforced by Spring method security and request filters.

## 4. Security Requirements (Non-Negotiable)
- Passwords hashed with bcrypt cost 12.
- Access token: JWT RS256, TTL 24h.
- Refresh token: opaque token, SHA-256 hash stored, TTL 30d, multi-device rows in DB.
- PII encrypted at rest via AES-256-GCM + per-record IV through JPA converters:
  - `phone_primary`, `phone_secondary`, `nid_number`, `emergency_contact_phone`
- Strict CORS allowlist.
- TLS-only deployment, HSTS enabled.
- Rate limiting:
  - login: 5 attempts / 15 min / IP
  - global API: 100 req/min / user
  - bulk invoice: 1 req / property / minute
- File MIME validated from magic bytes; size max 5MB.
- Audit logs append-only; all write operations audited.

## 5. Core Domain Invariants
- Exactly one active tenant per unit.
- `occupancy_status=OCCUPIED` forbids new active tenant assignment.
- No duplicate non-cancelled/non-void invoice for same tenant-month.
- Invoice line items are snapshots at generation time and immutable for historical correctness.
- Cumulative payments for an invoice cannot exceed total due.
- FIFO payment cannot exceed total outstanding across tenant invoices.
- Deposit refunds + deductions cannot exceed collected deposit balance.
- Soft delete only for tenant/property in v1.0 operational flows.

## 6. Data Model (Implementation Subset)
Primary entities:
- `users`
- `refresh_tokens`
- `properties`
- `property_units`
- `property_settings`
- `utility_charge_configs`
- `tenants`
- `tenant_unit_history`
- `invoices`
- `invoice_line_items`
- `invoice_payments`
- `expenses`
- `security_deposit_transactions`
- `audit_logs`
- `export_jobs`

Schema conventions:
- Monetary fields: `DECIMAL(12,2)`.
- IDs: BIGINT for core domain entities; UUID acceptable where specified (e.g., refresh token id).
- Timestamps stored and interpreted in BST policy.
- All FK relations enforced in DB.
- Flyway migrations are forward-only, versioned, and reversible via explicit rollback scripts when risky.

## 7. API Contract Rules
- Base path: `/api/v1`.
- Response envelope:
  - success: `{ "success": true, "data": ..., "meta": ... }`
  - error: `{ "success": false, "error": { "code": "...", "message": "...", "field": "..." } }`
- Validation failures return deterministic error codes from PRD section 9.
- Pagination required for list endpoints.
- Blob endpoints return binary with accurate `Content-Type`.
- Breaking changes require `/api/v2`.

## 8. Billing and Payment Engine Specification
Invoice generation:
- Inputs: tenant/unit/property snapshot, billing month, selected utility amounts.
- Derive billing period as first/last day of month.
- Due date logic: `due_date = min(configured_day, last_day_of_month)`.
- Late fee applied only when `current_date > due_date + grace_days`.
- Store immutable snapshots:
  - rent amount
  - utility items JSON
  - tax/late fee configuration used
- Bulk mode behavior:
  - process each eligible tenant independently
  - skip duplicates with reason `DUPLICATE_INVOICE`
  - return partial success summary

Payment application:
- Direct endpoint applies to targeted invoice with guardrails.
- FIFO endpoint applies oldest outstanding first until amount exhausted.
- Reject with `OVERPAYMENT` if amount exceeds outstanding total.
- Status transitions are deterministic (`DRAFT` -> `SENT` -> `PARTIALLY_PAID`/`PAID`/`OVERDUE` etc.).

## 9. File Handling Spec
NID image:
- Required on tenant create.
- Accepted MIME: `image/jpeg`, `image/png`.
- Max size 5MB.
- Stored in `MEDIUMBLOB` + MIME type column.

Expense receipt:
- Optional/required per endpoint definition.
- Accepted MIME: `image/jpeg`, `image/png`, `application/pdf`.
- Max size 5MB.

Transaction rule:
- Blob write and entity write are one transaction; any failure rolls back complete operation.

## 10. Reporting and Export Spec
PDF:
- Server-side generated invoice and deposit-refund receipts.
- Language option: `en` or `bn` via query parameter.
- Bangla font embedded; if missing, fallback to English with response header `X-Language-Fallback: en`.

CSV:
- Endpoints for tenants, invoices, payments, expenses, deposits.
- UTF-8 with BOM for Bangla Excel compatibility.
- Large responses streamed.

ZIP export:
- Async job model with status polling.
- States: `PENDING`, `RUNNING`, `COMPLETED`, `FAILED`.

## 11. Observability and Operations
- Structured JSON logs with PII masking.
- Health/metrics/info endpoints via Actuator.
- Daily backups + PITR window 7 days.
- Alerting targets:
  - API latency p95 breach
  - error-rate spike
  - DB growth threshold due to blobs
  - export job failure rate

## 12. Performance Targets
- API p95:
  - reads < 300ms
  - writes/calculation < 800ms
- Dashboard load < 2s with 12-month, 50-property scenario.
- PDF generation < 5s per invoice.
- CSV first byte < 2s for large data sets.

## 13. Testing Strategy
Required suites:
- Unit tests for services/calculators/validators.
- Integration tests for:
  - auth/session flows
  - encryption converters
  - tenant-unit occupancy invariants
  - invoice generation and duplicate protection
  - FIFO payment behavior
  - deposit refund bounds
  - blob upload/download and rollback behavior
- Contract tests for API schema.
- Performance smoke in staging.

Coverage:
- >= 80% on business/service layer.
- Explicit test vectors for PRD edge cases EC-01 through EC-18.

## 14. Delivery Constraints and ADR Policy
- PRD clarified decisions are binding unless superseded by formal change request.
- Any intentional deviation requires ADR before merge.
- No destructive migration in production without rollback path.

## 15. Open Design Decisions for Kickoff (Need Early Lock)
- Choose PDF library (`PDFBox` vs `iText 7 community`) with Bangla rendering benchmark.
- Choose build tool (`Maven` vs `Gradle`) and standardize CI templates.
- Decide audit implementation style (`Envers` vs explicit audit table writes) based on query/reporting needs.

## 16. Acceptance Checklist
- All v1.0 endpoints implemented with role checks and validation rules.
- Financial calculations reproducible and tested.
- Security controls active and verified.
- Non-functional targets met in staging.
- Release runbook complete (deploy, rollback, backup restore, key rotation).

## 17. Implementation Sequence Mapping
Follow this strict order to reduce rework:
1. Auth/security/crypto/audit baseline.
2. Property and tenant domains with occupancy invariants.
3. Invoice engine (snapshot-first design).
4. Payment engine (direct + FIFO).
5. Expense/deposit modules.
6. PDF/CSV/export jobs.
7. Dashboard aggregates after transactional correctness is stable.
8. Performance/security hardening and release controls.

## 18. Non-Negotiable Pre-Merge Checks
Each feature PR must pass:
1. Validation rule tests tied to relevant PRD error codes.
2. Role authorization tests for `OWNER` and `MANAGER`.
3. Audit event assertion for all write paths.
4. No leakage of plaintext PII in response logs or DB columns.
5. API contract snapshot update when payload structure changes.

## 19. Release Gate Metrics
Promotion to production is blocked unless all are true:
1. API p95 targets met under staging load profile.
2. Invoice and payment invariants verified by integration suite.
3. Export and PDF failure rates below agreed thresholds.
4. Backup restore drill completed successfully.
5. High/critical vulnerability count is zero.
