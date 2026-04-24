# Deployment Runbook

## Target
Render deployment using:
- Managed PostgreSQL
- Backend Docker web service
- Frontend Docker web service (Nginx reverse proxy to backend)

Blueprint file: [`render.yaml`](C:/Users/mohibul/Documents/rent/render.yaml)

## Preconditions
- Backend tests pass: `cd backend && ./gradlew test`
- Frontend build passes: `cd frontend && npm run build`
- Production secrets prepared:
  - `JWT_PRIVATE_KEY_B64`
  - `JWT_PUBLIC_KEY_B64`
  - `PII_AES256_KEY_B64`

## Render Setup
1. Create a Blueprint deployment from repository root.
2. Confirm resources created:
   - `rentease-postgres`
   - `rentease-backend`
   - `rentease-frontend`
3. Set required backend env vars in Render dashboard:
   - `JWT_PRIVATE_KEY_B64`
   - `JWT_PUBLIC_KEY_B64`
   - `PII_AES256_KEY_B64`
   - `CORS_ALLOWED_ORIGINS`
   - `PUBLIC_INVOICE_BASE_URL`
4. Verify:
   - `JWT_ALLOW_EPHEMERAL_KEY_PAIR=false`
   - `APP_BOOTSTRAP_ENABLED=false` after initial owner bootstrap

## Deployment Validation
1. Backend health:
   - `GET /actuator/health`
   - `GET /api/v1/system/ping`
2. Auth flow:
   - login
   - refresh token
   - logout session
3. Core API flow:
   - tenant list
   - invoice list
   - dashboard summary
4. Export flow:
   - create ZIP export job
   - poll completion
   - download artifact
5. PDF flow:
   - generate invoice/deposit PDF

## Security Verification
- Frontend served over HTTPS.
- Backend receives forwarded HTTPS headers and emits HSTS.
- No ephemeral JWT key mode in production.
- CORS only allows approved frontend origins.

## Rollback
1. Roll back backend to previous successful Render deploy.
2. Roll back frontend to previous successful Render deploy.
3. Restore PostgreSQL from Render backup/snapshot if schema/data rollback is required.
