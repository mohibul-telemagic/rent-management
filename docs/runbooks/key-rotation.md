# Encryption and JWT Key Rotation Runbook

## Scope
- `PII_AES256_KEY_B64`
- JWT key pair (`JWT_PRIVATE_KEY_B64`, `JWT_PUBLIC_KEY_B64`)

## Rotation Steps
1. Generate new keys in secure environment.
2. Update secret store for target environment.
3. Deploy backend with new key configuration.
4. Force refresh/login cycle if JWT public key changed.
5. Validate:
- Login
- Token refresh
- PII read/write paths

## Rollback
- Revert to previous key version from secret history.
- Re-deploy and re-validate auth + PII workflows.
