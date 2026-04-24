# RentEase BD Problem Synthesis

Date: 2026-04-15

This note synthesizes:
- documented gaps and blockers from the repo's Markdown files
- current implementation findings from code inspection
- deployment findings for Render Hobby

It is intended to answer one question clearly: what is still problematic, why it matters, and what needs to be resolved before a stable hosted deployment and mobile use.

## 1. Executive Summary

The repo is functionally broad, but there is a gap between:
- the desired production target in the spec and plans
- the current implementation details in code
- the realities of deploying on Render Hobby

The main problems are:
- the backend is built around MySQL, which does not map cleanly to Render's managed database path
- export ZIP jobs rely on temp files and in-memory tracking, which is fragile on ephemeral containers
- Bangla PDF support is specified as embedded-font capable, but the required Bengali font file is not present in resources
- several quality-hardening items are still explicitly marked incomplete in project docs
- the Android app is not production-hosted by default and still points to localhost
- the web frontend is mobile-usable, but not yet a PWA and may need further narrow-screen refinement

## 2. Problems Explicitly Documented In Markdown Files

### 2.1 Tracking Matrix Says Several Areas Are Still In Progress

Source: [docs/tracking-matrix.md](C:\Users\mohibul\Documents\rent\docs\tracking-matrix.md)

Documented unresolved issues:
- DB + Migrations: advanced index and invariant migration gaps
- Security + Auth: rate-limit and session-listing hardening pending
- Crypto + Audit: full mutation coverage and masking policy pending
- Tenant Module: DB-level occupancy constraint hardening pending
- Quality Hardening: performance, security, and WCAG work still pending

Why this matters:
- these are not cosmetic tasks
- they affect correctness, abuse resistance, accessibility, and release readiness

### 2.2 Implementation Steps Still Mark Foundational Hardening As Partial

Source: [implementation-steps.md](C:\Users\mohibul\Documents\rent\implementation-steps.md)

Documented unresolved issues:
- CI and branch policy artifacts are partial in repo
- DB-level active-invoice and related hard constraints are partial
- audit coverage is partial
- masking rules are not implemented
- some tenant invariants are still service-layer only
- full perf/security/WCAG hardening remains pending

Why this matters:
- service-layer-only protections are weaker than DB-backed invariants
- release quality is still below the bar described in the spec

### 2.3 Plan And Spec Set A Higher Production Bar Than Current Repo State

Sources:
- [plan.md](C:\Users\mohibul\Documents\rent\plan.md)
- [spec.md](C:\Users\mohibul\Documents\rent\spec.md)

The design docs require:
- strict DB constraints for occupancy and invoice uniqueness
- TLS-only deployment and HSTS
- strong rate limiting
- append-only audit coverage for all writes
- embedded Bangla PDF font with fallback behavior
- performance targets for API, dashboard, PDF, and CSV
- no open critical/high issues before release

Current issue:
- the docs describe production expectations that are not yet fully closed in implementation or verification

### 2.4 Android Plan And README Confirm Mobile Is Real, But Still Local/Refactor-Oriented

Sources:
- [plan-android.md](C:\Users\mohibul\Documents\rent\plan-android.md)
- [android-app/README.md](C:\Users\mohibul\Documents\rent\android-app\README.md)

Documented risks:
- large single-file Compose refactor may break compile
- backend validation mismatch risk
- emulator/SMS environment issues
- local-hosted backend assumptions remain in Android setup docs

Why this matters:
- mobile support exists, but production readiness is not finished

## 3. Problems Found In Code While Checking Render Feasibility

### 3.1 Backend Is Coupled To MySQL

Relevant files:
- [application.yml](C:\Users\mohibul\Documents\rent\backend\src\main\resources\application.yml)
- [build.gradle.kts](C:\Users\mohibul\Documents\rent\backend\build.gradle.kts)
- [docker-compose.yml](C:\Users\mohibul\Documents\rent\docker-compose.yml)

Current state:
- datasource defaults to MySQL
- Flyway is configured with MySQL support
- production-style compose depends on a MySQL container

Problem for Render Hobby:
- Render is a much better fit with managed Postgres than with self-hosted MySQL
- keeping MySQL means either:
  - migrating to Postgres later
  - using an external MySQL provider now
  - or self-managing MySQL in a way that is operationally weaker on a small plan

Impact:
- this is the primary deployment architecture blocker

### 3.2 Backend Port Is Hardcoded Instead Of Using Render's `PORT`

Relevant file:
- [application.yml](C:\Users\mohibul\Documents\rent\backend\src\main\resources\application.yml)

Current state:
- `server.port: 8080`

Problem:
- hosted web services on platforms like Render expect binding to the runtime-provided `PORT`

Impact:
- backend may fail to come up correctly unless patched

### 3.3 Frontend Docker/Nginx Routing Is Compose-Specific

Relevant files:
- [frontend/nginx.conf](C:\Users\mohibul\Documents\rent\frontend\nginx.conf)
- [frontend/src/api.ts](C:\Users\mohibul\Documents\rent\frontend\src\api.ts)

Current state:
- nginx proxies `/api/` to `http://backend:8080`
- this works in Docker Compose because `backend` is a Docker service name

Problem:
- that proxy target does not exist in a normal Render split deployment unless frontend and backend are redesigned around that topology

Impact:
- frontend hosting is easy, but the current proxy assumption does not transfer directly

### 3.4 ZIP Export Jobs Are Not Durable In A Hosted Ephemeral Environment

Relevant file:
- [ExportJobService.java](C:\Users\mohibul\Documents\rent\backend\src\main\java\com\renteasebd\export\ExportJobService.java)

Current state:
- export ZIPs are written to temp files
- file paths are stored in an in-memory `ConcurrentHashMap`

Problems:
- temp files can disappear on restart/redeploy
- in-memory job artifact mapping disappears on restart
- completed job metadata in DB does not guarantee the file still exists

Impact:
- export downloads are the clearest runtime fragility for Render
- a user can see `COMPLETED` in DB but still fail to download if the instance restarted

### 3.5 Bangla PDF Fallback Exists, But Embedded Bangla Font Is Missing

Relevant files:
- [PdfService.java](C:\Users\mohibul\Documents\rent\backend\src\main\java\com\renteasebd\export\PdfService.java)
- `backend/src/main/resources/fonts/`

Current state:
- PDF generation uses Apache PDFBox
- Bangla mode attempts to load `fonts/NotoSansBengali-Regular.ttf`
- the `fonts` directory is present but empty

Problem:
- the spec says Bangla font should be embedded with fallback behavior
- actual behavior will likely be English fallback because the Bengali font file is missing

Impact:
- PDF generation itself should still work on Render
- Bangla PDF output is not fully implemented as intended

### 3.6 CORS Defaults Are Localhost-Only

Relevant file:
- [application.yml](C:\Users\mohibul\Documents\rent\backend\src\main\resources\application.yml)

Current state:
- allowed origins are localhost variants only

Problem:
- hosted frontend origins must be explicitly configured

Impact:
- deployed frontend will fail API access until env vars are set correctly

### 3.7 Production Secrets Are Mandatory And Not Optional

Relevant file:
- [backend/README.md](C:\Users\mohibul\Documents\rent\backend\README.md)

Required for production:
- `JWT_PRIVATE_KEY_B64`
- `JWT_PUBLIC_KEY_B64`
- `PII_AES256_KEY_B64`
- `JWT_ALLOW_EPHEMERAL_KEY_PAIR=false`

Problem:
- deployment is not one-click; secure environment setup is mandatory

Impact:
- misconfiguration here breaks auth or weakens security posture

## 4. Mobile And Device-Access Problems

### 4.1 Web App Is Mobile-Usable, But Not Yet Mobile-Optimized Enough To Call Finished

Relevant files:
- [frontend/src/App.vue](C:\Users\mohibul\Documents\rent\frontend\src\App.vue)
- [frontend/src/styles.css](C:\Users\mohibul\Documents\rent\frontend\src\styles.css)
- various responsive view files under `frontend/src/views/`

Current state:
- there is responsive CSS
- main navigation collapses better at smaller widths
- several views have media-query support

Remaining issue:
- mobile browser use appears feasible, but there is no evidence in docs or code of full mobile UX validation across dense admin screens

Impact:
- usable on phone browser, but not fully de-risked

### 4.2 No PWA Installability Layer

Current state:
- no manifest/service worker/PWA setup was found

Problem:
- web app can be opened on mobile, but it is not installable as a strong app-like experience

Impact:
- mobile convenience is lower than it could be

### 4.3 Android App Still Points To Localhost By Default

Relevant files:
- [android-app/app/build.gradle.kts](C:\Users\mohibul\Documents\rent\android-app\app\build.gradle.kts)
- [android-app/app/src/main/AndroidManifest.xml](C:\Users\mohibul\Documents\rent\android-app\app\src\main\AndroidManifest.xml)

Current state:
- `API_BASE_URL` points to `http://127.0.0.1:8080/api/v1/`
- cleartext traffic is enabled

Problem:
- the Android app is not production-configured for a hosted HTTPS backend

Impact:
- mobile app support exists in code, but not yet in deployable production configuration

## 5. PDF And Export-Specific Problems

### 5.1 PDF Generation Is Not The Deployment Blocker

Relevant file:
- [PdfService.java](C:\Users\mohibul\Documents\rent\backend\src\main\java\com\renteasebd\export\PdfService.java)

Current state:
- PDF generation is pure server-side Java via PDFBox
- no headless Chrome, Puppeteer, or Playwright dependency

Conclusion:
- Render Hobby should be able to run PDF generation

Actual PDF problem:
- Bangla font packaging is incomplete

### 5.2 Export Workflow Does Not Match The Durability Expected By Ops Docs

Sources:
- [docs/runbooks/deployment.md](C:\Users\mohibul\Documents\rent\docs\runbooks\deployment.md)
- [spec.md](C:\Users\mohibul\Documents\rent\spec.md)

Expectation in docs:
- export endpoints are part of post-deploy verification
- async export job states should be stable

Implementation issue:
- the file artifact behind a completed export job is not durable

Impact:
- the state machine may look correct while actual download behavior is unreliable after restart

## 6. Render Hobby Fit Assessment

### 6.1 What Fits Well

- frontend as a hosted web frontend
- backend as a Java web service
- server-side PDF generation
- API-based mobile/browser usage

### 6.2 What Does Not Fit Cleanly Without Changes

- MySQL-first backend design
- Compose-specific frontend proxying
- temp-file-based ZIP exports
- production Android defaults

### 6.3 Static Frontend Clarification

Important clarification from the deployment analysis:
- a static frontend does not mean admin features stop working
- the admin UI is already a client-side SPA that talks to the backend API
- login and write operations still work when frontend assets are hosted statically

The real issue is not "admin versus static".
The real issue is:
- how the frontend reaches the backend
- how CORS is configured
- whether the app needs server-side rendering or custom middleware

No current evidence suggests the frontend needs SSR.

## 7. Prioritized Problem List

### Critical

1. Database hosting mismatch:
- current app expects MySQL
- Render is not a natural managed-MySQL target

2. Export durability:
- temp-file + in-memory export artifacts are not safe for hosted ephemeral containers

### High

3. Production hardening gaps still open in project docs:
- audit coverage
- masking policy
- DB-level invariants
- perf/security/WCAG completion

4. Backend hosting assumptions need patching:
- `PORT`
- CORS
- public invoice base URL

5. Bangla PDF is incomplete:
- fallback exists, embedded Bengali font asset does not

### Medium

6. Frontend deployment assumptions are still Docker Compose-oriented

7. Android app is not yet production-hosted by default

8. Mobile browser support exists, but no formal mobile UX hardening evidence was found

### Low

9. No PWA support yet

## 8. Recommended Resolution Order

1. Decide database path:
- migrate to Postgres, or
- choose an external MySQL provider

2. Patch backend hosting assumptions:
- use `PORT`
- set hosted CORS origins
- set `PUBLIC_INVOICE_BASE_URL`

3. Redesign export artifact handling:
- stream ZIP directly, or
- store artifact in object storage, or
- store bytes durably for small jobs

4. Add the missing Bengali font asset if Bangla PDF is required in production

5. Close quality-hardening items already called out in repo docs

6. Repoint Android production config to hosted HTTPS backend

7. Optionally add PWA support and additional mobile UX tuning

## 9. Bottom Line

The project is deployable with changes, but not production-clean on Render Hobby as-is.

The biggest real problems are not PDF generation and not admin use of the frontend.
They are:
- database fit
- export durability
- missing production-hardening closures
- mobile and Android still being partially local-development-oriented
