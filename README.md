# KostaBank

Banking application with a **Spring Boot** REST API and a **React** client. It handles
checking and savings accounts, deposits, withdrawals, transfers, bill payments and IRIS
transfers, with access decided by the user's role.

Final project — **Coding Factory 10**, Athens University of Economics and Business.

[![build](https://github.com/kostarasae/BankApp/actions/workflows/build.yml/badge.svg)](https://github.com/kostarasae/BankApp/actions/workflows/build.yml)

🔗 **Live:** https://bankapp-3cwp.onrender.com
📖 **API documentation (Swagger):** https://bankapp-3cwp.onrender.com/swagger-ui.html

> Hosted on Render's free tier — the first request after a period of inactivity takes
> ~30 seconds while the server wakes up.

**🇬🇷 Ελληνικά:** [Οδηγίες στα ελληνικά](#-ελληνικά)

---

## Contents

- [Build](#build)
- [Deploy](#deploy)
- [Architecture](#architecture)
- [Authentication & Authorization](#authentication--authorization)
- [Domain model](#domain-model)
- [Tests](#tests)
- [API](#api)
- [Configuration](#configuration)

---

## Build

### Prerequisites

| Tool | Version | Note |
|---|---|---|
| **JDK** | 21 | The Gradle wrapper downloads Gradle itself |
| **Docker** | any | For the database |
| ~~Node.js~~ | — | **Not needed.** Gradle downloads Node 20 automatically |

### The quick way — everything with one command

```bash
docker compose up
```

Starts **PostgreSQL and the application together**. Open **http://localhost:8080**.

The first build takes a few minutes: it compiles the backend and the React client
inside the image. You do not need to create any tables — **Flyway** runs migrations
`V1`–`V5` on first start and inserts seed data.

| Command | What it does |
|---|---|
| `docker compose up` | Build and start |
| `docker compose up -d` | Same, in the background |
| `docker compose down` | Stop |
| `docker compose down -v` | Stop **and delete** the database data |

### The development way — database in Docker, application locally

More convenient while writing code: no image rebuild on every change.

**Step 1 — Database**

```bash
docker compose up -d db
```

**Step 2 — Build and run**

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Open **http://localhost:8080**.

**What that command actually does** — the build is unified, backend and frontend
together:

```
./gradlew bootRun
  ├─ nodeSetup        downloads Node 20 (once)
  ├─ npmInstall       installs the bankApp/ dependencies
  ├─ buildReact       runs vite build → bankApp/dist
  ├─ compileJava      compiles the backend
  ├─ processResources copies resources
  ├─ copyReactDist    copies bankApp/dist into the static resources
  └─ bootRun          starts Spring Boot on :8080
```

The React client is **not** served by a separate server — it is packaged into the same
artifact and served by Spring Boot. One build, one deployable.

### Other commands

```bash
./gradlew build          # full build + tests → build/libs/restBankApp-0.0.1-SNAPSHOT.jar
./gradlew test           # tests only
./gradlew buildReact     # frontend only
java -jar build/libs/restBankApp-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Frontend development

For hot reload while working on React:

```bash
cd bankApp
npm install
npm run dev      # http://localhost:5173
```

Vite proxies `/api` to the backend, so run `bootRun` alongside it.

| Command | What it does |
|---|---|
| `npm run dev` | Dev server with hot reload |
| `npm run build` | Production build → `dist/` |
| `npm run typecheck` | TypeScript type check |
| `npm run lint` | ESLint |

### Test accounts

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin1234!` | Administrator |
| `maria` | `Test1234!` | Customer |
| `nikos` | `Test1234!` | Customer |

---

## Deploy

The application runs on **Render** as a Docker web service, with a managed PostgreSQL
instance.

### How the image is built

The [`Dockerfile`](Dockerfile) is **multi-stage**:

```
Stage 1 (eclipse-temurin:21-jdk)   ← build
  copies gradlew, gradle/, build.gradle, settings.gradle, src/, bankApp/
  runs ./gradlew build -x test
  → produces the jar, with the built React client inside it

Stage 2 (eclipse-temurin:21-jre-alpine)   ← runtime
  copies ONLY the jar from stage 1
  ENV SPRING_PROFILES_ACTIVE=pro   (docker-compose overrides this to dev locally)
  ENTRYPOINT: java -jar app.jar
```

The second stage keeps only the JRE and the jar — not the JDK, the sources or
`node_modules`. `.dockerignore` keeps `build/`, `node_modules/`, `docs/` and `.git/`
out of the build context.

Tests are skipped during the image build (`-x test`) so a deploy is not held up by them;
they run on every push through GitHub Actions instead — see
[`.github/workflows/build.yml`](.github/workflows/build.yml).

### Deploy steps on Render

1. **PostgreSQL:** New → PostgreSQL. Note the host, port, database, user and password.
2. **Web Service:** New → Web Service → connect the GitHub repo → Runtime **Docker**.
   Render picks up the `Dockerfile` on its own.
3. **Environment variables** (Settings → Environment):

   | Variable | Value |
   |---|---|
   | `APP_SECURITY_SECRET_KEY` | Base64 key, **≥32 bytes once decoded** |
   | `DB_HOST` | Internal host of the database |
   | `DB_PORT` | `5432` |
   | `DB_NAME` | Database name |
   | `DB_USER` | User |
   | `DB_PASSWORD` | Password |
   | `ALLOWED_ORIGINS` | The application URL, e.g. `https://bankapp-3cwp.onrender.com` |

   Generating a key:
   ```bash
   openssl rand -base64 48
   ```

4. **Deploy.** Every push to `main` triggers a new build automatically.

### What happens on first start

**Flyway** applies the migrations in order and records progress in the
`flyway_schema_history` table:

| Migration | What it does |
|---|---|
| `V1__schema.sql` | Tables, keys, indexes |
| `V2__static_data.sql` | Regions, roles, capabilities |
| `V3__seed_data.sql` | Seed users, accounts, transactions |
| `V4__account_number_sequence.sql` | Sequence for account numbers |
| `V5__unique_only_among_active_rows.sql` | Partial unique indexes (ignore deleted rows) |

⚠️ If a migration fails, **the application does not start**. `ddl-auto` is `validate`,
not `update` — Hibernate never changes the schema on its own.

### Verifying a deploy

```bash
curl -i https://<your-url>/                      # 200, the React index.html
curl -i https://<your-url>/swagger-ui.html       # 200
curl -X POST https://<your-url>/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin1234!"}'   # returns a token
```

Render's logs should show `Migrating schema "public"` and
`Started RestBankAppApplication`.

---

## Architecture

Layered, with dependencies always pointing inwards:

```
Controller   →  accepts HTTP, validates input, translates to DTOs
    ↓
Service      →  business logic + access control + transactions
    ↓
Repository   →  Spring Data JPA
    ↓
Entities     →  domain model
```

```
src/main/java/gr/aueb/cf/restbankapp/
├── api/            REST controllers + central error handling (@ControllerAdvice)
├── service/        Business logic — access control lives here
├── repository/     Spring Data JPA interfaces
├── model/          Entities (account inheritance, soft delete)
├── dto/            Java records with Bean Validation
├── security/       JWT filter, security config, ownership checks
├── validation/     Custom validators (duplicates, business rules)
├── mapper/         Entity ↔ DTO
└── core/           Exceptions, factories, OpenAPI configuration

src/main/resources/db/migration/   Flyway V1–V5
src/main/resources/static/legacy/  The original vanilla frontend (archived)
bankApp/                           React client
```

**Entities never leave the service layer** — controllers exchange DTOs only.

### Technologies

**Backend:** Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA ·
PostgreSQL 16 · Flyway · springdoc-openapi · Gradle
**Frontend:** React 19 · Vite · Tailwind CSS 4 · React Router 7 · Axios
**Infrastructure:** Docker (multi-stage) · Render

---

## Authentication & Authorization

**Authentication** uses **JWT**. `/api/v1/auth/authenticate` returns a token that
accompanies every subsequent call as `Authorization: Bearer <token>`.
`JwtAuthenticationFilter` validates it on each request.

**Authorization** happens at two levels:

1. **URL level** (`SecurityConfiguration`) — which endpoints are public.
2. **Method level** (`@PreAuthorize` on the **service**) — the substantive check.

Checks are written against **capabilities**, not roles:

| Role | Capabilities |
|---|---|
| **CUSTOMER** | `VIEW_ONLY_CUSTOMER`, `VIEW_ONLY_ACCOUNT`, `CAN_DEPOSIT`, `CAN_WITHDRAW`, `CAN_TRANSFER` |
| **EMPLOYEE** | `VIEW_CUSTOMERS`, `VIEW_CUSTOMER`, `INSERT_CUSTOMER`, `EDIT_CUSTOMER`, `VIEW_ACCOUNTS`, `VIEW_ACCOUNT`, `CREATE_ACCOUNT` |
| **ADMIN** | All of them |

The mapping lives in the database (`V2__static_data.sql`), not in code — a new
capability is added with a migration, without recompiling.

**A role alone is not enough for personal data.** IBANs travel with every transfer, so
they are not secret; **ownership** is checked instead:

```java
@PreAuthorize("hasAuthority('VIEW_ACCOUNT') or (hasAuthority('VIEW_ONLY_ACCOUNT') "
            + "and @securityService.isOwnAccount(#iban, authentication))")
```

Staff see everything; a customer sees only their own.

**On the frontend** authorization is mirrored: `AuthContext` holds the role,
`ProtectedRoute` guards routes, and tabs and actions appear accordingly. That is a
matter of user experience — **the decision is always made on the server**.

---

## Domain model

```
Region 1───N Customer 1───1 User N───1 Role N───N Capability
                │
                │ 1───1 PersonalInfo 1───1 Attachment (identity document)
                │
                └─ N───N Account ◄── AccountChecking / AccountSavings
                              │
                              └─ 1───N Transaction
```

**Design choices:**

- **Account inheritance** — `Account` is abstract, with `AccountChecking` and
  `AccountSavings` (single table + discriminator). The fee comes from a **strategy**,
  so a new account type needs no `if`.
- **Many owners per account** (many-to-many) — supports joint accounts. That is why
  closing an account does **not** delete its customers.
- **Soft delete everywhere** — nothing is lost; deleted records simply stop being
  visible. Partial unique indexes (V5) free the VAT/username for re-registration.
- **The statement always explains the balance** — every movement of money leaves an
  entry, including **fees** and the **incoming** side of a transfer.
- **Destructive actions have guards** — a customer with open accounts cannot be
  deleted, you cannot delete yourself, and the last administrator cannot be removed.

---

## Tests

```bash
./gradlew test
```

**Unit tests** (Mockito) for business logic — deposits, withdrawals, fee calculation,
validators.

**Security tests** that load the **real** service bean behind the method-security
proxy. A mocked bean carries no annotations, so it can never exercise `@PreAuthorize` —
these tests stand up a Spring context with `@EnableMethodSecurity` instead.

**Money integrity test** asserting that the sum of an account's transactions equals its
balance after a transfer — for the sender *and* the recipient.

**OpenAPI coverage test** that fails the build if any endpoint ships without a
description.

`contextLoads` runs against **H2 in-memory**, so it needs no PostgreSQL.

### Continuous integration

Every push to `main` runs the whole thing on a clean machine — the Gradle build with the
JUnit suite, then the Vitest suite and the linter. Nothing is carried over from a
developer's laptop, so a change that only builds locally fails here.

### Checking it by hand

Some things only a person can confirm. Against a running instance:

| Check | Expected |
|---|---|
| Log in as `admin`, then `maria`, then an employee | Each sees a different set of tabs and actions |
| Transfer between two customers | Amount and fee on the sender's statement, amount alone on the recipient's, both balances agreeing with their statements |
| Open a statement with more than 20 movements | It pages, and the dashboard chart still covers the whole period |
| Delete a customer who still has an open account | Refused until the account is closed |
| As an administrator, try to delete your own account | Refused |
| Press **Authorize** in Swagger UI, paste a token, call an endpoint | Works from the page |
| Refresh the browser on `/login` | Loads rather than returning 404 |

---

## API

All endpoints live under `/api/v1`. Full documentation in **Swagger UI**
(`/swagger-ui.html`) — press **Authorize**, paste a JWT, and try them from the page.

| Group | Endpoints |
|---|---|
| **Auth** | `POST /auth/authenticate` |
| **Accounts** | `POST /accounts` · `GET /accounts` · `GET /accounts/{iban}` · `DELETE /accounts/{iban}` · `POST /accounts/deposit` · `POST /accounts/withdraw` · `POST /accounts/transfer` · `GET /accounts/{iban}/transactions` *(paged)* · `GET /accounts/{iban}/fee` · `GET /accounts/{iban}/owner` · `GET /accounts/phone/{phone}` |
| **Customers** | `POST /customers` · `GET /customers` · `GET /customers/{uuid}` · `PUT /customers/{uuid}` · `DELETE /customers/{uuid}` · `GET /customers/{uuid}/accounts` · `POST /customers/{uuid}/id-file` · `PUT /customers/{uuid}/password` |
| **Users** | `POST /users` · `GET /users/staff` · `GET /users/{uuid}` · `DELETE /users/{uuid}` · `PUT /users/{uuid}/password` · `PUT /users/{uuid}/reset-password` |
| **Reports** | `POST /eligible/report` · `GET /eligible/report/{jobId}` *(asynchronous)* |

Example:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"maria","password":"Test1234!"}' | jq -r .token)

curl http://localhost:8080/api/v1/customers/{uuid}/accounts \
  -H "Authorization: Bearer $TOKEN"
```

---

## Configuration

Three profiles: **`dev`** (local), **`staging`**, **`pro`** (Render).

| Variable | Default in `dev` | Required in `pro` |
|---|---|---|
| `APP_SECURITY_SECRET_KEY` | throwaway development key | ✅ |
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | ✅ |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `bankapp` / `cf9` / `1` | ✅ |
| `ALLOWED_ORIGINS` | `localhost:5173,3000,8080` | ✅ |

⚠️ In `pro`, `APP_SECURITY_SECRET_KEY` has **no default** — without it the application
will not start. That is deliberate: no production key lives in the repository.

---

## What this is not

The **Loans**, **Cards** and **Investments** tabs are interface demonstrations backed by
synthetic data — there is no backend behind them. A deliberate scoping decision, not an
unfinished feature.

---
---

# 🇬🇷 Ελληνικά

Τραπεζική εφαρμογή με REST API σε **Spring Boot** και client σε **React**. Υποστηρίζει
λογαριασμούς όψεως και ταμιευτηρίου, καταθέσεις, αναλήψεις, μεταφορές, πληρωμές και
IRIS, με πρόσβαση που καθορίζεται από τον ρόλο του χρήστη.

Τελική εργασία — **Coding Factory 10**, ΟΠΑ.

🔗 **Live:** https://bankapp-3cwp.onrender.com
📖 **Τεκμηρίωση API (Swagger):** https://bankapp-3cwp.onrender.com/swagger-ui.html

---

## Build (ελληνικά)

### Προαπαιτούμενα

| Εργαλείο | Έκδοση | Σημείωση |
|---|---|---|
| **JDK** | 21 | Το Gradle wrapper κατεβάζει το Gradle μόνο του |
| **Docker** | οποιαδήποτε | Για τη βάση δεδομένων |
| ~~Node.js~~ | — | **Δεν χρειάζεται.** Το Gradle κατεβάζει Node 20 αυτόματα |

### Ο γρήγορος τρόπος — τα πάντα με μία εντολή

```bash
docker compose up
```

Σηκώνει **PostgreSQL και εφαρμογή μαζί**. Άνοιξε **http://localhost:8080**.

Το πρώτο build αργεί μερικά λεπτά — μεταγλωττίζει backend και React μέσα στο image.
Δεν χρειάζεται να δημιουργήσεις πίνακες: το **Flyway** τρέχει τις migrations `V1`–`V5`
στο πρώτο ξεκίνημα και εισάγει δοκιμαστικά δεδομένα.

| Εντολή | Τι κάνει |
|---|---|
| `docker compose up` | Build και εκκίνηση |
| `docker compose up -d` | Το ίδιο, στο παρασκήνιο |
| `docker compose down` | Σταμάτημα |
| `docker compose down -v` | Σταμάτημα **και διαγραφή** των δεδομένων |

### Ο τρόπος για ανάπτυξη — βάση σε Docker, εφαρμογή τοπικά

**Βήμα 1 — Βάση δεδομένων**

```bash
docker compose up -d db
```

**Βήμα 2 — Build και εκτέλεση**

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Τι ακριβώς κάνει αυτή η εντολή** — το build είναι ενοποιημένο:

```
./gradlew bootRun
  ├─ nodeSetup        κατεβάζει Node 20 (μία φορά)
  ├─ npmInstall       εγκαθιστά τα dependencies του bankApp/
  ├─ buildReact       τρέχει vite build → bankApp/dist
  ├─ compileJava      μεταγλωττίζει το backend
  ├─ processResources αντιγράφει τα resources
  ├─ copyReactDist    αντιγράφει το bankApp/dist στα static resources
  └─ bootRun          σηκώνει το Spring Boot στο :8080
```

Το React **δεν** σερβίρεται από ξεχωριστό server — μπαίνει μέσα στο ίδιο artifact.

### Δοκιμαστικοί λογαριασμοί

| Χρήστης | Κωδικός | Ρόλος |
|---|---|---|
| `admin` | `Admin1234!` | Διαχειριστής |
| `maria` | `Test1234!` | Πελάτης |
| `nikos` | `Test1234!` | Πελάτης |

---

## Deploy (ελληνικά)

Η εφαρμογή τρέχει στο **Render** ως Docker web service, με διαχειριζόμενη PostgreSQL.
Το `Dockerfile` είναι **multi-stage**: το πρώτο στάδιο χτίζει backend και React, το
δεύτερο κρατά μόνο το JRE και το jar.

**Βήματα:**

1. **PostgreSQL:** New → PostgreSQL. Κράτα host, port, database, user, password.
2. **Web Service:** New → Web Service → σύνδεση με το GitHub repo → Runtime **Docker**.
3. **Μεταβλητές περιβάλλοντος:**

   | Μεταβλητή | Τιμή |
   |---|---|
   | `APP_SECURITY_SECRET_KEY` | Κλειδί base64, **≥32 bytes μετά το decode** |
   | `DB_HOST` / `DB_PORT` | Internal host της βάσης / `5432` |
   | `DB_NAME` / `DB_USER` / `DB_PASSWORD` | Στοιχεία της βάσης |
   | `ALLOWED_ORIGINS` | Το URL της εφαρμογής |

   Παραγωγή κλειδιού: `openssl rand -base64 48`

4. **Deploy.** Κάθε push στο `main` ενεργοποιεί νέο build αυτόματα.

⚠️ Στο πρώτο ξεκίνημα το **Flyway** εφαρμόζει τις migrations `V1`–`V5`. Αν κάποια
αποτύχει, **η εφαρμογή δεν ξεκινά** — το `ddl-auto` είναι `validate`, οπότε το
Hibernate δεν αλλάζει ποτέ το schema μόνο του.

**Έλεγχος μετά το deploy:** το `/` επιστρέφει το React, το `/swagger-ui.html` ανοίγει,
και το `POST /api/v1/auth/authenticate` γυρνά token. Στα logs πρέπει να δεις
`Migrating schema "public"` και `Started RestBankAppApplication`.

---

## Αρχιτεκτονική (ελληνικά)

Στρωματοποιημένη, με τις εξαρτήσεις να δείχνουν πάντα προς τα μέσα:
**Controller → Service → Repository → Entities**. Τα entities δεν βγαίνουν ποτέ από το
service layer — οι controllers ανταλλάσσουν μόνο DTOs.

**Authentication** με **JWT**. **Authorization** σε δύο επίπεδα: URL level στο
`SecurityConfiguration`, και method level με `@PreAuthorize` στο **service**, όπου
γίνεται ο ουσιαστικός έλεγχος.

Ο έλεγχος γίνεται πάνω σε **δυνατότητες** (capabilities), όχι σε ρόλους, και η
αντιστοίχιση ζει στη **βάση** — νέα δυνατότητα προστίθεται με migration, χωρίς
recompile.

**Ο ρόλος δεν αρκεί για τα προσωπικά δεδομένα.** Τα IBAN ταξιδεύουν σε κάθε μεταφορά,
άρα δεν είναι μυστικά· γι' αυτό ελέγχεται **ιδιοκτησία**. Το προσωπικό βλέπει τα πάντα,
ο πελάτης μόνο τα δικά του. Στο frontend η εξουσιοδότηση καθρεφτίζεται για λόγους
εμπειρίας χρήστη — **η απόφαση παίρνεται πάντα στον server**.

**Σχεδιαστικές επιλογές:** κληρονομικότητα λογαριασμών με strategy για την προμήθεια ·
πολλοί κάτοχοι ανά λογαριασμό (κοινοί λογαριασμοί) · soft delete παντού · το ιστορικό
εξηγεί πάντα το υπόλοιπο, προμήθειες και εισερχόμενες μεταφορές συμπεριλαμβανομένων ·
φραγές στις καταστροφικές ενέργειες.

---

## Τι δεν είναι

Οι καρτέλες **Δάνεια**, **Κάρτες** και **Επενδύσεις** είναι επίδειξη διεπαφής με
συνθετικά δεδομένα — δεν υπάρχει backend από πίσω. Συνειδητή απόφαση λόγω εύρους,
όχι ημιτελής λειτουργία.
