# RentEase BD

Monorepo scaffold for RentEase BD v1.0.

## Layout
- `backend/` Spring Boot API + Flyway migrations
- `frontend/` Vue 3 web app
- `android-app/` Android app for property, tenant, invoice, and SMS workflows
- `docs/` runbooks and synthesis notes

## Quick Start (Local)
1. Backend: `cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'`
2. Frontend: `cd frontend && npm install && npm run dev`

## Docker Compose Deployment (PostgreSQL)
1. Copy env template:
   - `cp .env.production.example .env`
2. Fill required secrets in `.env`:
   - `JWT_PRIVATE_KEY_B64`
   - `JWT_PUBLIC_KEY_B64`
   - `PII_AES256_KEY_B64`
   - `POSTGRES_PASSWORD`
3. Start stack:
   - `docker compose up --build -d`
4. Open `http://localhost:${FRONTEND_PORT:-5175}`

Useful commands:
- Stop stack: `docker compose down`
- Stop and remove DB volume: `docker compose down -v`

## Render Deployment (Recommended)
The repo now includes a Render blueprint at [`render.yaml`](C:/Users/mohibul/Documents/rent/render.yaml).

1. In Render, create a new Blueprint service from this repository.
2. Render will create:
   - `rentease-postgres` managed PostgreSQL
   - `rentease-backend` (Docker web service)
   - `rentease-frontend` (Docker web service with Nginx reverse proxy to backend)
3. Before first successful deploy, set backend secrets in Render:
   - `JWT_PRIVATE_KEY_B64`
   - `JWT_PUBLIC_KEY_B64`
   - `PII_AES256_KEY_B64`
   - `CORS_ALLOWED_ORIGINS` (comma-separated, include your frontend/custom domain)
   - `PUBLIC_INVOICE_BASE_URL` (backend public HTTPS URL)
4. Keep `JWT_ALLOW_EPHEMERAL_KEY_PAIR=false` in production.

Notes:
- Backend accepts Render `DATABASE_URL` and auto-normalizes it to JDBC Postgres.
- Frontend proxies `/api/*` to backend over Render private networking (`API_UPSTREAM`).

### Key Generation Helpers
Generate RSA JWT keys:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt_private.pem
openssl rsa -pubout -in jwt_private.pem -out jwt_public.pem
base64 -w0 jwt_private.pem
base64 -w0 jwt_public.pem
```

Generate 32-byte AES key:

```bash
openssl rand -base64 32
```

### Bootstrap Owner (One-Time)
- Set `APP_BOOTSTRAP_ENABLED=true` with desired admin email/password for first deploy.
- After first successful login, set `APP_BOOTSTRAP_ENABLED=false` and redeploy.

## Docker Compose (Development)
Fast local setup with hot-reload frontend and backend `local` profile (H2):

1. `docker compose -f docker-compose.dev.yml up`
2. Open `http://localhost:5173`
3. API is available at `http://localhost:8080`
4. Login (default): `owner@rentease.bd` / `Owner@123`

## Notes
- Timezone baseline: `Asia/Dhaka`
- Currency baseline: BDT only
- API base path: `/api/v1`
