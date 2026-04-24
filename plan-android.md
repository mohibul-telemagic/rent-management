# Android Refactor Plan (RentEase Senior)

Date: 2026-03-11
Scope: `android-app/`
Primary goal: Ship a working, simple, elegant Android experience for older adults with clear navigation and low-cognitive-load rent operations.

## 1) User-Requested UX Targets

1. Login screen must include branding:
   - Visible logo mark
   - Pleasant background treatment
   - Clear sign-in action
2. Post-login first screen is a dashboard focused on unpaid rent:
   - Which tenant owes how much
   - Which unit/flat they occupy
   - Current status (`SENT`, `OVERDUE`, `PARTIALLY_PAID`, etc.)
3. Property management:
   - Show all properties and units/flats
   - Tap into a property to create/update/delete property
   - Manage units/apartments inside property
4. Tenant management:
   - Tenant list with due indicator
   - Tap tenant for detail page
   - Show payment history and current due/paid status
   - Send SMS with invoice PDF link from tenant detail
5. Keep UX simple for older adults:
   - Bigger controls, high contrast, plain labels, predictable navigation

## 2) Data + API Mapping (from backend code + PRD)

Available endpoints used in app:
- Auth:
  - `POST /api/v1/auth/login`
- Properties/Units:
  - `GET /api/v1/properties`
  - `POST /api/v1/properties`
  - `PUT /api/v1/properties/{id}`
  - `DELETE /api/v1/properties/{id}`
  - `GET /api/v1/properties/{id}/units`
  - `POST /api/v1/properties/{id}/units`
  - `PUT /api/v1/properties/{id}/units/{unitId}`
- Tenants:
  - `GET /api/v1/tenants`
  - `GET /api/v1/tenants/{id}`
  - `GET /api/v1/tenants/{id}/ledger`
  - `GET /api/v1/tenants/{id}/history`
- Invoices/Payments/SMS:
  - `GET /api/v1/invoices` (filter by `tenantId`, `billingMonth`)
  - `POST /api/v1/invoices`
  - `POST /api/v1/invoices/{id}/send`
  - `POST /api/v1/invoices/{id}/payments`

Note:
- Unit delete endpoint is not exposed in current backend; unit "manage" on Android is create + update.

## 3) App Information Architecture

1. `LoginScreen`
   - Branded background + logo
   - Email/password
   - Big "Sign in" button
2. `HomeShell` (after login)
   - Top app bar with logout + refresh
   - 3 simple tabs:
     - Dashboard
     - Properties
     - Tenants
3. `DashboardTab`
   - KPI strip (total due, unpaid tenants, overdue count)
   - Unpaid tenant cards with:
     - Tenant name
     - Property + Unit
     - Due amount
     - Status chip
4. `PropertiesTab`
   - Property list cards
   - Property form (create/update/delete)
   - Unit manager per selected property (create/update)
5. `TenantsTab`
   - Tenant list with due badges
   - Tenant detail panel:
     - Contact/rent basics
     - Ledger totals
     - Recent invoices + payments
     - "Send SMS with PDF link" action

## 4) Iterative Build Sequence

### Iteration A: Plan + API readiness
- Update `plan-android.md` with this strategy.
- Extend `Api.kt` models and endpoints needed for property/tenant detail workflows.

### Iteration B: Navigation shell + login redesign
- Replace single vertical flow with tab shell.
- Build logo+background login page.
- Keep auth and session persistence stable.

### Iteration C: Dashboard unpaid-first experience
- Aggregate invoice dues by tenant.
- Resolve property/unit labels.
- Render clear unpaid cards with statuses.

### Iteration D: Property + unit operations
- Add property create/edit/delete flow.
- Add unit create/edit flow scoped to selected property.
- Add optimistic refresh + readable backend errors.

### Iteration E: Tenant detail and communication
- Add tenant detail + ledger + history retrieval.
- Show current due/paid + previous payments.
- Enable SMS intent with backend-generated `smsText` and PDF link fallback.

### Iteration F: UX polish + validation + build check
- Increase touch target sizes and typography.
- Reduce clutter and duplicate inputs.
- Verify compile with `:app:assembleDebug`.

## 5) Usability Rules (Older Adult Focus)

- Minimum 52dp primary button height.
- Avoid hidden critical actions; use labeled buttons.
- Keep one main action per card section.
- Prefer list selections over typed IDs.
- Use currency format consistently (`BDT x,xxx.xx`).
- Show friendly actionable errors (`what failed` + `what to do`).

## 6) Risks and Mitigations

- Risk: large single-file Compose refactor may break compile.
  - Mitigation: apply in phases and compile after core milestones.
- Risk: backend validation mismatch for update forms.
  - Mitigation: mirror enum values and required fields from backend DTOs.
- Risk: emulator has no SMS app.
  - Mitigation: fail gracefully with snackbar; keep invoice link visible.

## 7) Done Criteria

- APK installs and app loads with branded login page.
- Dashboard clearly lists unpaid tenants with due amount + status + unit/property context.
- Property tab supports create/update/delete properties and create/update units.
- Tenant tab supports list + detail with payment/ledger visibility.
- Tenant detail can trigger SMS intent with invoice/PDF link text.
- `:app:assembleDebug` succeeds.
