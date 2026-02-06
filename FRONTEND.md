# Frontend integration guide (Phase 10 — optional)

Backend is **backend-first ready**. Use this when you build the Angular (or any) frontend.

---

## Angular 17 scaffold (production-ready)

A full Angular 17 + Tailwind frontend lives in **`frontend/`**. Steps:

### 1. Install and run locally

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200**. Ensure the backend is running at **http://localhost:8081**.

### 2. Build for production

```bash
cd frontend
npm run build
```

Output: `dist/money-tracker-ui/browser/`. Set your API base URL via environment (see below).

### 3. Deploy (Netlify / Vercel)

- **Netlify:** Connect repo, build command `cd frontend && npm install && npm run build`, publish directory `frontend/dist/money-tracker-ui/browser`. Add env var `API_URL` (e.g. your deployed backend URL) and use it in `environment.production.ts` or replace at build time.
- **Vercel:** Same idea; build command `cd frontend && npm ci && npm run build`, output directory `frontend/dist/money-tracker-ui/browser`.

### 4. API base URL

- **Local:** `http://localhost:8081` (in `src/environments/environment.ts`).
- **Production:** Set in `environment.production.ts` or via build-time replacement (e.g. `API_URL` env in Netlify/Vercel). The app reads `environment.apiUrl` for all API calls.

---

## Base URL & auth

- **Base URL**: `http://localhost:8081` (or your deployed URL)
- **Auth**: JWT in header on every request except register/login:
  ```http
  Authorization: Bearer <token>
  ```
- **CORS**: Allowed for `http://localhost:3000` and `http://localhost:8080` (configurable in `application.yml`).

---

## Auth

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/api/auth/register` | `{ "email": "...", "password": "..." }` | `{ userId, email, token }` |
| POST | `/api/auth/login` | `{ "email": "...", "password": "..." }` | `{ userId, email, token }` |
| GET | `/api/auth/me` | — | `{ userId, email, token: null }` (requires `Authorization`) |

Store `token` and send it on all other API calls.

---

## Wallets & balance

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/api/wallets` | `{ "type": "CASH" \| "ONLINE" }` | `{ id, type, balance }` |
| GET | `/api/wallets` | — | `[{ id, type, balance }, ...]` |
| GET | `/api/wallets/balance` | — | `{ cash, online, total }` (BigDecimal) |

Create **CASH** and **ONLINE** wallets once per user; then use them in transactions.

---

## People (ledger contacts)

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/api/people` | `{ "name": "...", "phone": "...", "notes": "..." }` | Person summary (id, name, totalReceived, totalGiven, netBalance, status) |
| GET | `/api/people` | — | `[PersonSummaryResponse, ...]` |
| GET | `/api/people/{id}` | — | Person summary |
| GET | `/api/people/{id}/ledger` | — | Same as `GET /api/people/{id}` (totalReceived, totalGiven, netBalance, status) |

**Person summary**: `id`, `name`, `phone`, `notes`, `totalReceived`, `totalGiven`, `netBalance`, `status` (`THEY_OWE_ME` \| `I_OWE_THEM` \| `SETTLED`).

---

## Transactions

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/api/transactions` | See below | `TransactionResponse` |
| POST | `/api/transactions/{id}/reverse` | — | New reversal transaction |
| GET | `/api/transactions` | Query: `?wallet=CASH|ONLINE&type=...&from=YYYY-MM-DD&to=YYYY-MM-DD` | `[TransactionResponse, ...]` |

**Create transaction body** (all amounts positive, BigDecimal):

- **RECEIVED**: `personId`, `toWalletId`; no `fromWalletId`
- **GIVEN**: `personId`, `fromWalletId`; no `toWalletId`
- **EXPENSE**: `fromWalletId`; no `personId`, no `toWalletId`
- **INCOME**: `toWalletId`; no `personId`, no `fromWalletId`
- **TRANSFER**: `fromWalletId`, `toWalletId` (different); no `personId`

Optional: `description`, `date` (ISO-8601).

**TransactionResponse**: `id`, `personId`, `fromWalletId`, `toWalletId`, `amount`, `transactionType`, `description`, `date`, `createdAt`.

---

## Error responses

All errors use this shape (see `ErrorResponse`):

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad request",
  "message": "...",
  "path": "/api/...",
  "details": ["field: message"]
}
```

Common statuses: **400** validation/bad request, **401** invalid/missing token, **403** forbidden (not your resource), **404** not found, **409** conflict (e.g. duplicate wallet), **429** rate limit (auth endpoints).

---

## Suggested Angular structure (Phase 10)

1. **Dashboard**
   - Call `GET /api/wallets/balance` and `GET /api/wallets`; show **CASH**, **ONLINE**, **TOTAL**.
   - Optional: recent transactions `GET /api/transactions?from=...&to=...`.

2. **Ledger view**
   - `GET /api/people` → list contacts with `netBalance` and `status`.
   - Drill into `GET /api/people/{id}/ledger` for one person’s totals and (later) transaction list per person.

3. **Transfer UI**
   - Form: type = **TRANSFER**, `fromWalletId`, `toWalletId`, `amount` (and optional description/date).
   - POST `/api/transactions`; then refresh balance or dashboard.

4. **Auth**
   - Login/register forms → store token; use an HTTP interceptor to add `Authorization: Bearer <token>` and handle **401** (e.g. redirect to login).

5. **Wallets & people**
   - Ensure CASH/ONLINE wallets exist (POST if needed); manage people (create/list) for RECEIVED/GIVEN flows.

You can implement backend-first and add this Angular (or other) UI when ready.
