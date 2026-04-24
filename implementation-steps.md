# RentEase BD v1.0 Implementation Steps

This document converts the PRD, `plan.md`, and `spec.md` into an execution sequence that engineering can implement directly.

## 0. Current Progress (2026-03-06)
- Completed: Step 1 Delivery Setup
- Completed: Step 2 Database and Migration Baseline (initial schema bootstrap)
- Completed: Step 3 Security and Authentication (core JWT/refresh flows scaffolded)
- Completed: Step 4 Crypto and Audit baseline (AES converter infrastructure + audit log writes in auth flows)
- Completed: Step 5 Property and Unit Module (backend CRUD + settings/config APIs + UI screens)
- In Progress: Step 6 Tenant Module (backend CRUD + NID blob endpoints + occupancy/history invariants)
- Completed: Step 7 Invoice Engine (single/bulk generation + lifecycle + UI delivered)
- Completed: Step 8 Payment Engine MVP (direct + FIFO payment APIs, overpayment protection, status transitions, invoice UI payment actions)
- Completed: Step 9 PDF + Export MVP (invoice PDF + deposit refund receipt PDF + CSV endpoints + async ZIP export jobs)
- Completed: Step 10 Expenses + Deposits MVP (expense CRUD + receipt blobs + deposit ledger + move-out settlement workflow + UI screens)
- Completed: Step 11 Dashboard + Analytics MVP (summary/trend/property-breakdown/overdue-aging APIs + live dashboard UI)
- In Progress: Step 12 Quality Hardening (integration coverage expansion + runbooks baseline completed; security/perf/WCAG execution pending)
- Completed: Step 13 Tracking Matrix (weekly review artifact and checkpoint process doc added)

### Simple Progress Table (Code-Verified)
| Step | Status | Notes |
|---|---|---|
| 1. Delivery Setup | In Progress | Monorepo and runnable apps exist; CI/branch policy artifacts are partial in repo. |
| 2. DB + Migration Baseline | In Progress | Core schema/migrations exist; DB-level active-invoice/invariant hard constraints are partial. |
| 3. Security + Auth | Done | Login/refresh/logout/session list+revoke/rate limit/CORS allowlist/frontend guards are implemented. |
| 4. Crypto + Audit | In Progress | AES at-rest encryption and broad audit writes are present; masking policy remains incomplete. |
| 5. Property + Unit | Done | CRUD, settings, utility config, and UI are live with owner/manager policy at API level. |
| 6. Tenant Module | In Progress | Full lifecycle UI + backend exist (edit/move/renew/deactivate/NID/history/ledger); DB invariants are still partly service-layer only. |
| 7. Invoice Engine | Done | Single/bulk generation, lifecycle actions, filters, payment hooks, and invoice UX are implemented. |
| 8. Payment Engine | Done | Direct + FIFO payments with status transitions and audit trail are implemented. |
| 9. PDF + Export | Done | Invoice/deposit PDFs, CSV export, and async ZIP workflow are implemented. |
| 10. Expenses + Deposits | Done | Expense CRUD/receipts + deposit ledger/move-out settlement workflows are implemented. |
| 11. Dashboard + Analytics | Done | Summary/trend/property breakdown/overdue aging APIs + dashboard filters and drilldowns are implemented. |
| 12. Quality Hardening | In Progress | Test baseline and runbooks exist; full perf/security/WCAG hardening is still pending. |
| 13. Tracking Matrix | Done | Weekly tracking matrix and checkpoint workflow are documented. |

### Code-Verified Progress Table (Snapshot: 2026-03-06)
Status is based on current repository code, not plan intent.

| Step | Done Items | In Progress Items | Not Started Items | Notes |
|---|---|---|---|---|
| 1. Delivery Setup | 1 | 2, 3 | 4 | Monorepo exists; CI and env profiles are partial; branch protection/checklist not represented in repo code. |
| 2. DB + Migration Baseline | 1, 3 | 2 | - | Major schema is present; uniqueness/index rules for active-invoice behavior are partial. |
| 3. Security + Auth | 1, 2, 3, 6 | 4, 5 | - | Core auth and frontend guards exist; session listing and rate limiting are not fully implemented. |
| 4. Crypto + Audit | 1, 2 | 3 | 4 | AES converter + key wiring exist; audit coverage is partial; masking rules are not implemented. |
| 5. Property + Unit | 1, 2, 3, 4 | - | - | Property CRUD, unit CRUD, property settings, and utility config are implemented in backend + UI. |
| 6. Tenant Module | 1, 2, 3 | 4, 5 | - | Service-level occupancy controls and history are implemented; DB-level invariant constraints and full tenant form UX parity remain. |
| 7. Invoice Engine | 1, 2, 3, 4, 5, 6 | - | - | Invoice service/API/UI now support single+bulk generation, snapshots, due/late logic, lifecycle actions, and SMS text storage. |
| 8. Payment Engine | 1, 2, 3, 4 | - | - | Direct and FIFO payment endpoints are implemented with allocation responses, overpayment guards, audit events, and invoice status updates (`PARTIALLY_PAID`, `PAID`, `OVERDUE`). |
| 9. PDF + Export | 1, 2, 3, 4 | - | - | Implemented invoice PDF (`en`/`bn` with `X-Language-Fallback: en`), deposit refund receipt PDF endpoint, CSV export endpoints, and async ZIP export jobs with `PENDING/RUNNING/COMPLETED/FAILED`. |
| 10. Expenses + Deposits | 1, 2, 3 | - | - | Implemented expense CRUD with receipt blob endpoints (upload/download/remove + MIME/size validation), deposit ledger with bounds validation, move-out settlement hook with tenant deactivation path, refund receipt PDF endpoint reuse, and frontend Expenses/Deposits screens. |
| 11. Dashboard + Analytics | 1, 2, 3 | - | - | Implemented dashboard analytics endpoints (`summary`, `trend`, `property-breakdown`, `overdue-aging`) with owner/manager access scoping and a live frontend dashboard in Overview. |
| 12. Quality Hardening | 1, 5 | 2, 3, 4 | - | Added dashboard + financial invariant integration tests and runbook docs (deployment/rollback/backup/key-rotation); security checklist execution, perf profiling, and WCAG validation remain pending. |
| 13. Tracking Matrix | 1, 2, 3 | - | - | Added weekly tracking artifact with status/owner/blocker/evidence fields and mandatory checkpoint workflow (`docs/tracking-matrix.md`). |

## 1. Delivery Setup (Days 1-3)
1. Create monorepo layout and baseline tooling:
   - `backend/` Spring Boot 3.3.x (Java 21)
   - `frontend/` Vue 3 + Vite
   - `docs/adr/` ADR templates
2. Configure CI pipeline:
   - Backend: compile, unit tests, static checks
   - Frontend: typecheck, unit tests, lint
3. Add environment profiles:
   - `local` (SQLite optional), `dev/staging/prod` (MySQL)
4. Define default branch protections and PR checklist.

Done when:
- Fresh clone can run backend and frontend locally.
- CI is green on the default branch.

## 2. Database and Migration Baseline (Days 4-6)
1. Initialize Flyway migration chain and create base tables:
   - `users`, `refresh_tokens`, `audit_logs`
   - `properties`, `property_units`, `property_settings`, `utility_charge_configs`
   - `tenants`, `tenant_unit_history`
   - `invoices`, `invoice_line_items`, `invoice_payments`
   - `expenses`, `security_deposit_transactions`, `export_jobs`
2. Add foreign keys, indexes, and uniqueness constraints:
   - Unique active invoice per `(tenant_id, billing_period_start)` with status filter logic
   - Unique `unit_identifier` per property
3. Add timezone and monetary conventions in schema and ORM mappings.

Done when:
- Migrations run successfully on MySQL and local profile.
- Referential integrity tests pass.

## 3. Security and Authentication (Week 2)
1. Implement password hashing (`BCryptPasswordEncoder`, cost 12).
2. Implement JWT access token (RS256, 24h) and DB refresh token flow (30d).
3. Implement endpoints:
   - `POST /api/v1/auth/login`
   - `POST /api/v1/auth/refresh`
   - `POST /api/v1/auth/logout`
   - `POST /api/v1/auth/logout-all`
4. Implement session listing/revocation endpoints.
5. Enforce rate limiting and CORS allowlist.
6. Add frontend auth store + axios interceptor + route guards.

Done when:
- Multi-device sessions work end-to-end.
- Role-protected route access is verified by tests.

## 4. Crypto and Audit (Week 2)
1. Build AES-256-GCM attribute converters for PII fields.
2. Add secure key loading from environment/config.
3. Implement append-only audit logging for all write operations.
4. Add log masking for phone/NID patterns.

Done when:
- PII is encrypted at rest.
- Audit events are written for all CRUD mutations.

## 5. Property and Unit Module (Week 3)
1. Implement property CRUD with owner scoping.
2. Implement unit CRUD under property.
3. Implement property settings and utility charge config endpoints.
4. Frontend screens:
   - Property list/create/edit
   - Unit management
   - Settings and utility config

Done when:
- Manager/Owner permissions match PRD matrix.
- Unit counts and uniqueness rules are enforced.

## 6. Tenant Module (Week 3-4)
1. Implement tenant create/update/list/detail/soft-delete.
2. Implement NID upload/update/download endpoints (blob + MIME).
3. Implement tenant-unit history updates on assignment changes.
4. Enforce occupancy invariants in DB + service layer.
5. Build tenant UI forms with validation parity.

Done when:
- Active tenant cannot be assigned to occupied unit.
- Tenant writes rollback on NID upload failure.

## 7. Invoice Engine (Week 5-6)
1. Implement invoice generation service (single + bulk).
2. Add snapshot persistence for rent, charges, and rules used.
3. Implement due-date calculation and grace/late fee logic.
4. Implement invoice status lifecycle operations (`send`, `cancel`, `void`).
5. Add SMS text composition and storage.
6. Build invoice screens with list/filter/detail.

Done when:
- Duplicate generation is blocked with deterministic `409` behavior.
- Edge cases EC-01, EC-03, EC-04, EC-07, EC-13, EC-18 pass tests.

## 8. Payment Engine (Week 6)
1. Implement direct invoice payment endpoint.
2. Implement FIFO payment application endpoint.
3. Add overpayment protection and deterministic allocation logs.
4. Ensure status transitions update correctly (`PARTIALLY_PAID`, `PAID`, `OVERDUE`).

Done when:
- FIFO applies oldest outstanding first for all tested scenarios.
- EC-05 and EC-06 pass integration tests.

## 9. PDF and Export (Week 7)
1. Implement invoice PDF generation (`en`/`bn`) with embedded Bangla font.
2. Implement deposit refund PDF receipt.
3. Implement CSV export endpoints (UTF-8 BOM).
4. Implement async ZIP export job workflow and status polling.

Done when:
- PDF fallback behavior works if Bangla font is unavailable.
- Export jobs return stable `PENDING/RUNNING/COMPLETED/FAILED` statuses.

## 10. Expenses and Deposits (Week 7-8)
1. Implement expense CRUD + receipt blob endpoints.
2. Implement deposit transaction ledger and validation bounds.
3. Implement move-out/refund workflow hooks and receipt generation.

Done when:
- Expense/receipt transactions are atomic.
- Deposit balance cannot go negative after deductions/refunds.

## 11. Dashboard and Analytics (Week 8)
1. Implement summary, trend, property breakdown, overdue aging endpoints.
2. Add optimized aggregate queries and indexes.
3. Build dashboard UI charts and filters.

Done when:
- Dashboard load target (<2s with defined data profile) is met in staging.

## 12. Quality Hardening (Week 9-10)
1. Complete integration coverage for validation rules and EC-01..EC-18.
2. Execute security verification checklist (OWASP-oriented).
3. Run performance tests and optimize slow queries.
4. Complete WCAG AA checks on critical screens.
5. Finalize runbooks:
   - deployment
   - rollback
   - backup and restore
   - encryption key rotation

Done when:
- No open critical/high issues.
- Release checklist is signed by Eng/QA/Product.

## 13. Tracking Matrix (Use in Weekly Reviews)
Track each area with:
- `Not Started | In Progress | Blocked | Done`
- owner
- target date
- blockers
- exit evidence (test run, API doc, demo link)

Mandatory weekly checkpoints:
1. Invariant status (tenant occupancy, duplicate invoice, overpayment, deposit bounds)
2. Security status (encryption, auth, rate limits, audit coverage)
3. Performance status (API p95, dashboard, PDF/export timings)
