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
