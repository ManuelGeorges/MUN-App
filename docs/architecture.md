# Architecture & Recommendations

## System Overview
The solution consists of three primary layers:

1. **Android App (Organizer-facing)**
   - NFC reading for card UID
   - Offline-first data capture and queueing
   - Sync with backend API

2. **Backend API**
   - Auth + RBAC (organizer/admin)
   - Meal tracking business rules
   - POS transactions
   - Admin reporting and export

3. **Database + Cache**
   - PostgreSQL for source-of-truth
   - Redis for caching, rate limiting, and queue coordination

---

## Backend Framework Recommendation
**FastAPI + SQLModel + Alembic** is recommended because it:
- Provides first-class OpenAPI documentation.
- Supports async for NFC-heavy, concurrent operations.
- Enables strong typing for request validation.
- Integrates cleanly with PostgreSQL and Redis.

**Alternative:** Node.js/Express is viable if the team already uses JS/TS and wants shared language between frontend/backend.

---

## Database Choice: SQL vs NoSQL
**SQL (PostgreSQL)** is strongly preferred because:
- Transaction integrity is essential for balances and meal limits.
- Analytics and reporting queries are easier with relational models.
- Strict constraints help enforce one-meal-per-period rules.

---

## Real-time Communication
**WebSockets** are recommended for:
- Live dashboard updates.
- Balance change notifications.
- Transaction processing feedback.

Polling is simpler but more bandwidth-heavy and less responsive at scale.

---

## File Storage for Reports
**Cloud object storage** (S3/GCS/Azure Blob) is preferred for:
- Generated CSV/PDF exports
- Versioned files and longer retention
- Reduced load on application servers

---

## Caching Strategy
**Redis** is recommended for:
- Rate limiting
- Short-lived token caches
- Dashboard stats cache
- Offline sync queues

---

## API Versioning Strategy
Use **URI versioning** (e.g., `/api/v1/...`) plus clear deprecation windows. For major schema changes, add `/api/v2` while maintaining old versions for a defined timeline.

---

## Testing Strategy
1. **Unit Tests**
   - Business logic for meal limits and balances
2. **Integration Tests**
   - API endpoints + database interactions
3. **E2E Tests**
   - Full flows (registration → meal swipe → POS)

Recommended tooling:
- **Backend:** Pytest + Testcontainers + HTTPX
- **Mobile:** JUnit + MockWebServer + Espresso

