# Rollback Runbook

## Trigger Conditions
- Critical API errors after deploy
- Auth/session failure across users
- Data integrity risks

## Steps
1. Stop traffic to the new version.
2. Re-deploy last known good backend and frontend versions.
3. Validate core health and smoke checks.
4. If migration introduced incompatibility, execute approved rollback script or hotfix migration.
5. Announce rollback completion and open incident report.

## Validation
- `GET /api/v1/system/ping` returns success.
- Login, tenant listing, invoice listing, dashboard endpoints return success.
