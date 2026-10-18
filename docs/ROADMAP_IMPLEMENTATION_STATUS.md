# Society Ledger roadmap implementation status

This document tracks the finalized product direction against the code, not a claim that every feature is already implemented.

## Product guardrails
- Complete society management for committee and residents with different permissions.
- Initial payment scope is manual recording of payments made externally/offline. Do not add a payment gateway for maintenance dues.
- Preserve existing donations, expenses, member records, and audit data during database upgrades.
- Do not expose one resident's private dues or contact details to another resident.
- Mark features as implemented only after UI, persistence, permission, and regression tests pass.

## Existing code observed during the first audit
- Local Room entities and screens exist for members, donations, expenses, transactions, pledges, and audit logs.
- Member records already include a flat number.
- Expenses and financial reports exist.
- The current app remains donation-ledger oriented; a dedicated maintenance assessment ledger was missing from the inspected data model.

## Changes in this branch
- Added a separate `MaintenanceCharge` Room entity. A charge tracks flat, resident, billing period, amount, due date, paid amount, payment mode/reference, notes, and recording metadata.
- Added a DAO for listing charges, filtering by flat or billing period, and calculating assessed/outstanding amounts.
- Added repository validation for YYYY-MM billing periods, positive finite charge amounts, bounded paid amounts, and one charge per flat per period.
- Exposed maintenance charges and add/update operations from `SocietyViewModel`.
- Added Room migration 5 → 6 that creates the new table without deleting existing ledger tables or records.

## Still required before this can be considered a user-facing feature
- Add a Maintenance Dues screen and navigation entry.
- Add committee-only charge creation and payment-recording UI; resident view must be restricted to their own flat.
- Add a clear role/authorization model based on the signed-in identity; local PIN state alone is not sufficient for multi-user privacy.
- Add tests for migration from a real v5 schema, duplicate prevention, billing period validation, partial/full payment status, and resident isolation.
- Review the existing `fallbackToDestructiveMigration()` policy. Although migration 5 → 6 is supplied here, other unsupported schema upgrades can still erase local data.
- Verify Firestore synchronization and audit-log integration for maintenance records before relying on cross-device data.
- Complete resident dashboard, notices/reminders/statements, advanced reports, exports, and backup/reconciliation work in later phases.

## Release rule
Do not publish or merge this branch as a complete implementation until the UI and security gaps above are addressed and CI passes. This is an incremental data-layer foundation.
