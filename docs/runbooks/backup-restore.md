# Backup and Restore Runbook

## Backup Policy
- Daily full backups
- Transaction log/binlog retention for PITR window

## Backup Procedure
1. Verify DB connectivity and free storage.
2. Run full backup job.
3. Store backup in designated secure location.
4. Record backup timestamp and checksum.

## Restore Drill Procedure
1. Restore backup to isolated environment.
2. Start backend against restored DB.
3. Execute smoke tests:
- Auth login
- Tenant list
- Invoice list
- Dashboard summary
4. Record recovery time and issues.
