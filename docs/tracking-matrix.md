# RentEase BD Tracking Matrix

Snapshot date: 2026-03-06

Use this matrix in weekly reviews. Update `Status`, `Owner`, `Target Date`, `Blockers`, and `Exit Evidence` for each area.

| Area | Status | Owner | Target Date | Blockers | Exit Evidence |
|---|---|---|---|---|---|
| Delivery Setup | Done | Engineering | 2026-03-05 | None | Monorepo structure, CI scripts, local boot path |
| DB + Migrations | In Progress | Engineering | 2026-03-10 | Advanced index/invariant migration gaps | `V1__init_schema.sql`, migration boot tests |
| Security + Auth | In Progress | Engineering | 2026-03-12 | Rate-limit/session listing hardening pending | Auth API tests, JWT+refresh flow, CORS fix for localhost ports |
| Crypto + Audit | In Progress | Engineering | 2026-03-13 | Full mutation coverage and masking policy pending | AES converter, auth/property/tenant/invoice audit writes |
| Property + Unit | Done | Engineering | 2026-03-05 | None | Backend CRUD APIs + frontend modules |
| Tenant Module | In Progress | Engineering | 2026-03-08 | DB-level occupancy constraint hardening pending | Tenant create/list/NID endpoints + onboarding UI |
| Invoice Engine | Done | Engineering | 2026-03-05 | None | Single/bulk generation + lifecycle + PDF + UX |
| Payment Engine | Done | Engineering | 2026-03-05 | None | Direct/FIFO APIs, overpayment guard, tests |
| PDF + Export | Done | Engineering | 2026-03-06 | None | Invoice/deposit PDFs + CSV + async ZIP jobs |
| Expenses + Deposits | Done (MVP) | Engineering | 2026-03-06 | Reconciliation UX enhancements | Expense CRUD/receipt blobs + deposit ledger/settlement APIs + UI |
| Dashboard + Analytics | Done (MVP) | Engineering | 2026-03-06 | Perf-tuning/indexing for large datasets | Summary/trend/breakdown/aging APIs + live dashboard UI |
| Quality Hardening | In Progress | Engineering + QA | 2026-03-20 | Perf/security/WCAG execution in staging | Added integration tests + runbooks baseline |

## Mandatory Weekly Checkpoints

1. Invariant status
- Occupancy exclusivity (`unit` + active tenant).
- Duplicate invoice prevention for same tenant-month.
- Overpayment and deposit-bound protection.

2. Security status
- JWT/refresh behavior and revocation paths.
- Encryption-at-rest coverage.
- Audit coverage of write paths.

3. Performance status
- API p95 for key reads/writes.
- Dashboard response under staging data profile.
- PDF/export latency and failure rates.

## Review Workflow

1. Update this matrix before weekly review.
2. Attach evidence links for each `In Progress` or `Done` row.
3. Convert unresolved blockers into prioritized engineering tasks.
