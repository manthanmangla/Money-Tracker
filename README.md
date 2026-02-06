# Money Tracker API

Ledger-based money tracker: dual wallet (Cash & Online), person-wise debt, internal transfers, reversals. JWT auth, ownership checks, OpenAPI docs. 

---

## Architecture

```mermaid
flowchart TB
    subgraph Client
        UI[Frontend / API client]
    end

    subgraph API["Money Tracker API (Spring Boot)"]
        Auth[Auth: register, login, JWT]
        Wallets[Wallets: CASH / ONLINE, balance]
        People[People: ledger contacts]
        Tx[Transactions: RECEIVED, GIVEN, EXPENSE, INCOME, TRANSFER]
        Reversal[Reversals: immutable undo]
    end

    subgraph Security
        JWT[JWT filter]
        CORS[CORS]
        RateLimit[Rate limit auth]
        Ownership[Ownership checks]
    end

    subgraph Data
        PG[(PostgreSQL)]
    end

    UI --> Auth
    UI --> Wallets
    UI --> People
    UI --> Tx
    Auth --> JWT
    Wallets --> Ownership
    People --> Ownership
    Tx --> Ownership
    Reversal --> Tx
    Auth --> RateLimit
    API --> CORS
    Wallets --> PG
    People --> PG
    Tx --> PG
```

- **Layers**: Auth (JWT), Wallets (CASH/ONLINE + balance), People (ledger contacts), Transactions (by type + reversals).
- **Security**: JWT on all `/api/*` except register/login; CORS; optional rate limit on auth; every resource scoped by `user_id` (403 if not owner).
- **Data**: PostgreSQL; JPA/Hibernate; no deletes, reversals only.

---

## Tech stack

- **Java 17**, **Spring Boot 3** (Web, Data JPA, Security, Validation)
- **PostgreSQL**
- **JWT** (jjwt), **BCrypt**, **SpringDoc OpenAPI** (Swagger UI)
- **Docker** (multi-stage), **docker-compose** (app + Postgres)

---

## Prerequisites

- Java 17+, Maven, PostgreSQL (or use Docker only)

---

## Run locally

1. **Database**

   ```bash
   createdb money_tracker
   createuser money_tracker -P   # set password
   # Grant: GRANT ALL PRIVILEGES ON DATABASE money_tracker TO money_tracker;
   ```

2. **Env** (optional; defaults in `application.yml`)

   ```bash
   export DB_USERNAME=money_tracker
   export DB_PASSWORD=your_password
   export JWT_SECRET=your-base64-or-plain-secret
   ```

3. **Start**

   ```bash
   mvn spring-boot:run
   ```

   API: **http://localhost:8081**

---

## Public API docs (Swagger)

- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

Docs are unauthenticated; use **Authorize** in Swagger UI with `Bearer <token>` (from `/api/auth/login` or `/api/auth/register`) to call protected endpoints.

---

## Docker

**Run app + Postgres:**

```bash
docker-compose up -d
```

API: **http://localhost:8081**. Set `JWT_SECRET` in env or in `docker-compose.yml` for production.

**Build image only:**

```bash
docker build -t money-tracker .
docker run -p 8081:8081 -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/money_tracker -e JWT_SECRET=secret money-tracker
```

---

## Deploy

### Render

1. Connect repo; use **Blueprint** and select `render.yaml` (creates Web Service + PostgreSQL), or create **Web Service** and **PostgreSQL** manually.
2. **Web Service**: Runtime **Docker**, Dockerfile path `./Dockerfile`. Render assigns `PORT`; app uses `server.port=${PORT:8081}`.
3. **Env vars** (if not using Blueprint env, or if Blueprint sets `postgres://`):
   - `SPRING_DATASOURCE_URL` = JDBC URL (e.g. `jdbc:postgresql://host:port/money_tracker`). If Render gives `postgres://user:pass@host:port/db`, use `jdbc:postgresql://host:port/db` and set username/password separately.
   - `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` = DB user/password.
   - `JWT_SECRET` = strong secret (use Render **Secret**).
4. Health check path: `/v3/api-docs` (or `/swagger-ui.html`).

**Deployed URL**: e.g. `https://money-tracker-api.onrender.com` → **Public API docs** at `https://<your-app>.onrender.com/swagger-ui.html`.

### AWS (options)

- **App Runner** or **ECS (Fargate)**: Use the same Dockerfile; set env (e.g. RDS JDBC URL, `JWT_SECRET`) in task definition.
- **Elastic Beanstalk**: Deploy JAR or Docker; configure RDS and env vars in console.
- **RDS PostgreSQL**: Create DB; set `SPRING_DATASOURCE_*` and `JWT_SECRET` in the compute service.

---

## Env vars summary

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port (Render sets this) | 8081 |
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL | money_tracker |
| `JWT_SECRET` | JWT signing secret | dev value (change in prod) |
| `RATE_LIMIT_ENABLED` | Rate limit auth endpoints | true |
| `RATE_LIMIT_AUTH_PER_MINUTE` | Requests per IP per minute | 10 |

---

## Resume line

*Designed and deployed a ledger-based Money Tracker API with dual-wallet (Cash & Online) accounting, person-wise debt tracking, immutable transactions with reversals, JWT auth, ownership hardening, OpenAPI docs, and Docker/Render deployment using Spring Boot and PostgreSQL.*
