# DEV demo data

Flyway always applies schema migrations from `src/main/resources/db/migration`. In the Quarkus `dev` profile it additionally scans `src/main/resources/db/dev-seed`; production scans only `db/migration`. The demo seed is therefore never discovered or run in PROD.

`R__insert_demo_data.sql` is a repeatable Flyway migration. It is rerun when its checksum changes, and its inserts are idempotent so a normal application restart does not duplicate the data.

## Demo accounts and test data

The seed creates 50 clearly marked accounts: `demo.user01@example.test` through `demo.user50@example.test`. All use the password `password` (development only; stored as a BCrypt hash). Each user has three offered skills and two wanted skills across Java, PostgreSQL, React, Python, Docker, UX, languages, Git, testing, SQL, and related topics.

Useful manual test pairs include:

- `demo.user01@example.test` / `demo.user02@example.test`: accepted request, closed session, published ratings, and conversation.
- `demo.user03@example.test` / `demo.user04@example.test`: accepted request and completed session.
- `demo.user05@example.test` / `demo.user06@example.test`: active session.
- `demo.user07@example.test` / `demo.user08@example.test`: rating window open.
- demo.user09@example.test / demo.user10@example.test: pending request and conversation.
- demo.user11@example.test / demo.user12@example.test: completed session with published ratings.
- demo.user13@example.test / demo.user14@example.test: completed session with published ratings.
- demo.user15@example.test / demo.user16@example.test: rejected request.
- `demo.user17@example.test` / `demo.user18@example.test`: active session for fast completion and rating-window testing.
- `demo.user20@example.test`: internally flagged demo user with three private reports.

## Fast rating-window flow test

Use this seeded active session:

```text
sessionId: 20000000-0000-4000-8000-000000000007
user17: 00000000-0000-4000-8000-000000000017 / demo.user17@example.test / password
user18: 00000000-0000-4000-8000-000000000018 / demo.user18@example.test / password
```

The flow opens the rating window immediately after both users submit `COMPLETED`.
The first rating stays `PENDING`; the second rating publishes both ratings and closes the rating window.

PowerShell example:

```powershell
$base = "http://localhost:8080"

$login17 = Invoke-RestMethod -Method Post "$base/auth/login" -ContentType "application/json" -Body '{"email":"demo.user17@example.test","password":"password"}'
$login18 = Invoke-RestMethod -Method Post "$base/auth/login" -ContentType "application/json" -Body '{"email":"demo.user18@example.test","password":"password"}'

$token17 = $login17.token
$token18 = $login18.token
$sessionId = "20000000-0000-4000-8000-000000000007"
$user17 = "00000000-0000-4000-8000-000000000017"
$user18 = "00000000-0000-4000-8000-000000000018"

Invoke-RestMethod -Method Post "$base/sessions/$sessionId/completion-response" -Headers @{ Authorization = "Bearer $token17" } -ContentType "application/json" -Body '{"answer":"COMPLETED","reason":"Demo fast-flow test"}'
Invoke-RestMethod -Method Post "$base/sessions/$sessionId/completion-response" -Headers @{ Authorization = "Bearer $token18" } -ContentType "application/json" -Body '{"answer":"COMPLETED","reason":"Demo fast-flow test"}'

Invoke-RestMethod -Method Post "$base/ratings/create/" -Headers @{ Authorization = "Bearer $token17" } -ContentType "application/json" -Body (@{ sessionId = $sessionId; receiverUserId = $user18; points = 4.5; comment = "Great exchange." } | ConvertTo-Json)
Invoke-RestMethod -Method Post "$base/ratings/create/" -Headers @{ Authorization = "Bearer $token18" } -ContentType "application/json" -Body (@{ sessionId = $sessionId; receiverUserId = $user17; points = 5.0; comment = "Very helpful." } | ConvertTo-Json)
```

To inspect the result as admin:

```powershell
$admin = Invoke-RestMethod -Method Post "$base/auth/login" -ContentType "application/json" -Body '{"email":"admin@kompetenz.de","password":"admin123"}'
$adminToken = $admin.token

Invoke-RestMethod -Method Get "$base/sessions/$sessionId" -Headers @{ Authorization = "Bearer $adminToken" }
Invoke-RestMethod -Method Get "$base/ratings/get-all-ratings" -Headers @{ Authorization = "Bearer $adminToken" }
```

## Flagged-user demo data

The seed includes private reports against `demo.user20@example.test`, which crosses the current flag threshold of three reports:

```text
reported user: 00000000-0000-4000-8000-000000000020
private_report_count: 3
internally_flagged: true
```

Admin inspection endpoints:

```powershell
$base = "http://localhost:8080"
$admin = Invoke-RestMethod -Method Post "$base/auth/login" -ContentType "application/json" -Body '{"email":"admin@kompetenz.de","password":"admin123"}'
$adminToken = $admin.token

Invoke-RestMethod -Method Get "$base/sessions/admin/private-reports" -Headers @{ Authorization = "Bearer $adminToken" }
Invoke-RestMethod -Method Get "$base/users/getUser/00000000-0000-4000-8000-000000000020" -Headers @{ Authorization = "Bearer $adminToken" }
```

## Reset and reload locally

With the local Docker database running, reset its data volume and start it again:

```powershell
docker compose down -v
docker compose up -d postgres
```

Then start the backend in DEV mode:

```powershell
.\mvnw quarkus:dev
```

Flyway creates the schema and applies the repeatable seed at application startup. To apply a changed seed without a reset, restart the DEV application; Flyway reruns the repeatable migration when the file checksum changes.

## Optional embedding backfill

Embeddings are intentionally not inserted by SQL because `skill_embeddings.embedding_json` must contain valid model vectors. To generate them locally, start the app with `EMBEDDING_ENABLED=true`, a valid `GEMINI_API_KEY`, and `EMBEDDING_BACKFILL_ENABLED=true`, then call:

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/internal/embeddings/backfill
```

See `docs/embedding-runbook.md` for the operational details.
