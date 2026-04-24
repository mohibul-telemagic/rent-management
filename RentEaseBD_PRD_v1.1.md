# PRODUCT REQUIREMENTS DOCUMENT
## Rent Management Web Application
### Targeted for Bangladeshi Property Owners

---

| Attribute | Detail |
|---|---|
| **Product Name** | RentEase BD *(working title)* |
| **Version** | 1.1.0 — Draft (Clarifications Applied) |
| **Date** | June 2025 |
| **Release Scope** | Web Application v1.0 (Mobile deferred) |
| **Primary Users** | Property Owners / Property Managers (Bangladesh) |
| **Currency** | Bangladeshi Taka (BDT ৳) — always; no multi-currency |
| **Auth Model** | Role-based; Owner + Manager in v1.0; Tenant login in v1.1 |
| **Backend Stack** | Spring Boot 3.x (Java 21, LTS) |
| **Frontend Stack** | Vue 3 (Composition API, Vite) |
| **Database** | MySQL 8.x (primary); SQLite supported for local/dev |
| **Integration Surface** | RESTful API; SMS via Android/iOS native intent (no gateway) |
| **Compliance** | Bangladesh Data Protection best practices; PII encryption at rest |
| **Prepared by** | Senior Product Manager & Principal Software Architect |
| **Audience** | AI Coding Agents, Engineering Teams, QA, Stakeholders |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Goals and Non-Goals](#2-goals-and-non-goals)
3. [Tech Stack & Architecture](#3-tech-stack--architecture)
4. [User Personas](#4-user-personas)
5. [Feature Specifications](#5-feature-specifications)
6. [User Flows](#6-user-flows)
7. [Data Model](#7-data-model)
8. [API Design Overview](#8-api-design-overview)
9. [Validation Rules](#9-validation-rules)
10. [Security Considerations](#10-security-considerations)
11. [Edge Cases](#11-edge-cases)
12. [Non-Functional Requirements](#12-non-functional-requirements)
13. [Future Enhancements](#13-future-enhancements)
14. [Resolved Clarifications](#14-resolved-clarifications)

---

## 1. Executive Summary

This document specifies the complete product requirements for a **Rent Management Web Application** tailored for property owners operating in Bangladesh. The application enables property owners and their designated managers to digitally manage tenants, properties, billing, expenses, and financial reporting — replacing paper-based and spreadsheet workflows common in the local market.

The initial release targets **web browsers only**, built with **Spring Boot 3.x** on the backend and **Vue 3** on the frontend. A companion mobile application is explicitly out of scope for this release, though all backend APIs must be designed with mobile compatibility in mind.

The messaging subsystem generates pre-composed SMS text. When the mobile app is developed, it will trigger the device's **native Android/iOS SMS intent** using that text — no third-party SMS gateway is required or integrated in any version.

All files (NID images, receipt images) are stored as **BLOBs or Base64-encoded strings in MySQL**. No object storage (S3 or equivalent) is used.

**Localization:** Bangladeshi Taka (BDT ৳) is the only currency. Timezone is Bangladesh Standard Time (BST = UTC+6). NID validation defaults to 17-digit Smart NID format. PDF/CSV export supports **English or Bangla** as a user-selectable option.

---

## 2. Goals and Non-Goals

### 2.1 Goals

- Enable property owners and managers to manage tenants, properties, billing, and expenses via a secure web interface.
- Automate monthly invoice generation with configurable rent, utility charges, and late-fee line items.
- Allow owners/managers to manually update invoice payment status after receiving rent in-hand.
- Provide a real-time financial dashboard with income, expense, and net-income visibility.
- Generate server-side PDF invoices downloadable in **English or Bangla**.
- Compose a pre-formatted SMS text payload that a future mobile app will pass to the device native SMS intent.
- Track security deposits and generate refund receipts on tenant move-out.
- Allow owners to export all data (tenants, invoices, expenses) as CSV.
- Store NID images and receipt files as BLOBs/Base64 in MySQL — no external file storage.
- Design all APIs RESTful and versioned (`/api/v1/`) for future mobile app consumption.
- Support simultaneous login from multiple devices per user account.

### 2.2 Non-Goals (v1.0)

- Mobile application (iOS / Android) — deferred to v2.0.
- Third-party SMS gateway or WhatsApp integration — system only composes text; device native intent handles delivery.
- Tenant self-service portal — tenant login deferred to v1.1.
- Online payment gateway (bKash, Nagad, card) — deferred.
- Multi-language UI toggle (Bangla/English runtime switch) — deferred to v1.1; only PDF/CSV export has language option in v1.0.
- Multi-currency support — all monetary values are BDT only.
- Co-ownership (multiple owners per property) — architecture must support it but implementation deferred.
- Automated bank reconciliation.
- Tax filing or VAT reporting.

---

## 3. Tech Stack & Architecture

### 3.1 Backend — Spring Boot 3.x

| Component | Choice | Notes |
|---|---|---|
| **Framework** | Spring Boot 3.3.x (latest stable) | Java 21 (LTS); virtual threads enabled via Project Loom |
| **Language** | Java 21 | Record types for DTOs; sealed interfaces for domain events |
| **API Layer** | Spring Web MVC (REST) | `@RestController`; versioned under `/api/v1/` |
| **Security** | Spring Security 6.x + JJWT | JWT access tokens (24h) + refresh tokens (30d, stored in DB) |
| **ORM** | Spring Data JPA + Hibernate 6.x | Entities, repositories, JPQL for complex queries |
| **Database** | MySQL 8.x (prod) / SQLite (dev/test via `sqlite-dialect`) | Flyway for schema migrations |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) | `@Valid` on all request bodies |
| **PDF Generation** | Apache PDFBox or iText 7 (community) | Server-side; Bangla font via embedded TTF (e.g., Kalpurush) |
| **CSV Export** | OpenCSV or Apache Commons CSV | Streamed response for large exports |
| **Bangla Text** | ICU4J for Bangla text shaping in PDFs | Required for correct Bangla rendering |
| **Audit** | Spring Data Envers or custom `AuditLog` entity | All mutations logged |
| **Build** | Maven or Gradle (team choice) | Docker-compatible |
| **Testing** | JUnit 5 + Mockito + Spring Boot Test + Testcontainers (MySQL) | Min 80% business logic coverage |

### 3.2 Frontend — Vue 3

| Component | Choice | Notes |
|---|---|---|
| **Framework** | Vue 3.4.x (Composition API) | `<script setup>` syntax throughout |
| **Build Tool** | Vite 5.x | Fast HMR; env-based config |
| **State Management** | Pinia | Stores: auth, tenants, properties, invoices, expenses, dashboard |
| **Routing** | Vue Router 4.x | Route guards for auth; lazy-loaded route chunks |
| **HTTP Client** | Axios | Interceptors for JWT attach and 401 refresh flow |
| **UI Component Library** | Naive UI or PrimeVue | BDT-aware number formatting; Bangla font support |
| **Forms** | VeeValidate + Yup | Client-side validation mirroring backend rules |
| **Charts** | Chart.js via vue-chartjs | Income/expense bar chart; occupancy line chart |
| **PDF Preview** | Vue PDF Embed (pdf.js wrapper) | Preview generated PDF in modal before download |
| **i18n** | Vue I18n 9.x | Installed from v1.0 but only `en` locale active; `bn` locale added in v1.1 |
| **File Handling** | Native `<input type="file">` + FileReader API | NID image and receipt upload; Base64 encoded before POST |
| **Testing** | Vitest + Vue Test Utils | Unit tests for composables and components |

### 3.3 Database Schema Strategy

- **Primary database:** MySQL 8.x in production.
- **Dev/test:** SQLite via Hibernate `SQLiteDialect` (community). All Flyway migrations must be compatible with both dialects (avoid MySQL-specific functions in migrations; use JPQL/HQL in queries).
- **File storage:** NID images stored as `MEDIUMBLOB` (up to 16MB). Receipt images stored as `MEDIUMBLOB`. Retrieve via dedicated endpoints returning `application/octet-stream` or `image/*`.
- **No external object storage.** No S3, no MinIO.

### 3.4 High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Browser (Vue 3 SPA)                   │
│  Vue Router │ Pinia │ Axios │ Chart.js │ Vue I18n       │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTPS / REST (JSON)
                        ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot 3.x API Server                  │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ Auth     │  │ Tenant   │  │ Invoice  │  ...         │
│  │ Controller│  │ Service  │  │ Service  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│                                                          │
│  Spring Security │ JPA/Hibernate │ Flyway │ PDFBox      │
└───────────────────────┬─────────────────────────────────┘
                        │ JDBC
                        ▼
┌─────────────────────────────────────────────────────────┐
│           MySQL 8.x (prod) / SQLite (dev)                │
│   Tables: users, properties, property_units, tenants,    │
│   tenant_unit_history, invoices, invoice_payments,       │
│   expenses, owner_settings, property_settings,           │
│   utility_charge_configs, audit_logs, refresh_tokens     │
└─────────────────────────────────────────────────────────┘
```

---

## 4. User Personas

### 4.1 Primary Persona — Residential Property Owner

| Attribute | Detail |
|---|---|
| **Name (illustrative)** | Rahim Chowdhury |
| **Age** | 45–65 |
| **Location** | Dhaka, Chittagong, or Sylhet metropolitan areas |
| **Properties Owned** | 1–5 residential buildings (5–50 units total) |
| **Technical Literacy** | Moderate — comfortable with smartphones, WhatsApp, and banking apps |
| **Primary Pain Points** | Tracking rent manually; chasing overdue tenants; no financial overview |
| **Key Needs** | Invoice generation; overdue alerts; PDF receipts in Bangla or English; expense and deposit tracking |
| **Device** | Desktop PC or laptop; occasional mobile browser |

### 4.2 Secondary Persona — Property Manager

| Attribute | Detail |
|---|---|
| **Name (illustrative)** | Nasrin Akter |
| **Role** | Hired manager acting on behalf of the owner |
| **Access Level** | Manager role — can manage tenants, generate invoices, log expenses; cannot access financial reports or user management |
| **Key Needs** | Day-to-day operations without financial visibility |

### 4.3 Future Persona — Tenant (v1.1)

| Attribute | Detail |
|---|---|
| **Access** | Read-only portal to view their own invoices and payment status |
| **Note** | Auth system and tenant `user` account linking must be architected in v1.0 even though tenant login is inactive |

---

## 5. Feature Specifications

### 5.1 Authentication & Authorization

JWT-based authentication. All endpoints require a valid Bearer token. Access tokens expire after **24 hours**. Refresh tokens expire after **30 days**, stored in the `refresh_tokens` DB table (not in-memory), supporting **multiple concurrent device sessions per user**.

#### 5.1.1 Role Permissions Matrix

| Capability | Owner | Manager | Tenant (v1.1) |
|---|---|---|---|
| Login / Logout | YES | YES | YES |
| Tenant CRUD | YES | YES | NO |
| Property CRUD | YES | Read Only | NO |
| Invoice Generation | YES | YES | NO |
| Invoice Status Update (mark paid) | YES | YES | NO |
| View Own Invoices | YES | YES | YES (own only) |
| Financial Dashboard | YES | NO | NO |
| Expense Management | YES | YES | NO |
| Utility Charge Config | YES | NO | NO |
| Property Settings (late fee, etc.) | YES | NO | NO |
| CSV / PDF Export | YES | YES (limited) | NO |
| Audit Log View | YES | NO | NO |
| User Management | YES | NO | NO |
| Security Deposit Management | YES | YES | NO |

#### 5.1.2 Auth Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Accepts `email` + `password`. Returns `access_token` (JWT, 24h) + `refresh_token` (opaque UUID, 30d). Supports multiple active sessions. |
| POST | `/api/v1/auth/refresh` | Exchanges `refresh_token` for new `access_token`. Does **not** invalidate the refresh token (multi-device support). |
| POST | `/api/v1/auth/logout` | Invalidates the specific `refresh_token` passed in the request body (only current device session is logged out). |
| POST | `/api/v1/auth/logout-all` | Invalidates **all** `refresh_tokens` for the authenticated user (logout from all devices). |
| POST | `/api/v1/auth/forgot-password` | Sends OTP to registered email. |
| POST | `/api/v1/auth/reset-password` | Validates OTP, sets new password, invalidates all refresh tokens for that user. |

**Password rules:** Minimum 8 characters, at least 1 uppercase, 1 digit, 1 special character (`!@#$%^&*`). Stored as bcrypt hash (cost factor 12).

**`refresh_tokens` table:**

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | FK → `users.id` |
| `token_hash` | VARCHAR(64) | SHA-256 of the opaque token |
| `device_hint` | VARCHAR(200) | User-Agent substring, for display in "active sessions" list |
| `expires_at` | TIMESTAMPTZ | 30 days from creation |
| `created_at` | TIMESTAMPTZ | |

---

### 5.2 Tenant Management

Full CRUD on tenant records. Each tenant is mapped to exactly **one unit** at any given time. Historical associations preserved in `tenant_unit_history`.

#### 5.2.1 Tenant Entity — Exact Fields

| Field | Type | Required | Validation / Constraint | Notes |
|---|---|---|---|---|
| `id` | BIGINT (auto-increment) | System | Auto | PK; use BIGINT not UUID for MySQL performance |
| `full_name` | VARCHAR(120) | YES | Min 2 chars; letters, spaces, hyphens, dots only | |
| `phone_primary` | VARCHAR(20) | YES | Regex: `^\+8801[3-9]\d{8}$` (SIM numbers only) | Encrypted at rest (AES-256) |
| `phone_secondary` | VARCHAR(20) | NO | Same regex if provided | Encrypted at rest |
| `nid_number` | VARCHAR(17) | YES | Exactly 17 numeric digits: `^\d{17}$` | Encrypted at rest |
| `nid_image` | MEDIUMBLOB | YES | JPEG/PNG; max 5MB; stored as binary in DB | No OCR/extraction; owner uploads manually |
| `nid_image_mime_type` | VARCHAR(20) | YES | `image/jpeg` or `image/png` | |
| `date_of_birth` | DATE | NO | Must be in past; age >= 18 | |
| `permanent_address` | TEXT | YES | Min 10 chars | |
| `current_address` | TEXT | NO | Min 10 chars if provided | Defaults to `permanent_address` if blank |
| `emergency_contact_name` | VARCHAR(120) | YES | Min 2 chars | |
| `emergency_contact_phone` | VARCHAR(20) | YES | Same regex as `phone_primary` | Encrypted at rest |
| `emergency_contact_relation` | VARCHAR(50) | YES | Father / Mother / Spouse / Sibling / Friend / Other | |
| `lease_start_date` | DATE | YES | Must be <= `lease_end_date` if provided | |
| `lease_end_date` | DATE | NO | Must be > `lease_start_date`; NULL = month-to-month | |
| `monthly_rent_bdt` | DECIMAL(12,2) | YES | > 0; max 9,999,999.99 | Snapshot taken at invoice generation |
| `security_deposit_bdt` | DECIMAL(12,2) | YES | >= 0 | |
| `security_deposit_status` | ENUM | System | `HELD` \| `PARTIALLY_REFUNDED` \| `REFUNDED` | Default: HELD |
| `status` | ENUM | YES | `ACTIVE` \| `INACTIVE` \| `EVICTED` | Default: ACTIVE |
| `notes` | TEXT | NO | Max 1000 chars | |
| `property_unit_id` | BIGINT | YES | FK → `property_units.id` | Enforces 1 active tenant per unit |
| `user_id` | BIGINT | NO | FK → `users.id`; NULL until tenant login enabled in v1.1 | Link for future tenant portal |
| `created_by` | BIGINT | System | FK → `users.id` | |
| `created_at` | DATETIME | System | Auto; stored in BST (UTC+6) | |
| `updated_at` | DATETIME | System | Auto on update | |

#### 5.2.2 Tenant CRUD Operations

| Operation | Endpoint | Behaviour / Rules |
|---|---|---|
| List Tenants | `GET /api/v1/tenants` | Paginated (`page`, `size`). Filter by `status`, `propertyId`. Returns masked NID (last 4 digits). NID image not included in list. |
| Get Tenant | `GET /api/v1/tenants/:id` | Full record. Encrypted fields decrypted for Owner/Manager. NID image endpoint is separate. |
| Get NID Image | `GET /api/v1/tenants/:id/nid-image` | Returns binary image with correct `Content-Type`. Owner/Manager only. Audit logged. |
| Create Tenant | `POST /api/v1/tenants` | Multipart form-data (fields + NID image file). Validates unit is VACANT. Creates `tenant_unit_history`. Audit logged. |
| Update Tenant | `PUT /api/v1/tenants/:id` | Full replace. `PATCH` for partial. Changing unit closes old history, opens new. Audit logged. |
| Update NID Image | `PUT /api/v1/tenants/:id/nid-image` | Replaces stored NID image. Multipart. Audit logged. |
| Deactivate Tenant | `DELETE /api/v1/tenants/:id` | Soft delete — sets `status=INACTIVE`. Hard delete prohibited. Audit logged. |
| Tenant History | `GET /api/v1/tenants/:id/history` | All unit assignments with date ranges. |

---

### 5.3 Property Management

Properties are top-level entities containing one or more units. Tenants are assigned to **units**, not properties directly.

#### 5.3.1 Property Entity — Exact Fields

| Field | Type | Required | Validation | Notes |
|---|---|---|---|---|
| `id` | BIGINT | System | Auto | PK |
| `property_name` | VARCHAR(150) | YES | Min 3 chars; unique per owner | |
| `address_line1` | VARCHAR(200) | YES | Min 5 chars | |
| `address_line2` | VARCHAR(200) | NO | | |
| `thana` | VARCHAR(100) | YES | Sub-district | Dropdown from BD thana list |
| `district` | VARCHAR(100) | YES | From 64 Bangladesh districts enum | |
| `division` | VARCHAR(50) | YES | From 8 BD divisions enum | |
| `property_type` | ENUM | YES | `RESIDENTIAL_FLAT` \| `RESIDENTIAL_BUILDING` \| `COMMERCIAL` \| `MIXED_USE` \| `LAND` | |
| `total_units` | INTEGER | YES | >= 1; must match count of child `property_units` | Validated on create/update |
| `owner_notes` | TEXT | NO | Max 2000 chars | |
| `owner_id` | BIGINT | System | FK → `users.id` | Set from auth token |
| `created_at` | DATETIME | System | Auto BST | |
| `updated_at` | DATETIME | System | Auto BST | |

#### 5.3.2 Property Unit Entity — Exact Fields

| Field | Type | Required | Validation | Notes |
|---|---|---|---|---|
| `id` | BIGINT | System | Auto | PK |
| `property_id` | BIGINT | YES | FK → `properties.id` | |
| `unit_identifier` | VARCHAR(20) | YES | E.g., "3B", "GF-01". Unique within property. | |
| `floor_number` | INTEGER | NO | Can be negative (basement) | |
| `area_sqft` | DECIMAL(8,2) | NO | > 0 if provided | |
| `unit_type` | ENUM | NO | `1BHK` \| `2BHK` \| `3BHK` \| `STUDIO` \| `SHOP` \| `OFFICE` \| `OTHER` | |
| `occupancy_status` | ENUM | System | `VACANT` \| `OCCUPIED` \| `UNDER_MAINTENANCE` | Derived from active tenant |
| `notes` | TEXT | NO | Max 500 chars | |

---

### 5.4 Property-Level Settings

Each property has its own settings record (`property_settings`). This replaces a global `owner_settings` for all financial configuration, since different properties may have different rules.

#### 5.4.1 `property_settings` Entity

| Field | Type | Default | Notes |
|---|---|---|---|
| `id` | BIGINT | Auto | PK |
| `property_id` | BIGINT | FK | 1:1 with `properties` |
| `invoice_due_day_of_month` | INTEGER | `7` | Day invoices are due each month (1–28) |
| `late_fee_grace_days` | INTEGER | `0` | Days after due date before late fee applies |
| `late_fee_type` | ENUM | `FLAT` | `FLAT` \| `PERCENTAGE` |
| `late_fee_flat_bdt` | DECIMAL(10,2) | `0.00` | Used if `late_fee_type=FLAT` |
| `late_fee_percentage` | DECIMAL(5,2) | `0.00` | Used if `late_fee_type=PERCENTAGE` |
| `default_tax_percentage` | DECIMAL(5,2) | `0.00` | Applied to invoices unless overridden |
| `invoice_footer_text_en` | TEXT | `Please pay by the due date.` | English footer on PDF |
| `invoice_footer_text_bn` | TEXT | `অনুগ্রহ করে নির্ধারিত তারিখের মধ্যে পরিশোধ করুন।` | Bangla footer on PDF |
| `updated_at` | DATETIME | | |

---

### 5.5 Utility Charge Configuration

Owners can configure which utility charges (gas, electricity, water, service charge, etc.) appear on invoices for each property. Each charge can be enabled or disabled per property. When generating an invoice, the system includes all **enabled** charges as line items; the owner enters the amount for that billing period.

#### 5.5.1 `utility_charge_configs` Entity

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | BIGINT | System | PK |
| `property_id` | BIGINT | YES | FK → `properties.id` |
| `charge_key` | VARCHAR(50) | YES | Machine key: `GAS` \| `ELECTRICITY` \| `WATER` \| `SERVICE_CHARGE` \| `INTERNET` \| `CLEANING` \| `OTHER` |
| `label_en` | VARCHAR(100) | YES | Display label in English. E.g., "Gas Bill" |
| `label_bn` | VARCHAR(100) | YES | Display label in Bangla. E.g., "গ্যাস বিল" |
| `is_enabled` | BOOLEAN | YES | Default `TRUE`. When `FALSE`, this charge is hidden from invoice generation UI. |
| `default_amount_bdt` | DECIMAL(10,2) | NO | Pre-fill value in invoice generation form; owner can override per invoice. |
| `sort_order` | INTEGER | YES | Display order on invoice. |

> **At invoice generation time:** The system loads all `is_enabled=TRUE` utility charge configs for the property and presents them as editable line items. The owner enters the actual amount for that month. Items with zero amount are excluded from the PDF.

---

### 5.6 Billing & Invoice

Invoices are generated on a **monthly cycle**. The system calculates total due from: base rent + utility charges (enabled, amount entered by owner) + late fee − discount. Invoices are **immutable once marked PAID**. Edits to UNPAID invoices increment the `version` and append an audit entry.

**Payment status is updated manually by the owner or manager** after receiving rent in hand. When tenant login is enabled in v1.1, tenants will see the updated status in their portal — they cannot change it.

#### 5.6.1 Invoice Entity — Exact Fields

| Field | Type | Required | Constraint | Notes |
|---|---|---|---|---|
| `id` | BIGINT | System | Auto | PK |
| `invoice_number` | VARCHAR(30) | System | Format: `INV-{YYYY}-{MM}-{SEQ5}` e.g. `INV-2025-06-00042` | Sequential per owner per month |
| `tenant_id` | BIGINT | YES | FK → `tenants.id` | |
| `property_unit_id` | BIGINT | YES | FK → `property_units.id` | Denormalized for history |
| `property_id` | BIGINT | YES | FK → `properties.id` | Denormalized |
| `billing_period_start` | DATE | YES | First day of billing month | |
| `billing_period_end` | DATE | YES | Last day of billing month | |
| `due_date` | DATE | YES | `billing_period_start` + `property_settings.invoice_due_day_of_month` days | |
| `base_rent_bdt` | DECIMAL(12,2) | YES | Snapshot of `tenant.monthly_rent_bdt` at generation time | Immutable after creation |
| `utility_charges` | JSON | NO | Array of `{charge_key, label_en, label_bn, amount_bdt}`. Only enabled charges with amount > 0. | Snapshot at generation time |
| `utility_charges_total_bdt` | DECIMAL(12,2) | System | Sum of `utility_charges[].amount_bdt` | |
| `late_fee_bdt` | DECIMAL(12,2) | System | >= 0. Applied per `property_settings` rules. | |
| `late_fee_applicable` | BOOLEAN | System | TRUE if `current_date > due_date` at generation | |
| `discount_bdt` | DECIMAL(12,2) | NO | >= 0; requires `discount_reason` | |
| `discount_reason` | VARCHAR(200) | NO | Required if `discount_bdt > 0` | |
| `subtotal_bdt` | DECIMAL(12,2) | System | `base_rent + utility_charges_total + late_fee − discount` | |
| `tax_percentage` | DECIMAL(5,2) | NO | From `property_settings.default_tax_percentage`; overridable | |
| `tax_amount_bdt` | DECIMAL(12,2) | System | `subtotal × tax_percentage / 100` | |
| `total_due_bdt` | DECIMAL(12,2) | System | `subtotal + tax_amount` | |
| `status` | ENUM | System | `DRAFT` \| `SENT` \| `PAID` \| `PARTIALLY_PAID` \| `OVERDUE` \| `CANCELLED` \| `VOID` | Default: DRAFT |
| `amount_paid_bdt` | DECIMAL(12,2) | System | Sum of all `invoice_payments.amount_bdt` for this invoice | |
| `balance_due_bdt` | DECIMAL(12,2) | System | `total_due − amount_paid` | |
| `sms_text` | TEXT | System | Pre-composed SMS text in owner's selected language. Not sent by server. | Mobile app passes this to native SMS intent |
| `export_language` | ENUM | NO | `EN` \| `BN` | Controls PDF/SMS text language; default from user preference |
| `version` | INTEGER | System | Starts at 1; increments on edit | |
| `notes` | TEXT | NO | Max 500 chars | |
| `generated_by` | BIGINT | System | FK → `users.id` | |
| `created_at` | DATETIME | System | Auto BST | |
| `updated_at` | DATETIME | System | Auto BST | |

#### 5.6.2 Late Fee Calculation Rules

| Rule | Logic |
|---|---|
| Grace Period | `property_settings.late_fee_grace_days` (default: 0). |
| Late Fee Type | `property_settings.late_fee_type`: `FLAT` or `PERCENTAGE`. |
| Flat Late Fee | `property_settings.late_fee_flat_bdt` applied once after grace period. |
| Percentage Late Fee | `base_rent_bdt × property_settings.late_fee_percentage / 100`. Applied once. |
| Trigger Condition | `late_fee_bdt` added if: `current_date > due_date + grace_days`. Also applied retroactively if `payment_date > due_date + grace_days`. |
| Maximum Late Fee | Cannot exceed `base_rent_bdt`. Validated at calculation. |
| Manual Override | Owner can override `late_fee_bdt` with a reason (min 10 chars); logged to audit. |
| Rounding | `ROUND_HALF_UP` to 2 decimal places. |

#### 5.6.3 Partial Payment — FIFO Application Rule

When a tenant has **multiple outstanding invoices** and a payment is received:

1. Payments are applied to the **oldest unpaid invoice first** (FIFO by `billing_period_start` ASC).
2. If the payment covers the oldest invoice and has remainder, the remainder is applied to the next oldest.
3. The `POST /api/v1/invoices/payments/apply` endpoint accepts `{tenant_id, total_amount_bdt, payment_date, payment_method, payment_reference}` and automatically distributes across invoices in FIFO order.
4. Alternatively, `POST /api/v1/invoices/:id/payments` applies a payment to a **specific invoice only** — the owner must manage allocation manually.
5. The API response for the FIFO endpoint returns an array showing how the payment was distributed: `[{invoice_id, invoice_number, applied_amount_bdt, remaining_balance_bdt, new_status}]`.

#### 5.6.4 Invoice Status Lifecycle

```
DRAFT → SENT → PAID
              ↘ PARTIALLY_PAID → PAID
DRAFT → CANCELLED
SENT  → CANCELLED
SENT  → OVERDUE (system job; when due_date < today and balance_due > 0)
PAID  → [immutable; no status change permitted]
Any   → VOID (only by Owner; requires reason; creates audit entry)
```

**Status change rules:**
- `DRAFT → SENT`: Owner/Manager clicks "Send to Tenant". System composes SMS text, sets `sms_text` field.
- `SENT → PAID` or `PARTIALLY_PAID`: Owner/Manager clicks "Record Payment" and enters amount received.
- `OVERDUE`: Set by a scheduled background job (Spring `@Scheduled`) that runs daily at **23:59 BST**, marking all invoices where `due_date < today AND balance_due > 0 AND status = SENT`.
- Tenant (v1.1) views invoice status read-only; cannot change it.

#### 5.6.5 SMS Text Composition

The server composes a pre-formatted SMS string stored in `invoices.sms_text`. The mobile app (v2.0) will read this string and pass it to the Android `ACTION_SENDTO` intent or iOS `MFMessageComposeViewController`.

**English template:**
```
Dear {tenant_name}, your rent invoice {invoice_number} for {property_name} Unit {unit_id}
is ৳{total_due_bdt} due on {due_date}.
Breakdown: Rent ৳{base_rent} | Utilities ৳{utilities_total} | Late Fee ৳{late_fee}.
Please pay on time. -{owner_name}
```

**Bangla template:**
```
প্রিয় {tenant_name}, {property_name} ইউনিট {unit_id}-এর ভাড়া বিল {invoice_number}
মোট ৳{total_due_bdt}, পরিশোধের তারিখ {due_date}।
বিবরণ: ভাড়া ৳{base_rent} | ইউটিলিটি ৳{utilities_total} | বিলম্ব ফি ৳{late_fee}।
সময়মতো পরিশোধ করুন। -{owner_name}
```

> **Note:** SMS text is generated at invoice `SENT` time and stored. It is **never transmitted by the server**. The mobile team retrieves it via `GET /api/v1/invoices/:id` and passes it to the device SMS intent.

#### 5.6.6 Invoice Operations

| Operation | Endpoint | Rules |
|---|---|---|
| Generate Invoice | `POST /api/v1/invoices` | Body: `{tenantId, billingPeriodStart, utilityCharges[], discountBdt?, discountReason?, exportLanguage?}`. System calculates all monetary fields. Prevents duplicate for same `tenant_id + billing_period_start`. |
| Generate Bulk | `POST /api/v1/invoices/bulk-generate` | Body: `{propertyId, billingPeriodStart, exportLanguage?}`. Generates for all ACTIVE tenants. Returns per-tenant success/failure. |
| Get Invoice | `GET /api/v1/invoices/:id` | Full detail including `utility_charges` JSON, audit history, `sms_text`. |
| List Invoices | `GET /api/v1/invoices` | Filter by `tenantId`, `propertyId`, `status`, `billingMonth` (YYYY-MM), `dateRange`. Paginated. |
| Update Invoice | `PUT /api/v1/invoices/:id` | Allowed if `status IN (DRAFT, SENT)`. Increments `version`. Blocked if `PAID` or `VOID`. |
| Record Payment (specific) | `POST /api/v1/invoices/:id/payments` | Body: `{amountBdt, paymentDate, paymentMethod, paymentReference}`. Applied to this invoice only. |
| Apply Payment (FIFO) | `POST /api/v1/invoices/payments/apply` | Body: `{tenantId, totalAmountBdt, paymentDate, paymentMethod, paymentReference}`. Distributes FIFO across oldest outstanding invoices. |
| Mark as Sent | `POST /api/v1/invoices/:id/send` | Sets `status=SENT`. Composes and stores `sms_text`. Idempotent. |
| Cancel Invoice | `POST /api/v1/invoices/:id/cancel` | `status IN (DRAFT, SENT)` only. Requires `reason`. |
| Void Invoice | `POST /api/v1/invoices/:id/void` | Owner only. Any status except `VOID`. Requires `reason`. Audit logged. |
| Download PDF | `GET /api/v1/invoices/:id/pdf?lang=en\|bn` | Server generates PDF on demand in requested language. Returns `application/pdf`. |

#### 5.6.7 PDF Invoice Contents

| Section | Content |
|---|---|
| Header | System logo + "Tax Invoice" / "কর চালান" (based on `lang` param) |
| Meta | Invoice number, billing period, generated date, due date |
| Property | Property name, address, unit identifier |
| Tenant | Tenant name; NID masked (last 4 digits only) |
| Line Items | Base Rent \| Each utility charge (label + amount) \| Late Fee \| Discount \| Subtotal \| Tax \| **Total Due** |
| Payment Status | Current status badge; amounts paid to date; balance due |
| Footer | `invoice_footer_text_en` or `invoice_footer_text_bn` from `property_settings` |
| Bangla Font | Embedded Kalpurush or SolaimanLipi TTF; shaped via ICU4J |

---

### 5.7 Security Deposit Management

Security deposits are tracked per tenant. When a tenant vacates, the owner records the refund (full or partial) and the system generates a **Security Deposit Refund Receipt**.

#### 5.7.1 `security_deposit_transactions` Entity

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | BIGINT | System | PK |
| `tenant_id` | BIGINT | YES | FK → `tenants.id` |
| `transaction_type` | ENUM | YES | `COLLECTED` \| `REFUNDED` \| `DEDUCTED` |
| `amount_bdt` | DECIMAL(12,2) | YES | > 0 |
| `transaction_date` | DATE | YES | Cannot be future date |
| `reason` | VARCHAR(300) | NO | Required for `DEDUCTED` type (e.g., damage, unpaid dues) |
| `receipt_generated` | BOOLEAN | System | TRUE when refund receipt PDF is generated |
| `recorded_by` | BIGINT | System | FK → `users.id` |
| `created_at` | DATETIME | System | |

**Rules:**
- Total `REFUNDED + DEDUCTED` amounts cannot exceed total `COLLECTED` amount for the tenant.
- On recording a `REFUNDED` or full refund transaction, system updates `tenant.security_deposit_status` to `REFUNDED` or `PARTIALLY_REFUNDED`.
- `GET /api/v1/tenants/:id/deposit-receipt?lang=en|bn` generates a PDF receipt for the refund.

---

### 5.8 Dashboard

Read-only aggregated view. All monetary values in BDT. No mutations from dashboard.

#### 5.8.1 Dashboard KPI Cards

| KPI | Calculation | Scope |
|---|---|---|
| Total Income Received | `SUM(amount_paid_bdt)` WHERE `status IN (PAID, PARTIALLY_PAID)` | Selected period |
| Expected Rent | `SUM(total_due_bdt)` for all ACTIVE tenants | Selected period |
| Collection Rate | `(Income Received / Expected Rent) × 100` | Selected period |
| Overdue Amount | `SUM(balance_due_bdt)` WHERE overdue | As of today |
| Overdue Tenant Count | `COUNT(DISTINCT tenant_id)` with overdue balance | As of today |
| Total Expenses | `SUM(amount_bdt)` FROM `expenses` | Selected period |
| Net Income | Total Income − Total Expenses | Selected period |
| Vacancy Rate | `(VACANT units / total units) × 100` | As of today |
| Security Deposits Held | `SUM(security_deposit_bdt)` WHERE `tenant.status=ACTIVE` | As of today |

#### 5.8.2 Filters

| Filter | Options | Default |
|---|---|---|
| Time Range | This Month \| This Quarter \| This Year \| Last 12 Months \| Custom Date Range | This Month |
| Property | All Properties \| Specific Property \| Specific Unit | All Properties |

#### 5.8.3 Charts

- Monthly Income vs Expense bar chart (last 12 months)
- Property-wise income breakdown (horizontal bar)
- Overdue aging table: 0–30 days / 31–60 days / 61–90 days / 90+ days
- Occupancy trend line chart (last 12 months)

---

### 5.9 Expense Management

#### 5.9.1 Expense Entity — Exact Fields

| Field | Type | Required | Validation | Notes |
|---|---|---|---|---|
| `id` | BIGINT | System | Auto | PK |
| `property_id` | BIGINT | YES | FK → `properties.id` | |
| `property_unit_id` | BIGINT | NO | FK → `property_units.id`; NULL = property-level | |
| `category` | ENUM | YES | `MAINTENANCE` \| `UTILITY` \| `REPAIR` \| `CLEANING` \| `LEGAL` \| `TAX` \| `INSURANCE` \| `SALARY` \| `MISCELLANEOUS` | |
| `description` | VARCHAR(300) | YES | Min 5 chars | |
| `amount_bdt` | DECIMAL(12,2) | YES | > 0 | |
| `expense_date` | DATE | YES | Cannot be future date; stored in BST | |
| `vendor_name` | VARCHAR(150) | NO | | |
| `vendor_phone` | VARCHAR(20) | NO | Bangladeshi phone format if provided | |
| `receipt_image` | MEDIUMBLOB | NO | JPEG/PNG/PDF; max 5MB; stored in DB | No external storage |
| `receipt_image_mime_type` | VARCHAR(30) | NO | `image/jpeg` \| `image/png` \| `application/pdf` | |
| `receipt_original_name` | VARCHAR(255) | NO | Original filename | |
| `notes` | TEXT | NO | Max 500 chars | |
| `created_by` | BIGINT | System | FK → `users.id` | |
| `created_at` | DATETIME | System | Auto BST | |
| `updated_at` | DATETIME | System | Auto BST | |

#### 5.9.2 Expense Operations

| Operation | Endpoint | Rules |
|---|---|---|
| Create | `POST /api/v1/expenses` | Multipart form-data (fields + optional receipt file). |
| List | `GET /api/v1/expenses` | Filter by `propertyId`, `category`, `dateRange`. Paginated. Default sort: `expense_date DESC`. |
| Get | `GET /api/v1/expenses/:id` | Full record. Receipt image not included in body — use separate endpoint. |
| Get Receipt | `GET /api/v1/expenses/:id/receipt` | Returns binary image/PDF with correct `Content-Type`. |
| Update | `PUT /api/v1/expenses/:id` | New receipt replaces old (old BLOB overwritten). |
| Delete | `DELETE /api/v1/expenses/:id` | Hard delete if `expense_date` within current month. Older: soft delete with required `reason`. |
| Export CSV | `GET /api/v1/expenses/export?format=csv&lang=en\|bn` | Streamed CSV download. |

---

### 5.10 Data Export (CSV)

Owners can export all major data entities as CSV. All exports are streamed (not buffered in memory) to support large datasets.

| Export Type | Endpoint | Fields Included | Notes |
|---|---|---|---|
| Tenants | `GET /api/v1/export/tenants?lang=en\|bn` | All fields except NID image blob; NID masked | |
| Invoices | `GET /api/v1/export/invoices?lang=en\|bn&dateRange=` | All invoice fields; utility charges flattened | |
| Payments | `GET /api/v1/export/payments?lang=en\|bn&dateRange=` | All `invoice_payments` records | |
| Expenses | `GET /api/v1/export/expenses?lang=en\|bn&dateRange=` | All expense fields except receipt blob | |
| Security Deposits | `GET /api/v1/export/deposits?lang=en\|bn` | All deposit transaction records | |
| Full Export (ZIP) | `GET /api/v1/export/all?lang=en\|bn` | ZIP containing all above CSVs | Async job; returns job ID; poll for completion |

**CSV rules:**
- Header row labels in English (`lang=en`) or Bangla (`lang=bn`).
- Date format: `DD/MM/YYYY` (BST).
- Monetary values: numeric, no currency symbol in CSV (symbol in header label).
- Character encoding: UTF-8 with BOM (required for Bangla in Excel).

---

## 6. User Flows

### 6.1 Monthly Billing Flow

1. Owner navigates to **Billing > Generate Invoices**.
2. Selects property and billing month.
3. System shows a preview table: all ACTIVE tenants, base rent (from tenant record), enabled utility charges (from `utility_charge_configs`), late fee indicator if past due date, calculated totals.
4. Owner enters actual utility amounts for this month (e.g., gas: ৳450, electricity: ৳1,200).
5. Owner selects export language (English / Bangla) for PDF and SMS text.
6. Owner clicks **"Generate All"** or generates per tenant.
7. System creates invoice records (`status=DRAFT`), stores `utility_charges` JSON snapshot, computes all monetary fields.
8. Owner reviews PDF preview in modal.
9. Owner clicks **"Mark as Sent"** → `status=SENT`; `sms_text` composed and stored.
10. Rent is received in hand. Owner opens invoice, clicks **"Record Payment"**, enters amount and method.
11. System updates `amount_paid_bdt`, `balance_due_bdt`, sets `status=PAID` or `PARTIALLY_PAID`.
12. Dashboard KPIs recalculate automatically.

### 6.2 New Tenant Onboarding Flow

1. Owner navigates to **Tenants > Add Tenant**.
2. Selects property, then selects a `VACANT` unit.
3. Fills out tenant form. System validates: phone (`^\+8801[3-9]\d{8}$`), NID (exactly 17 digits).
4. Owner uploads NID image (JPEG/PNG, max 5MB). Image stored as MEDIUMBLOB.
5. Enters lease dates, monthly rent, security deposit.
6. On save: phone, NID, emergency contact phone encrypted via AES-256. Unit status → `OCCUPIED`. `tenant_unit_history` record created. Security deposit `COLLECTED` transaction recorded automatically.
7. Audit log: `"Tenant {name} added to unit {unit_identifier} by {user}."`

### 6.3 Tenant Move-Out Flow

1. Owner opens tenant record, clicks **"End Tenancy."**
2. System shows warning if outstanding invoices exist with `balance_due > 0`: `"N invoices remain unpaid. Total outstanding: ৳X."`
3. Owner enters move-out date and security deposit refund amount (can be partial; any deductions require a reason).
4. Owner confirms. System:
   - Sets `tenant.status = INACTIVE`.
   - Closes `tenant_unit_history.end_date`.
   - Records `REFUNDED` or `DEDUCTED` transaction(s) in `security_deposit_transactions`.
   - Generates Security Deposit Refund Receipt PDF.
   - Unit `occupancy_status` → `VACANT`.
5. Outstanding invoices remain under `OVERDUE` — not auto-cancelled.
6. Audit log: `"Tenancy ended for {name} on {date}. Deposit ৳X refunded."`

### 6.4 Configuring Utility Charges for a Property

1. Owner navigates to **Settings > Properties > {Property Name} > Utility Charges**.
2. System lists all `utility_charge_configs` for the property (Gas, Electricity, Water, Service Charge, etc.).
3. Owner toggles each charge on/off (`is_enabled`), updates `label_en`, `label_bn`, and optionally sets a `default_amount_bdt`.
4. Owner can add a custom charge with a custom label (stored with `charge_key=OTHER` + custom label).
5. Changes take effect on the **next invoice generation** — existing invoices are not affected.

### 6.5 Applying a FIFO Payment

1. Owner navigates to a tenant record. System shows outstanding invoice list.
2. Owner clicks **"Apply Payment"** at the tenant level.
3. Enters total amount received, date, and payment method.
4. System distributes FIFO: oldest invoice balance first, remainder to next oldest.
5. Response shows allocation breakdown. All affected invoices and audit logs updated.

---

## 7. Data Model

### 7.1 Entity Relationship Summary

| Entity | Relationship | Cardinality | Notes |
|---|---|---|---|
| `users` | → `properties` | 1 : MANY | One owner owns many properties |
| `properties` | → `property_units` | 1 : MANY | Delete blocked if active tenants |
| `properties` | → `property_settings` | 1 : 1 | Per-property configuration |
| `properties` | → `utility_charge_configs` | 1 : MANY | Configurable per property |
| `property_units` | → `tenants` (active) | 1 : 0..1 | One active tenant per unit at a time |
| `tenants` | → `tenant_unit_history` | 1 : MANY | Full assignment history |
| `tenants` | → `invoices` | 1 : MANY | |
| `tenants` | → `security_deposit_transactions` | 1 : MANY | |
| `property_units` | → `invoices` | 1 : MANY | Denormalized on invoice |
| `invoices` | → `invoice_payments` | 1 : MANY | Tracks individual payment instalments |
| `invoices` | → `audit_logs` | 1 : MANY | All mutations logged |
| `properties` | → `expenses` | 1 : MANY | |
| `users` | → `audit_logs` | 1 : MANY | All user actions |
| `users` | → `refresh_tokens` | 1 : MANY | Multi-device sessions |

### 7.2 Additional Entity Schemas

#### 7.2.1 `tenant_unit_history`

| Field | Type | Notes |
|---|---|---|
| `id` | BIGINT | PK |
| `tenant_id` | BIGINT | FK → `tenants.id` |
| `property_unit_id` | BIGINT | FK → `property_units.id` |
| `start_date` | DATE | Move-in date |
| `end_date` | DATE | NULL = currently active |
| `monthly_rent_bdt_snapshot` | DECIMAL(12,2) | Rent at start of this tenancy period |
| `created_at` | DATETIME | |

#### 7.2.2 `invoice_payments`

| Field | Type | Notes |
|---|---|---|
| `id` | BIGINT | PK |
| `invoice_id` | BIGINT | FK → `invoices.id` |
| `amount_bdt` | DECIMAL(12,2) | > 0; cumulative cannot exceed `total_due_bdt` |
| `payment_date` | DATE | |
| `payment_method` | ENUM | `CASH` \| `BANK_TRANSFER` \| `CHEQUE` \| `BKASH` \| `NAGAD` \| `OTHER` |
| `payment_reference` | VARCHAR(100) | Transaction ID, cheque number, etc. |
| `applied_via_fifo` | BOOLEAN | TRUE if applied via FIFO bulk endpoint |
| `recorded_by` | BIGINT | FK → `users.id` |
| `created_at` | DATETIME | |

#### 7.2.3 `audit_logs`

| Field | Type | Notes |
|---|---|---|
| `id` | BIGINT | PK |
| `entity_type` | VARCHAR(50) | `INVOICE` \| `TENANT` \| `EXPENSE` \| `PROPERTY` \| `DEPOSIT` \| `SETTINGS` |
| `entity_id` | BIGINT | FK to affected entity |
| `action` | VARCHAR(60) | `CREATED` \| `UPDATED` \| `DELETED` \| `STATUS_CHANGED` \| `PDF_GENERATED` \| `PAYMENT_RECORDED` \| `DEPOSIT_REFUNDED` \| `NID_IMAGE_VIEWED` |
| `actor_id` | BIGINT | FK → `users.id` |
| `actor_ip` | VARCHAR(45) | IPv4 or IPv6 |
| `previous_state` | JSON | Snapshot before mutation; NULL for CREATED |
| `new_state` | JSON | Snapshot after mutation |
| `notes` | TEXT | Required for manual overrides (min 10 chars) |
| `created_at` | DATETIME | Indexed; append-only (DB trigger prevents UPDATE/DELETE) |

#### 7.2.4 `users`

| Field | Type | Notes |
|---|---|---|
| `id` | BIGINT | PK |
| `email` | VARCHAR(150) | Unique; used for login |
| `password_hash` | VARCHAR(60) | bcrypt hash |
| `full_name` | VARCHAR(120) | |
| `role` | ENUM | `OWNER` \| `MANAGER` \| `TENANT` |
| `owner_id` | BIGINT | NULL for OWNER role; FK → `users.id` for MANAGER (who manages them) |
| `is_active` | BOOLEAN | Default TRUE |
| `preferred_language` | ENUM | `EN` \| `BN`; default `EN`; controls default PDF/export language |
| `last_login_at` | DATETIME | |
| `created_at` | DATETIME | |
| `updated_at` | DATETIME | |

---

## 8. API Design Overview

### 8.1 Conventions

- **Base URL:** `https://{domain}/api/v1/`
- **Content-Type:** `application/json` (except multipart endpoints for file uploads)
- **Authentication:** `Authorization: Bearer {JWT}` on every protected endpoint
- **Pagination:** `?page=0&size=20` (Spring Data JPA zero-indexed). Response envelope:
  ```json
  {
    "data": [],
    "pagination": { "totalElements": 0, "totalPages": 0, "page": 0, "size": 20 }
  }
  ```
- **Timestamps:** All stored and returned in BST (`Asia/Dhaka`), formatted as `dd/MM/yyyy HH:mm:ss`
- **Dates:** `dd/MM/yyyy` (e.g., `01/06/2025`)
- **Monetary values:** Returned as `String` with 2 decimal places (`"12500.00"`) to avoid float precision issues. Prefix `৳` added in frontend only.
- **Error format:**
  ```json
  { "error": { "code": "TENANT_NOT_FOUND", "message": "Tenant with id 42 not found.", "field": "tenantId" } }
  ```
- **Versioning:** URL path versioning. Breaking changes increment to `/api/v2/`.
- **Spring Boot specifics:** Use `@RestController`, `ResponseEntity<>`, Spring's `@ExceptionHandler` via `@ControllerAdvice` for all error responses.

### 8.2 Full Endpoint Summary

| Resource | Method | Endpoint | Description |
|---|---|---|---|
| Auth | POST | `/auth/login` | Login; returns tokens |
| Auth | POST | `/auth/refresh` | Refresh access token |
| Auth | POST | `/auth/logout` | Logout current device |
| Auth | POST | `/auth/logout-all` | Logout all devices |
| Auth | POST | `/auth/forgot-password` | Request OTP |
| Auth | POST | `/auth/reset-password` | Reset password |
| Users | GET | `/users/me` | Get current user profile |
| Users | PUT | `/users/me` | Update profile / preferred language |
| Users | GET | `/users/sessions` | List active refresh token sessions |
| Users | DELETE | `/users/sessions/:tokenId` | Revoke specific session |
| Properties | GET | `/properties` | List all (owner-scoped) |
| Properties | POST | `/properties` | Create with units array |
| Properties | GET | `/properties/:id` | Detail + units + tenant stubs |
| Properties | PUT | `/properties/:id` | Update metadata |
| Properties | DELETE | `/properties/:id` | Soft delete |
| Units | GET | `/properties/:id/units` | List units |
| Units | POST | `/properties/:id/units` | Add unit |
| Units | PUT | `/properties/:id/units/:unitId` | Update unit |
| Prop Settings | GET | `/properties/:id/settings` | Get property settings |
| Prop Settings | PUT | `/properties/:id/settings` | Update settings |
| Utility Config | GET | `/properties/:id/utility-charges` | List utility charge configs |
| Utility Config | PUT | `/properties/:id/utility-charges` | Bulk update (enable/disable/reorder) |
| Utility Config | POST | `/properties/:id/utility-charges` | Add custom charge |
| Tenants | GET | `/tenants` | List (paginated, filterable) |
| Tenants | POST | `/tenants` | Create (multipart with NID image) |
| Tenants | GET | `/tenants/:id` | Full detail |
| Tenants | PUT | `/tenants/:id` | Update |
| Tenants | DELETE | `/tenants/:id` | Soft delete |
| Tenants | GET | `/tenants/:id/nid-image` | Download NID image |
| Tenants | PUT | `/tenants/:id/nid-image` | Replace NID image |
| Tenants | GET | `/tenants/:id/history` | Unit assignment history |
| Tenants | GET | `/tenants/:id/invoices` | Invoices for this tenant |
| Deposits | GET | `/tenants/:id/deposits` | Deposit transaction history |
| Deposits | POST | `/tenants/:id/deposits` | Record refund/deduction |
| Deposits | GET | `/tenants/:id/deposit-receipt` | Generate refund receipt PDF |
| Invoices | GET | `/invoices` | List (filterable) |
| Invoices | POST | `/invoices` | Generate single invoice |
| Invoices | POST | `/invoices/bulk-generate` | Bulk for property + month |
| Invoices | GET | `/invoices/:id` | Full detail |
| Invoices | PUT | `/invoices/:id` | Update DRAFT/SENT |
| Invoices | POST | `/invoices/:id/send` | Mark SENT; compose SMS text |
| Invoices | POST | `/invoices/:id/payments` | Record payment (specific invoice) |
| Invoices | POST | `/invoices/payments/apply` | Apply payment FIFO across tenant invoices |
| Invoices | POST | `/invoices/:id/cancel` | Cancel |
| Invoices | POST | `/invoices/:id/void` | Void (Owner only) |
| Invoices | GET | `/invoices/:id/pdf` | Download PDF (`?lang=en\|bn`) |
| Expenses | GET | `/expenses` | List |
| Expenses | POST | `/expenses` | Create (multipart) |
| Expenses | GET | `/expenses/:id` | Detail |
| Expenses | GET | `/expenses/:id/receipt` | Download receipt image/PDF |
| Expenses | PUT | `/expenses/:id` | Update |
| Expenses | DELETE | `/expenses/:id` | Delete |
| Dashboard | GET | `/dashboard/summary` | KPI cards |
| Dashboard | GET | `/dashboard/income-trend` | Monthly income/expense chart |
| Dashboard | GET | `/dashboard/property-breakdown` | Per-property breakdown |
| Dashboard | GET | `/dashboard/overdue-aging` | Aging buckets |
| Export | GET | `/export/tenants` | CSV |
| Export | GET | `/export/invoices` | CSV |
| Export | GET | `/export/payments` | CSV |
| Export | GET | `/export/expenses` | CSV |
| Export | GET | `/export/deposits` | CSV |
| Export | GET | `/export/all` | ZIP of all CSVs (async) |
| Export | GET | `/export/jobs/:jobId` | Poll async export job status |
| Audit | GET | `/audit-logs` | List (Owner only; paginated) |

---

## 9. Validation Rules

| Field / Context | Rule | Error Code |
|---|---|---|
| Phone (all phone fields) | Regex: `^\+8801[3-9]\d{8}$`. Must be exactly 14 chars. SIM numbers only. | `INVALID_PHONE_FORMAT` |
| NID | Exactly 17 numeric digits: `^\d{17}$` | `INVALID_NID_FORMAT` |
| NID Image | Required on tenant create. MIME: `image/jpeg` or `image/png`. Max 5MB. | `NID_IMAGE_REQUIRED` / `INVALID_FILE_TYPE` / `FILE_TOO_LARGE` |
| Lease dates | `lease_end_date > lease_start_date` if both provided. NULL `lease_end_date` = month-to-month. | `INVALID_LEASE_DATE_RANGE` |
| Monthly rent | `DECIMAL(12,2)`. `> 0`. Max `9,999,999.99`. | `INVALID_RENT_AMOUNT` |
| Security deposit | `DECIMAL(12,2)`. `>= 0`. | `INVALID_DEPOSIT_AMOUNT` |
| Billing period | `billing_period_start` = 1st of month. `billing_period_end` = last day of same month. | `INVALID_BILLING_PERIOD` |
| Duplicate invoice | No two non-CANCELLED/VOID invoices for same `tenant_id + billing_period_start`. | `DUPLICATE_INVOICE` |
| Unit occupancy | Cannot assign ACTIVE tenant to unit with `occupancy_status=OCCUPIED`. | `UNIT_ALREADY_OCCUPIED` |
| Payment amount | `amount_bdt > 0`. Cumulative payments `<= total_due_bdt`. | `INVALID_PAYMENT_AMOUNT` / `OVERPAYMENT` |
| FIFO payment | `totalAmountBdt` must be `> 0` and `<= SUM(balance_due_bdt)` of all outstanding invoices for tenant. | `INVALID_FIFO_PAYMENT` |
| Expense date | `expense_date <= today (BST)`. | `FUTURE_EXPENSE_DATE` |
| Receipt file | MIME: `image/jpeg` \| `image/png` \| `application/pdf`. Max 5MB. | `INVALID_FILE_TYPE` / `FILE_TOO_LARGE` |
| Total units | `total_units` must match COUNT of `property_units` records. | `UNIT_COUNT_MISMATCH` |
| Password | Min 8 chars, 1 uppercase, 1 digit, 1 special char. | `WEAK_PASSWORD` |
| Late fee override | Manual override requires `notes` (min 10 chars). | `MISSING_OVERRIDE_REASON` |
| District | Must be from 64 official Bangladesh districts enum. | `INVALID_DISTRICT` |
| Utility charge amount | `>= 0`. If 0, line item excluded from invoice and PDF. | `NEGATIVE_CHARGE_AMOUNT` |
| Deposit refund | `REFUNDED + DEDUCTED <= COLLECTED` for that tenant. | `DEPOSIT_REFUND_EXCEEDS_HELD` |
| Deposit deduction | Requires non-empty `reason`. | `DEDUCTION_REASON_REQUIRED` |
| Export language | `lang` query param: `en` or `bn` only. | `INVALID_LANGUAGE_CODE` |

---

## 10. Security Considerations

| Concern | Implementation |
|---|---|
| **PII Encryption at Rest** | AES-256-GCM for `phone_primary`, `phone_secondary`, `nid_number`, `emergency_contact_phone`. Encryption key in environment variable (or Spring Cloud Config). Per-record IV stored alongside ciphertext in DB. Implemented as JPA `AttributeConverter`. |
| **NID Image Storage** | Stored as `MEDIUMBLOB` in MySQL. Not accessible via direct URL. Served only through authenticated API endpoint. Access logged to `audit_logs` with `NID_IMAGE_VIEWED` action. |
| **Password Storage** | bcrypt, cost factor 12. Spring Security's `BCryptPasswordEncoder`. No plaintext. |
| **JWT Security** | Access token: RS256 (asymmetric), 24h TTL. Refresh tokens: opaque UUID stored as SHA-256 hash in `refresh_tokens` table. Multi-device: multiple rows per user. |
| **HTTPS** | TLS 1.2+ only. HSTS header (`Strict-Transport-Security`). No HTTP in production. |
| **CORS** | `CorsConfigurationSource` bean with strict origin whitelist. `allowCredentials: true` for frontend origin only. |
| **SQL Injection** | Spring Data JPA parameterized queries everywhere. No native query string interpolation. `@Query` with named parameters only. |
| **Rate Limiting** | Spring + Bucket4j (in-memory or Redis). Login: 5 attempts / 15 min / IP. API global: 100 req/min per user. Bulk invoice: 1 request / property / minute. |
| **File Upload Security** | MIME validated via Apache Tika (magic bytes, not extension). Files stored as BLOB — no filesystem path traversal risk. Max size enforced in `spring.servlet.multipart.max-file-size=5MB`. |
| **Audit Logging** | All writes append to `audit_logs`. DB trigger `BEFORE UPDATE/DELETE ON audit_logs` raises error to enforce append-only. Previous/new state stored as JSON (excluding encrypted fields). |
| **Input Sanitisation** | `@Size`, `@Pattern`, `@NotBlank` on all DTOs. Strip HTML from all text fields via custom validator. |
| **PDF Generation** | Server-side via PDFBox/iText. No user-supplied HTML in template engine. Bangla text shaped server-side. |
| **Least Privilege DB User** | App DB user: `SELECT`, `INSERT`, `UPDATE` on app tables only. No `DROP`, `ALTER`, `CREATE`. DDL managed by Flyway with separate migration user. |
| **Sensitive Data in Logs** | Logback `PatternLayout` + custom `MaskingPatternLayout` to mask NID and phone patterns in all log output. |
| **BST Timezone Enforcement** | Spring Boot: `spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Dhaka`. MySQL: `time_zone='+06:00'`. Consistent across app and DB. |

---

## 11. Edge Cases

| # | Scenario | Expected Behaviour |
|---|---|---|
| EC-01 | Invoice generated for a month where `lease_end_date` falls mid-month | Allow generation with a warning: *"Lease ends on {date} during this billing period."* Owner must confirm. |
| EC-02 | `monthly_rent_bdt` updated after invoice already generated for that month | Invoice retains the `base_rent_bdt` snapshot. New rent applies to future invoices only. |
| EC-03 | Bulk generation — one tenant's invoice already exists for that month | Skip that tenant; return partial success response listing skipped tenants with reason `DUPLICATE_INVOICE`. |
| EC-04 | Utility charge config changed after invoices generated for that month | Existing invoices retain their `utility_charges` JSON snapshot. Config change affects next generation only. |
| EC-05 | FIFO payment — payment amount equals exactly one invoice's balance | First invoice → `PAID`; no remainder to apply. Correct. |
| EC-06 | FIFO payment — payment exceeds all outstanding invoice balances | Reject with `OVERPAYMENT`. Return total outstanding amount. Suggest owner enter the correct amount. |
| EC-07 | Concurrent requests to generate invoice for same tenant + month | DB unique constraint on `(tenant_id, billing_period_start)` where `status NOT IN (CANCELLED, VOID)`. Second request → `409 Conflict`. |
| EC-08 | NID image upload fails (DB write error mid-transaction) | `@Transactional` rolls back entire tenant creation. No partial tenant record created. |
| EC-09 | Receipt image upload fails mid-expense creation | Same transaction: entire expense creation rolled back. |
| EC-10 | PDF generation fails (PDFBox error for Bangla font) | Return `503 PDF_GENERATION_FAILED`. Log full stack trace. Do not return a corrupted PDF. Invoice remains in current status. |
| EC-11 | Lease `end_date` not set (month-to-month) | No automated deactivation. Owner must manually trigger End Tenancy. `OVERDUE` job still runs. |
| EC-12 | Tenant has OVERDUE invoices when `End Tenancy` is triggered | System shows warning with total outstanding. Owner must acknowledge before proceeding. Invoices remain OVERDUE after move-out. |
| EC-13 | Late fee grace period = 0 and invoice is generated on the due date | `current_date == due_date` → grace period not exceeded → `late_fee_bdt = 0`. Late fee only applies when `current_date > due_date + grace_days` (strictly greater than). |
| EC-14 | Owner records payment with `payment_date` in past | Allowed. `payment_date` is the date cash was physically received. System does not validate against today. Late fee re-evaluation occurs: if `payment_date > due_date + grace_days`, late fee is applicable even if invoice was generated without one. |
| EC-15 | Security deposit refund amount > deposit held | Rejected with `DEPOSIT_REFUND_EXCEEDS_HELD`. System shows currently held balance. |
| EC-16 | Export ZIP job (async) fails mid-generation | Job status set to `FAILED`. `GET /export/jobs/:jobId` returns `{"status": "FAILED", "error": "..."}`. Owner can retry. |
| EC-17 | Bangla PDF requested but Bangla font file missing from classpath | Log `WARN: Bangla font not found, falling back to English`. Generate English PDF. Return `X-Language-Fallback: en` header so the frontend can notify the user. |
| EC-18 | `invoice_due_day_of_month` = 31 for a month with 28 days (February) | Use last day of the month (`28` or `29`). Logic: `min(due_day, lastDayOfMonth(billing_period_start))`. |

---

## 12. Non-Functional Requirements

| Category | Requirement | Target |
|---|---|---|
| **Performance** | API p95 response time | < 300ms read; < 800ms write/calculation |
| **Performance** | Dashboard load (12-month, 50 properties) | < 2s |
| **Performance** | PDF generation | < 5s per invoice; async for bulk (job-based) |
| **Performance** | CSV export (10,000 invoices) | Streamed; first byte < 2s; no OOM |
| **Availability** | Uptime SLA | 99.5% monthly (max 4h scheduled downtime/month) |
| **Scalability** | Data volume | 10,000 tenants; 500,000 invoices; no schema changes needed |
| **Scalability** | BLOB storage | MySQL `MEDIUMBLOB` max 16MB. Monitor DB size growth; document upgrade path to dedicated storage for v2.0. |
| **Security** | PII encryption | AES-256-GCM; key rotation without data loss via re-encryption job |
| **Security** | OWASP Top 10 | Addressed before launch. |
| **Maintainability** | Test coverage | >= 80% on service/business logic layer. All invoice calculation paths covered by integration tests. |
| **Maintainability** | API docs | SpringDoc OpenAPI 3.0 auto-generated at `/api/docs` and `/api/docs.yaml` |
| **Maintainability** | DB migrations | Flyway; all migrations versioned; no destructive migrations in production without rollback script |
| **Observability** | Logging | Logback structured JSON. Log level configurable via env. PII masked. |
| **Observability** | Actuator | Spring Boot Actuator: `/actuator/health`, `/actuator/metrics`, `/actuator/info` |
| **Data Integrity** | Transactions | All multi-table writes in `@Transactional`. No partial states. |
| **Data Integrity** | Backup | Daily DB dump. Point-in-time recovery for 7 days. |
| **Timezone** | BST everywhere | `Asia/Dhaka` (UTC+6) in Spring app, MySQL server, and Vue date rendering. |
| **Browser Support** | Targets | Chrome 90+, Firefox 90+, Edge 90+, Safari 14+. IE: Not supported. |
| **Accessibility** | WCAG | 2.1 Level AA on login, invoice list, tenant form. |

---

## 13. Future Enhancements

| Version | Feature | Notes |
|---|---|---|
| v1.1 | Bangla UI runtime language toggle | Vue I18n `bn` locale. Backend Bangla error messages. |
| v1.1 | Tenant login portal (read-only) | `users` table already has `TENANT` role + `tenant.user_id` FK. Activate route guard. |
| v1.1 | Lease expiry automated reminders | Spring `@Scheduled` cron; compose SMS text 30/15/7 days before `lease_end_date`. |
| v1.1 | Overpayment credit tracking | Credit balance per tenant; auto-apply to next invoice. |
| v1.1 | Invoice QR code | Static QR on PDF linking to invoice detail page. |
| v2.0 | iOS & Android mobile app | Consumes existing `/api/v1/` REST API. Native SMS intent for composed `sms_text`. |
| v2.0 | Online payment gateway | bKash, Nagad, SSLCOMMERZ. Payment webhook updates invoice status. |
| v2.0 | Push notifications | Replace SMS text compose with actual push via FCM/APNs. |
| v2.0 | Migrate BLOB storage to object storage | S3/R2/MinIO for NID images and receipts. DB BLOB → URL reference. |
| v2.0 | Maintenance ticketing | Tenant submits, owner tracks resolution. |
| v2.1 | Bank statement reconciliation | Import CSV; auto-match payments to invoices. |
| v2.1 | Tax/VAT reporting | Annual income summary for tax filing. |
| v3.0 | Co-ownership model | Multiple owners per property (architecture stub exists in `users.owner_id`). |
| v3.0 | Multi-agency / SaaS | Account isolation; subscription billing. |

---

## 14. Resolved Clarifications

This section documents the decisions made from the initial open questions. These replace the ambiguities in v1.0 and are now firm requirements.

| # | Area | Decision |
|---|---|---|
| Q-01 | NID Format | **Default: exactly 17 numeric digits** (`^\d{17}$`). No 10-digit legacy support in v1.0. Owner **uploads an NID image** (stored as DB BLOB); no OCR or data extraction. |
| Q-02 | Phone Format | **SIM numbers only.** Regex `^\+8801[3-9]\d{8}$` covers Grameenphone (017), Robi (018), Banglalink (019), Teletalk (015), Airtel (016), etc. No VOIP support. |
| Q-03 | Partial Payments | **FIFO:** Payment applied to **oldest outstanding invoice first**, then remainder to next oldest. Dedicated FIFO endpoint available. Direct per-invoice payment endpoint also available. |
| Q-04 | Invoice Status | **Manual status change by Owner/Manager** after receiving rent in hand. Invoice shows current bill. When **tenant login is enabled (v1.1)**, tenants see the status as updated by owner — they cannot change it. |
| Q-05 | Co-ownership | **Not implemented in v1.0.** `users` table and `properties.owner_id` designed for extensibility. One owner account per property in v1.0. |
| Q-06 | Security Deposit | **Fully tracked.** `security_deposit_transactions` records collection, deductions, and refunds. System generates a **PDF refund receipt** on move-out. |
| Q-07 | Multi-currency | **BDT only, always.** No FX conversion. All fields stored and displayed in BDT. |
| Q-08 | PDF / Export Language | **User selects language per export/invoice: English or Bangla.** User has `preferred_language` setting (default `EN`). Bangla requires embedded TTF font in PDF engine (Kalpurush/SolaimanLipi + ICU4J). |
| Q-09 | SMS Provider | **No SMS provider.** System composes pre-formatted SMS text and stores it on the invoice. Mobile app (v2.0) passes this text to the **Android native SMS intent / iOS MFMessageComposeViewController**. Zero gateway integration. |
| Q-10 | Timezone | **Bangladesh Standard Time (BST = UTC+6) everywhere.** Spring `Asia/Dhaka`, MySQL `+06:00`. All dates displayed and stored in BST. |
| Q-11 | Utility Charges on Invoice | **Configurable per property.** Gas, Electricity, Water, Service Charge (and custom labels) managed via `utility_charge_configs`. Owner toggles each on/off. Enabled charges appear as line items on invoice; owner enters actual amount at generation time. |
| Q-12 | Settings Scope | **Per property** via `property_settings` table. Each property has independent late fee rules, due date, tax, and footer text. No global owner-level settings. |
| Q-13 | CSV Export | **Full CSV export available** for tenants, invoices, payments, expenses, deposits. Bulk ZIP export (async job). UTF-8 with BOM for Bangla compatibility in Excel. |
| Q-14 | Concurrent Sessions | **Multiple device sessions allowed.** Each login creates a new `refresh_tokens` row. User can view and revoke individual sessions. `logout` clears current device only; `logout-all` clears all. |
| Q-15 | File Storage | **MySQL MEDIUMBLOB only.** No S3, no MinIO, no local filesystem. NID images and expense receipts stored as binary in DB. Migration to object storage documented as v2.0 task. |

---

*Document Control: This PRD is version-controlled. All deviations from specifications must be logged as an ADR (Architecture Decision Record) before implementation. Resolved Clarifications (Section 14) are firm requirements — reopen via a formal change request only.*
