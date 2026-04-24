# Backend

Spring Boot backend for RentEase BD.

## Run
- Local profile (H2): `./gradlew bootRun --args='--spring.profiles.active=local'`

## Database
- Default/prod profile uses PostgreSQL.
- `DATABASE_URL` in Render format (`postgres://...` / `postgresql://...`) is auto-converted to JDBC at startup when `SPRING_DATASOURCE_URL` is not set.

## Required Environment Variables (prod/default)
- `JWT_PRIVATE_KEY_B64`: Base64-encoded PKCS8 RSA private key
- `JWT_PUBLIC_KEY_B64`: Base64-encoded X509 RSA public key
- `PII_AES256_KEY_B64`: Base64-encoded 32-byte AES key
- `JWT_ALLOW_EPHEMERAL_KEY_PAIR=false` in production

## Recommended Production Variables
- `CORS_ALLOWED_ORIGINS`: comma-separated frontend origins
- `PUBLIC_INVOICE_BASE_URL`: backend HTTPS base URL used in invoice links
- `AUTH_RATE_LIMIT_MAX_ATTEMPTS`
- `AUTH_RATE_LIMIT_WINDOW_SECONDS`

## Profiles
- `default` / `prod`: PostgreSQL + Flyway migrations
- `local`: embedded H2 for local dev

## Security Notes
- App binds to `PORT` when provided (Render-compatible).
- Forwarded headers are honored (`server.forward-headers-strategy=framework`) for correct HTTPS detection behind proxy.
- HSTS and strict referrer policy headers are enabled in security config.
