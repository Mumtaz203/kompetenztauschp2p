# Semantic Search Manual Test Guide

This guide explains how to manually test the Gemini embedding and hybrid semantic search flow locally.

## Prerequisites

- PostgreSQL is running and the backend can connect to it.
- The backend starts successfully.
- A real Gemini API key is available.
- Embedding API usage is explicitly enabled only when needed.

PowerShell example:

```powershell
$env:EMBEDDING_ENABLED="true"
$env:GEMINI_API_KEY="your_real_key_here"
```

Do not put real API keys in `application.properties`, `docker-compose.yml`, docs, or commits.

## Required Local Config

For local backfill testing, enable the internal backfill endpoint:

```properties
embedding.backfill.enabled=true
```

This property must stay `false` by default. Only enable it locally or in controlled internal environments.

The production/default setting is:

```properties
embedding.enabled=false
embedding.backfill.enabled=false
```

## Start Backend

Start the Quarkus backend:

```bash
mvn quarkus:dev
```

On Windows PowerShell, quote `-D` properties if you enable backfill from the command line:

```powershell
mvn quarkus:dev "-Dembedding.enabled=true" "-Dembedding.backfill.enabled=true"
```

By default, the app should be available at:

```text
http://localhost:8080
```

## Create Or Ensure Test Users

Semantic search needs users with `offeredSkills`. Create or update users so the database contains examples like:

- User A: `Java`, `Spring Boot`, `REST API`
- User B: `PostgreSQL`, `SQL`, `Database Design`
- User C: `React`, `HTML`, `CSS`

Example registration request:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"backend_user","email":"backend@example.com","password":"password123"}'
```

Repeat registration for the database and frontend users with different usernames and emails.

Then update each user's offered skills using the existing profile update endpoint. Replace `<USER_ID>` with the real user ID from your database or API response:

```bash
curl -X PUT http://localhost:8080/users/<USER_ID>/updateSkills \
  -H "Content-Type: application/json" \
  -d '{"offeredSkills":["Java","Spring Boot","REST API"],"wantedSkills":[]}'
```

```bash
curl -X PUT http://localhost:8080/users/<USER_ID>/updateSkills \
  -H "Content-Type: application/json" \
  -d '{"offeredSkills":["PostgreSQL","SQL","Database Design"],"wantedSkills":[]}'
```

```bash
curl -X PUT http://localhost:8080/users/<USER_ID>/updateSkills \
  -H "Content-Type: application/json" \
  -d '{"offeredSkills":["React","HTML","CSS"],"wantedSkills":[]}'
```

If your local data already has users with these offered skills, you can reuse them.

## Trigger Backfill

After enabling `embedding.backfill.enabled=true`, trigger embedding backfill manually:

```bash
curl -X POST http://localhost:8080/internal/embeddings/backfill
```

Expected response shape:

```json
{
  "usersChecked": 3,
  "usersWithOfferedSkills": 3,
  "embeddingsEnsured": 9
}
```

The exact numbers depend on your local database and on how many embeddings already exist.

## Verify Database

Check that the `skill_embeddings` table contains records:

```sql
SELECT user_id, skill_text, skill_type
FROM skill_embeddings;
```

Avoid printing full embedding vectors unless you specifically need to debug vector storage.

## Test Semantic Search

The search endpoint requires a user token. Log in first:

```powershell
$login = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body '{"email":"backend@example.com","password":"password123"}'

$token = $login.token
```

Run semantic search requests:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/users/search?skills=backend" `
  -Headers @{ Authorization = "Bearer $token" }
```

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/users/search?skills=database" `
  -Headers @{ Authorization = "Bearer $token" }
```

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/users/search?skills=frontend" `
  -Headers @{ Authorization = "Bearer $token" }
```

Expected logical behavior:

- `backend` should find users with skills like `Java`, `Spring Boot`, or `REST API`.
- `database` should find users with skills like `SQL`, `PostgreSQL`, or `Database Design`.
- `frontend` should find users with skills like `React`, `HTML`, or `CSS`.

## Test Lexical Behavior

Existing exact and partial search behavior should still work:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/users/search?skills=SQL" `
  -Headers @{ Authorization = "Bearer $token" }
```

Expected behavior:

- Exact `SQL` matches should rank before partial matches like `MySQL`.
- Existing exact/partial behavior should remain intact while semantic ranking adds broader matches.

## Troubleshooting

- If no semantic results appear, check whether backfill was executed.
- If backfill fails, check that `embedding.enabled=true` and `GEMINI_API_KEY` is set and valid.
- If the backfill endpoint returns `403`, check that `embedding.backfill.enabled=true` is set for the running backend.
- If `skill_embeddings` is empty, no stored embeddings exist yet.
- Unit tests use mocks and do not call the real Gemini API.

## Security Notes

- Keep `embedding.backfill.enabled=false` by default.
- The backfill endpoint is intended for local/internal use only.
- Do not publish or share real Gemini API keys.
- Do not expose `/internal/embeddings/backfill` as a normal public feature endpoint.
