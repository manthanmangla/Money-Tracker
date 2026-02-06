# Money Tracker — Angular 17 Frontend

Production-ready Angular 17 app (standalone components, Tailwind CSS) connected to the Spring Boot API.

---

## Step-by-step: run locally

1. **Backend**
   - Start the API: from repo root run `mvn spring-boot:run` (or your usual way).
   - API must be at **http://localhost:8081** (CORS is allowed for http://localhost:4200).

2. **Frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   - App runs at **http://localhost:4200**.

3. **Use the app**
   - **Register** or **Login** (JWT is stored in `localStorage`).
   - **Dashboard**: total / cash / online balance.
   - **Wallets**: list wallets, create CASH/ONLINE, transfer between wallets.
   - **People**: add people, view ledger (received / given / net).
   - **Transactions**: create (RECEIVED, GIVEN, EXPENSE, INCOME, TRANSFER), list with filters.

---

## Project structure

```
src/app/
  auth/           login, register
  dashboard/      balance cards
  wallets/        list, create, transfer
  people/         list, create, ledger table
  transactions/   create form, list + filters
  core/
    guards/       auth.guard
    interceptors/ auth.interceptor (JWT header + 401 → login)
    services/     auth, wallet, people, transaction
```

- **JWT**: stored in `localStorage`; `Authorization: Bearer <token>` added by interceptor.
- **Env**: `src/environments/environment.ts` (dev) and `environment.production.ts` (prod). Set `apiUrl` to your API base URL.

---

## Build

```bash
npm run build
```

- Output: **dist/money-tracker-ui/browser/** (Angular 17 application builder).

---

## Deploy (Netlify / Vercel)

1. **Build**
   - Build command: `npm install && npm run build`
   - Publish directory: **dist/money-tracker-ui/browser**

2. **API base URL**
   - Set your deployed API URL in **src/environments/environment.production.ts** (`apiUrl`) before building, **or**
   - Use your platform’s build-time env (e.g. Netlify: add `NG_APP_API_URL` and replace in a build script if needed). Default production file uses `http://localhost:8081`; change it to your live API URL for deploy.

3. **Backend CORS**
   - Ensure the backend allows your frontend origin (e.g. `https://your-app.netlify.app`). In Spring Boot `application.yml`, add it under `security.cors.allowed-origins`.

---

## Tech

- Angular 17, standalone components, reactive forms
- Tailwind CSS
- JWT in localStorage, interceptor, auth guard
- No mocks — all calls go to the real backend at `environment.apiUrl`
