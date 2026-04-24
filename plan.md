# RentEase BD v1.0 Implementation Plan

Source of truth: `RentEaseBD_PRD_v1.1.md` (June 2025, v1.1.0 draft clarifications applied).
Execution companion: `implementation-steps.md` (task-level sequence and weekly checkpoints).

## 1. Objective
Ship a production-ready web application for Bangladeshi property owners/managers to manage properties, tenants, invoices, payments, expenses, deposits, exports, and reporting with strict data integrity and security controls.

## 2. Engineering Principles
- Protect invariants first: one active tenant per unit, one active invoice per tenant-month, payment totals cannot exceed dues, deposit refunds cannot exceed held balance.
- Prefer explicit, boring systems over cleverness: deterministic invoice snapshots, append-only audit, versioned REST contracts.
- Build for change without pretending to solve v2.0 now: clean boundaries for SMS intent handoff, tenant portal activation, and blob-storage migration.
- Every user-visible financial number must be reproducible from persisted data.

## 3. Scope Baseline (v1.0)
In scope:
- Auth/session management (Owner, Manager), JWT + DB refresh tokens with multi-device support.
- Property + unit management.
- Tenant lifecycle + NID upload/view + assignment history.
- Property-level settings + utility charge configs.
- Invoice generation (single + bulk), status lifecycle, late fee logic, PDF export (en/bn), SMS text composition.
- Payment recording (per-invoice + FIFO allocation).
- Expense tracking with receipt upload.
- Security deposit ledger + refund receipt.
- Dashboard analytics.
- CSV exports + async ZIP export jobs.
- Audit log and security hardening controls.

Out of scope:
- Mobile app, payment gateway, tenant portal activation, messaging gateway integration, multi-currency.

## 4. Delivery Phases

### Phase 0: Foundation and Guardrails
- Finalize repo structure (backend/frontend split), coding standards, CI, branch protections.
- Define ADR template and issue taxonomy (feature, risk, defect, migration).
- Provision environments: local/dev/staging.
- Bootstrap Flyway and base schema skeleton.

Exit criteria:
- CI green on lint + unit test smoke.
- Local stack bootable with MySQL and SQLite profile.
- `implementation-steps.md` sections 1-2 marked complete.

### Phase 1: Core Platform (Auth, Security, Base Models)
- Implement users, roles, refresh_tokens, audit_logs, crypto converters, timezone policy.
- Build auth endpoints (`login`, `refresh`, `logout`, `logout-all`, reset flows).
- Configure Spring Security, JWT RS256, rate limiting, CORS allowlist, log masking.
- Build frontend auth store, route guards, token refresh interceptor.

Exit criteria:
- Multi-device sessions verified.
- Sensitive fields encrypted at rest.
- Security integration tests pass.
- `implementation-steps.md` sections 3-4 marked complete.

### Phase 2: Property and Tenant Domain
- Implement properties, units, tenant CRUD, NID endpoints, tenant history.
- Enforce occupancy and assignment invariants with DB constraints + service checks.
- Implement soft-delete semantics and owner/manager permission boundaries.
- Build UI modules for property/unit and tenant management.

Exit criteria:
- Cannot assign active tenant to occupied unit.
- Tenant create/update transactions rollback on file failure.
- `implementation-steps.md` sections 5-6 marked complete.

### Phase 3: Billing Engine and Payments
- Implement property settings and utility charge config.
- Build invoice generation (single + bulk), invoice snapshots, duplicate protection.
- Implement due-date normalization (`min(due_day, last_day_of_month)`), late fee/grace logic.
- Implement payment endpoints (direct + FIFO), overpayment protection, status transitions.
- Build invoice UI + PDF preview + send workflow.

Exit criteria:
- Financial invariants covered by integration tests.
- Concurrent generate race resolved via DB uniqueness and 409 behavior.
- `implementation-steps.md` sections 7-8 marked complete.

### Phase 4: Expenses, Deposits, Dashboard, Exports
- Implement expenses + receipt storage/retrieval.
- Implement deposit transactions and refund receipt PDF.
- Implement dashboard summary/trend/breakdown/aging endpoints.
- Implement CSV exports and async ZIP export jobs.

Exit criteria:
- Large export streams without OOM.
- Dashboard p95 and chart load targets met in staging.
- `implementation-steps.md` sections 9-11 marked complete.

### Phase 5: Hardening and Release Readiness
- Complete audit log coverage for all write actions.
- Performance profiling, indexing pass, API p95 target checks.
- Accessibility pass (WCAG 2.1 AA for critical screens).
- Complete runbooks: backup/restore, key rotation, incident response.

Exit criteria:
- Release checklist signed (Eng, QA, Product).
- No open critical/high-severity defects.
- `implementation-steps.md` section 12 marked complete.

## 5. Work Breakdown Structure
- Backend
  - Domain models + migrations
  - API contracts + validation + error model
  - Security and cryptography
  - Billing and payment algorithms
  - Reporting/export services
- Frontend
  - Auth/session UX
  - CRUD screens and forms
  - Invoice generation/send/payment flows
  - Dashboard visualizations
  - Export workflows and job polling
- QA
  - Test matrix by feature and role
  - Integration tests for edge cases EC-01..EC-18
  - Performance and security tests
- DevOps
  - CI/CD, secrets, observability, backups

## 6. Quality Gates
- Unit tests: service/business logic >= 80% coverage.
- Integration tests: invoice math, FIFO payment, status transitions, blob upload/download, role controls.
- Contract tests: API schema compatibility under `/api/v1`.
- Security tests: auth abuse, rate limit behavior, CORS policy, encrypted field persistence.
- Regression suite required before each release candidate.

## 7. High-Risk Areas and Mitigations
- Financial correctness drift
  - Mitigation: immutable invoice snapshots, deterministic calculators, golden test vectors.
- Concurrency collisions on invoice generation
  - Mitigation: DB unique constraints + transactional retries + explicit 409 semantics.
- Blob growth in DB
  - Mitigation: payload limits, monitoring, retention policy, capacity alerts.
- Bangla PDF rendering failures
  - Mitigation: startup font health check + fallback header behavior + PDF integration tests.
- Security misconfiguration
  - Mitigation: hardened defaults, penetration checklist, automated config validation.

## 8. Milestone Timeline (Suggested)
- Weeks 1-2: Phase 0-1
- Weeks 3-5: Phase 2
- Weeks 6-8: Phase 3
- Weeks 9-10: Phase 4
- Weeks 11-12: Phase 5 + UAT + go-live

## 9. Definition of Done (System)
- All in-scope endpoints implemented and documented.
- PRD validation rules and edge cases enforced and tested.
- p95 performance targets met in staging for representative data.
- Auditability complete for all mutation paths.
- Deployment, rollback, backup, and recovery procedures verified.

## 10. Implementation Order (Mandatory)
1. Foundation, migrations, and security baseline before feature modules.
2. Property/unit/tenant domain before invoice generation.
3. Invoice generation before payment distribution (FIFO).
4. Payment correctness before dashboard analytics.
5. Reporting/export after transactional features stabilize.
6. Hardening and observability before go-live signoff.

## 11. Weekly Control Points
1. Invariant health: occupancy, duplicate invoice prevention, overpayment prevention, deposit bounds.
2. Security health: encryption coverage, auth/session behavior, rate-limits, audit completeness.
3. Performance health: API p95, dashboard response, PDF generation, export first-byte latency.
