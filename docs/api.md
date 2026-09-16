# API Outline & Payloads

> All endpoints below assume `/api/v1` prefix and JWT auth for organizer/admin roles.

## Auth
### `POST /auth/login`
Request:
```json
{ "username": "organizer1", "password": "***" }
```
Response:
```json
{ "access_token": "...", "refresh_token": "...", "expires_in": 3600 }
```

### `POST /auth/register`
```json
{ "username": "organizer1", "email": "team@org.com", "password": "***" }
```

---

## Users + Cards
### `GET /users`
Query params: `page`, `limit`, `active`.

### `POST /users`
```json
{ "name": "Ada", "email": "ada@example.com", "phone": "+123", "token_balance": 10 }
```

### `PUT /users/{id}`
```json
{ "name": "Ada Lovelace", "is_active": true }
```

### `POST /cards/link`
```json
{ "user_id": "uuid", "card_uid": "04a224..." }
```

### `GET /cards/{uid}`
Response:
```json
{ "card_uid": "04a224...", "user": { "id": "uuid", "name": "Ada" } }
```

---

## Meals
### `POST /meals/swipe`
```json
{ "card_uid": "04a224...", "meal_type": "breakfast", "timestamp": "2024-02-01T08:30:00Z" }
```

### `GET /meals/user/{userId}`
### `GET /meals/today`
### `GET /meals/statistics?range=weekly`

---

## Transactions
### `POST /transactions/deduct`
```json
{ "card_uid": "04a224...", "amount": 2.5, "description": "Snack" }
```

### `POST /transactions/recharge`
```json
{ "card_uid": "04a224...", "amount": 20, "description": "Top up" }
```

### `GET /balance/{userId}`

---

## Admin + Reports
### `GET /dashboard/overview`
### `GET /reports/meals?from=2024-02-01&to=2024-02-07`
### `GET /reports/transactions?from=2024-02-01&to=2024-02-07`
### `GET /reports/export?type=csv&entity=meals`
### `PUT /settings/meal-windows`
```json
{
  "breakfast": { "start": "06:00", "end": "10:00" },
  "lunch": { "start": "11:30", "end": "14:30" }
}
```

---

## Access Control (Halls/Committees)
### `POST /access/halls`
```json
{ "name": "Main Hall", "capacity_threshold": 100, "allowed_roles": ["admin", "organizer", "user"] }
```
### `GET /access/halls`
### `PUT /access/halls/{hall_id}`
### `GET /access/halls/{hall_id}/presence`
Returns a list of user IDs currently present in the hall.

### `POST /access/scan`
```json
{ "card_uid": "04a224...", "hall_id": "uuid", "action": "entry" }
```
Response:
```json
{ "id": "uuid", "user_id": "uuid", "hall_id": "uuid", "action": "entry", "timestamp": "...", "allowed": true, "reason": null }
```

---

## Teams & Notifications
### `POST /teams`
```json
{ "name": "The Board", "capacity": 10 }
```
### `GET /teams`
### `PUT /teams/{team_id}`
### `POST /teams/{team_id}/members/{user_id}?role=leader`
Assigns a user to a team with a specific role (`leader` or `member`). Enforces capacity.

### `POST /teams/{team_id}/notifications?sender_id={uuid}`
```json
{ "message": "Emergency meeting in Main Hall in 5 mins!" }
```
(Only Team Leaders can broadcast to their team).

### `GET /teams/{team_id}/notifications`
Returns a list of notifications broadcasted to the team.
