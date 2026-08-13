# KostaBank

Τραπεζική εφαρμογή με REST API σε **Spring Boot** και client σε **React**. Υποστηρίζει
λογαριασμούς όψεως και ταμιευτηρίου, καταθέσεις, αναλήψεις, μεταφορές, πληρωμές και
IRIS, με πρόσβαση που καθορίζεται από τον ρόλο του χρήστη.

Τελική εργασία — **Coding Factory 10**, ΟΠΑ.

🔗 **Live:** https://bankapp-3cwp.onrender.com
📖 **API documentation (Swagger):** https://bankapp-3cwp.onrender.com/swagger-ui.html

> Φιλοξενείται σε δωρεάν πλάνο του Render — η πρώτη κλήση μετά από αδράνεια αργεί
> ~30 δευτερόλεπτα όσο ξυπνάει ο server.

---

## Περιεχόμενα

- [Build](#build)
- [Deploy](#deploy)
- [Αρχιτεκτονική](#αρχιτεκτονική)
- [Authentication & Authorization](#authentication--authorization)
- [Domain model](#domain-model)
- [Tests](#tests)
- [API](#api)
- [Ρυθμίσεις](#ρυθμίσεις)

---

## Build

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

---

### Ο τρόπος για ανάπτυξη — βάση σε Docker, εφαρμογή τοπικά

Πιο βολικός όταν γράφεις κώδικα: δεν ξαναχτίζεις image σε κάθε αλλαγή.

**Βήμα 1 — Βάση δεδομένων**

```bash
docker compose up -d db
```

**Βήμα 2 — Build και εκτέλεση**

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Άνοιξε **http://localhost:8080**.

**Τι ακριβώς κάνει αυτή η εντολή** — το build είναι ενοποιημένο, backend και frontend
μαζί:

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

Το React **δεν** σερβίρεται από ξεχωριστό server — μπαίνει μέσα στο ίδιο artifact και
το σερβίρει το Spring Boot. Ένα build, ένα deployable.

### Άλλες εντολές

```bash
./gradlew build          # πλήρες build + tests → build/libs/restBankApp-0.0.1-SNAPSHOT.jar
./gradlew test           # μόνο τα tests
./gradlew buildReact     # μόνο το frontend
java -jar build/libs/restBankApp-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Ανάπτυξη στο frontend

Για hot reload κατά την ανάπτυξη του React:

```bash
cd bankApp
npm install
npm run dev      # http://localhost:5173
```

Το Vite κάνει proxy το `/api` στο backend, οπότε τρέξε και το `bootRun` παράλληλα.

| Εντολή | Τι κάνει |
|---|---|
| `npm run dev` | Dev server με hot reload |
| `npm run build` | Production build → `dist/` |
| `npm run typecheck` | Έλεγχος τύπων TypeScript |
| `npm run lint` | ESLint |

### Δοκιμαστικοί λογαριασμοί

| Χρήστης | Κωδικός | Ρόλος |
|---|---|---|
| `admin` | `Admin1234!` | Διαχειριστής |
| `maria` | `Test1234!` | Πελάτης |
| `nikos` | `Test1234!` | Πελάτης |

---

## Deploy

Η εφαρμογή τρέχει στο **Render** ως Docker web service, με διαχειριζόμενη PostgreSQL.

### Πώς χτίζεται το image

Το [`Dockerfile`](Dockerfile) είναι **multi-stage**:

```
Stage 1 (eclipse-temurin:21-jdk)   ← build
  αντιγράφει gradlew, gradle/, build.gradle, settings.gradle, src/, bankApp/
  τρέχει ./gradlew build -x test
  → παράγει το jar (μαζί με το χτισμένο React μέσα του)

Stage 2 (eclipse-temurin:21-jre-alpine)   ← runtime
  αντιγράφει ΜΟΝΟ το jar από το stage 1
  ENV SPRING_PROFILES_ACTIVE=pro   (το docker-compose το αλλάζει σε dev τοπικά)
  ENTRYPOINT: java -jar app.jar
```

Το δεύτερο stage κρατά μόνο το JRE και το jar — όχι το JDK, τα sources ή το
`node_modules`. Το `.dockerignore` κρατά έξω τα `build/`, `node_modules/`, `docs/`
και το `.git/`.

Τα tests παραλείπονται στο image build (`-x test`) γιατί το `contextLoads` χρειάζεται
βάση· τρέχουν τοπικά και στο CI.

### Βήματα deploy στο Render

1. **PostgreSQL:** New → PostgreSQL. Κράτα host, port, database, user, password.
2. **Web Service:** New → Web Service → σύνδεση με το GitHub repo → Runtime **Docker**.
   Το Render εντοπίζει μόνο του το `Dockerfile`.
3. **Environment variables** (Settings → Environment):

   | Μεταβλητή | Τιμή |
   |---|---|
   | `APP_SECURITY_SECRET_KEY` | Κλειδί base64, **≥32 bytes μετά το decode** |
   | `DB_HOST` | Internal host της βάσης |
   | `DB_PORT` | `5432` |
   | `DB_NAME` | Όνομα βάσης |
   | `DB_USER` | Χρήστης |
   | `DB_PASSWORD` | Κωδικός |
   | `ALLOWED_ORIGINS` | Το URL της εφαρμογής, π.χ. `https://bankapp-3cwp.onrender.com` |

   Παραγωγή κλειδιού:
   ```bash
   openssl rand -base64 48
   ```

4. **Deploy.** Κάθε push στο `main` ενεργοποιεί νέο build αυτόματα.

### Τι συμβαίνει στο πρώτο ξεκίνημα

Το **Flyway** εφαρμόζει τις migrations με τη σειρά και καταγράφει την πρόοδο στον
πίνακα `flyway_schema_history`:

| Migration | Τι κάνει |
|---|---|
| `V1__schema.sql` | Πίνακες, κλειδιά, indexes |
| `V2__static_data.sql` | Περιφέρειες, ρόλοι, δυνατότητες |
| `V3__seed_data.sql` | Δοκιμαστικοί χρήστες, λογαριασμοί, κινήσεις |
| `V4__account_number_sequence.sql` | Sequence για αριθμούς λογαριασμών |
| `V5__unique_only_among_active_rows.sql` | Partial unique indexes (αγνοούν τα διαγραμμένα) |

⚠️ Αν μια migration αποτύχει, **η εφαρμογή δεν ξεκινά**. Το `ddl-auto` είναι
`validate`, όχι `update` — το Hibernate δεν αλλάζει ποτέ το schema μόνο του.

### Έλεγχος μετά το deploy

```bash
curl -i https://<το-url-σου>/                      # 200, το React index.html
curl -i https://<το-url-σου>/swagger-ui.html       # 200
curl -X POST https://<το-url-σου>/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin1234!"}'   # επιστρέφει token
```

Στα logs του Render πρέπει να δεις `Migrating schema "public"` και
`Started RestBankAppApplication`.

---

## Αρχιτεκτονική

Στρωματοποιημένη, με τις εξαρτήσεις να δείχνουν πάντα προς τα μέσα:

```
Controller   →  δέχεται HTTP, επικυρώνει είσοδο, μεταφράζει σε DTO
    ↓
Service      →  επιχειρησιακή λογική + έλεγχοι πρόσβασης + transactions
    ↓
Repository   →  Spring Data JPA
    ↓
Entities     →  domain model
```

```
src/main/java/gr/aueb/cf/restbankapp/
├── api/            REST controllers + κεντρικός χειρισμός σφαλμάτων (@ControllerAdvice)
├── service/        Επιχειρησιακή λογική — εδώ ζουν οι έλεγχοι πρόσβασης
├── repository/     Spring Data JPA interfaces
├── model/          Entities (κληρονομικότητα λογαριασμών, soft delete)
├── dto/            Java records με Bean Validation
├── security/       JWT filter, security config, έλεγχοι ιδιοκτησίας
├── validation/     Custom validators (διπλότυπα, επιχειρησιακοί κανόνες)
├── mapper/         Entity ↔ DTO
└── core/           Εξαιρέσεις, factories, ρύθμιση OpenAPI

src/main/resources/db/migration/   Flyway V1–V5
src/main/resources/static/legacy/  Το αρχικό vanilla frontend (αρχειοθετημένο)
bankApp/                           React client
```

**Τα entities δεν βγαίνουν ποτέ από το service layer** — οι controllers ανταλλάσσουν
μόνο DTOs.

### Τεχνολογίες

**Backend:** Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA ·
PostgreSQL 16 · Flyway · springdoc-openapi · Gradle
**Frontend:** React 19 · Vite · Tailwind CSS 4 · React Router 7 · Axios
**Υποδομή:** Docker (multi-stage) · Render

---

## Authentication & Authorization

**Authentication** με **JWT**. Το `/api/v1/auth/authenticate` επιστρέφει token που
συνοδεύει κάθε επόμενη κλήση ως `Authorization: Bearer <token>`. Το
`JwtAuthenticationFilter` το επικυρώνει σε κάθε request.

**Authorization** σε δύο επίπεδα:

1. **URL level** (`SecurityConfiguration`) — ποια endpoints είναι δημόσια.
2. **Method level** (`@PreAuthorize` στο **service**) — ο ουσιαστικός έλεγχος.

Ο έλεγχος γίνεται πάνω σε **δυνατότητες** (capabilities), όχι σε ρόλους:

| Ρόλος | Δυνατότητες |
|---|---|
| **CUSTOMER** | `VIEW_ONLY_CUSTOMER`, `VIEW_ONLY_ACCOUNT`, `CAN_DEPOSIT`, `CAN_WITHDRAW`, `CAN_TRANSFER` |
| **EMPLOYEE** | `VIEW_CUSTOMERS`, `VIEW_CUSTOMER`, `INSERT_CUSTOMER`, `EDIT_CUSTOMER`, `VIEW_ACCOUNTS`, `VIEW_ACCOUNT`, `CREATE_ACCOUNT` |
| **ADMIN** | Όλες |

Η αντιστοίχιση ζει στη βάση (`V2__static_data.sql`), όχι στον κώδικα — νέα δυνατότητα
προστίθεται με migration, χωρίς recompile.

**Ο ρόλος δεν αρκεί για τα προσωπικά δεδομένα.** Τα IBAN ταξιδεύουν σε κάθε μεταφορά,
άρα δεν είναι μυστικά· γι' αυτό ελέγχεται **ιδιοκτησία**:

```java
@PreAuthorize("hasAuthority('VIEW_ACCOUNT') or (hasAuthority('VIEW_ONLY_ACCOUNT') "
            + "and @securityService.isOwnAccount(#iban, authentication))")
```

Το προσωπικό βλέπει τα πάντα· ο πελάτης μόνο τα δικά του.

**Στο frontend** η εξουσιοδότηση καθρεφτίζεται: `AuthContext` κρατά τον ρόλο,
`ProtectedRoute` φυλά τις διαδρομές, και οι καρτέλες/ενέργειες εμφανίζονται ανάλογα.
Είναι θέμα εμπειρίας χρήστη — **η απόφαση παίρνεται πάντα στον server**.

---

## Domain model

```
Region 1───N Customer 1───1 User N───1 Role N───N Capability
                │                                  
                │ 1───1 PersonalInfo 1───1 Attachment (αρχείο ταυτότητας)
                │
                └─ N───N Account ◄── AccountChecking / AccountSavings
                              │
                              └─ 1───N Transaction
```

**Σχεδιαστικές επιλογές:**

- **Κληρονομικότητα λογαριασμών** — `Account` είναι abstract με `AccountChecking` και
  `AccountSavings` (single table + discriminator). Η προμήθεια δίνεται από
  **strategy**, ώστε νέος τύπος λογαριασμού να μη χρειάζεται `if`.
- **Πολλοί κάτοχοι ανά λογαριασμό** (many-to-many) — υποστηρίζει κοινούς λογαριασμούς.
  Γι' αυτό το κλείσιμο λογαριασμού **δεν** διαγράφει τους πελάτες του.
- **Soft delete παντού** — τίποτα δεν χάνεται· τα διαγραμμένα παύουν να είναι ορατά.
  Τα partial unique indexes (V5) ελευθερώνουν ΑΦΜ/username για επανεγγραφή.
- **Το ιστορικό εξηγεί πάντα το υπόλοιπο** — κάθε κίνηση χρημάτων αφήνει εγγραφή,
  συμπεριλαμβανομένων των **προμηθειών** και της **εισερχόμενης** πλευράς μεταφοράς.
- **Οι καταστροφικές ενέργειες έχουν φραγές** — δεν διαγράφεται πελάτης με ανοιχτούς
  λογαριασμούς, δεν διαγράφεις τον εαυτό σου, δεν διαγράφεται ο τελευταίος διαχειριστής.

---

## Tests

```bash
./gradlew test
```

**Unit tests** (Mockito) για επιχειρησιακή λογική — καταθέσεις, αναλήψεις,
υπολογισμό προμηθειών, validators.

**Security tests** που φορτώνουν το **πραγματικό** service bean πίσω από το
method-security proxy. Ένα mocked bean δεν κουβαλά annotations, οπότε δεν μπορεί ποτέ
να ελέγξει `@PreAuthorize` — γι' αυτό αυτά τα tests στήνουν Spring context με
`@EnableMethodSecurity`.

**Money integrity test** που επιβεβαιώνει ότι το άθροισμα των κινήσεων ισούται με το
υπόλοιπο μετά από μεταφορά — για τον αποστολέα *και* τον παραλήπτη.

Το `contextLoads` τρέχει σε **H2 in-memory** ώστε να μη χρειάζεται PostgreSQL.

**Integration tests** με Postman — σενάρια στο `docs/kostabank_tests.md`.

---

## API

Όλα τα endpoints κάτω από `/api/v1`. Πλήρης τεκμηρίωση στο **Swagger UI**
(`/swagger-ui.html`) — πάτα **Authorize**, κόλλα ένα JWT, και δοκίμασε από τη σελίδα.

| Ομάδα | Endpoints |
|---|---|
| **Auth** | `POST /auth/authenticate` |
| **Λογαριασμοί** | `POST /accounts` · `GET /accounts` · `GET /accounts/{iban}` · `DELETE /accounts/{iban}` · `POST /accounts/deposit` · `POST /accounts/withdraw` · `POST /accounts/transfer` · `GET /accounts/{iban}/transactions` *(σελιδοποιημένο)* · `GET /accounts/{iban}/fee` · `GET /accounts/{iban}/owner` · `GET /accounts/phone/{phone}` |
| **Πελάτες** | `POST /customers` · `GET /customers` · `GET /customers/{uuid}` · `PUT /customers/{uuid}` · `DELETE /customers/{uuid}` · `GET /customers/{uuid}/accounts` · `POST /customers/{uuid}/id-file` · `PUT /customers/{uuid}/password` |
| **Χρήστες** | `POST /users` · `GET /users/staff` · `GET /users/{uuid}` · `DELETE /users/{uuid}` · `PUT /users/{uuid}/password` · `PUT /users/{uuid}/reset-password` |
| **Αναφορές** | `POST /eligible/report` · `GET /eligible/report/{jobId}` *(ασύγχρονο)* |

Παράδειγμα:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"maria","password":"Test1234!"}' | jq -r .token)

curl http://localhost:8080/api/v1/customers/{uuid}/accounts \
  -H "Authorization: Bearer $TOKEN"
```

---

## Ρυθμίσεις

Τρία profiles: **`dev`** (τοπικά), **`staging`**, **`pro`** (Render).

| Μεταβλητή | Προεπιλογή στο `dev` | Υποχρεωτική στο `pro` |
|---|---|---|
| `APP_SECURITY_SECRET_KEY` | throwaway κλειδί ανάπτυξης | ✅ |
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | ✅ |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `bankapp` / `cf9` / `1` | ✅ |
| `ALLOWED_ORIGINS` | `localhost:5173,3000,8080` | ✅ |

⚠️ Στο `pro` το `APP_SECURITY_SECRET_KEY` **δεν έχει προεπιλογή** — χωρίς αυτό η
εφαρμογή δεν ξεκινά. Είναι σκόπιμο: κανένα κλειδί παραγωγής δεν ζει στο repository.

---

## Τι δεν είναι

Οι καρτέλες **Δάνεια**, **Κάρτες** και **Επενδύσεις** είναι επίδειξη διεπαφής με
συνθετικά δεδομένα — δεν υπάρχει backend από πίσω. Συνειδητή απόφαση λόγω εύρους,
όχι ημιτελής λειτουργία.
