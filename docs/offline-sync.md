# Offline Sync Strategy

## Goals
- Allow organizers to continue NFC scanning without connectivity.
- Ensure no data loss for meal swipes and POS transactions.
- Resolve conflicts with deterministic, auditable rules.

## Client Strategy (Android)
1. **Local Queue**
   - Store transactions and meal swipes in Room.
   - Mark entries as `pending`.

2. **Connectivity Listener**
   - Trigger sync when network restores.

3. **Retry & Backoff**
   - Exponential backoff on server errors.

4. **Conflict Resolution**
   - Use server timestamp + idempotency key.
   - Client sends `request_id` to avoid duplicates.

## Server Strategy
- **Idempotency Keys**: Persist `request_id` with each operation.
- **Atomic Transactions**: Use DB transactions for balance updates.
- **Audit Log**: Record raw requests and outcomes for reconciliation.

