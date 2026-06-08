# Discover Users Manual Test Guide

This guide explains how to manually test the Discover/Recommendation feature locally.

## 1. Prerequisites

- PostgreSQL/database is running and the backend can connect to it.
- The backend starts successfully.
- Embedding API usage is explicitly enabled only when needed.
- Do not put real API keys in `application.properties`, `docker-compose.yml`, docs, or commits.
- The embedding backfill endpoint is disabled by default.

PowerShell example:

```powershell
$env:EMBEDDING_ENABLED="true"
$env:GEMINI_API_KEY="your_real_key_here"
```

The default setting must stay:

```properties
embedding.enabled=false
embedding.backfill.enabled=false
```

## 2. Start Backend

Start the backend on port `8082` and enable backfill only for local/manual testing:

```bash
mvn quarkus:dev -Dquarkus.http.port=8082 -Dembedding.enabled=true -Dembedding.backfill.enabled=true
```

Windows PowerShell version:

```powershell
mvn quarkus:dev "-Dquarkus.http.port=8082" "-Dembedding.enabled=true" "-Dembedding.backfill.enabled=true"
```

`embedding.backfill.enabled=true` should only be used locally or in controlled internal environments.

## 3. Test Data

Create or update users so the database contains examples like these:

Current user:

- username: `current_user`
- offeredSkills: `Turkish`, `Math`
- wantedSkills: `backend`

Backend candidate:

- username: `backend_user`
- offeredSkills: `Java`, `Spring Boot`, `REST API`
- wantedSkills: `Math`

Database candidate:

- username: `database_user`
- offeredSkills: `SQL`, `PostgreSQL`, `Database Design`
- wantedSkills: `Turkish`

Frontend candidate:

- username: `frontend_user`
- offeredSkills: `React`, `HTML`, `CSS`
- wantedSkills: `German`

Users can be created or updated through the existing user/profile endpoints. Use the existing profile update flow to set `offeredSkills` and `wantedSkills` for each test user.

## 4. Run Backfill

After the backend is running with `embedding.backfill.enabled=true`, trigger embedding backfill:

```bash
curl -X POST http://localhost:8082/internal/embeddings/backfill
```

Expected result:

- HTTP 200 if enabled
- JSON with fields like:
  - `usersChecked`
  - `usersWithOfferedSkills`
  - `embeddingsEnsured`

If the endpoint returns `403`, the running backend is missing `embedding.backfill.enabled=true`.

## 5. Verify Database

Check that skill embeddings were stored:

```sql
SELECT user_id, skill_text, skill_type
FROM skill_embeddings;
```

`OFFERED` and `WANTED` embeddings should exist. Do not print or expose full embedding vectors unless necessary for debugging.

## 6. Test Discover Endpoint

Replace `{currentUserId}` with the real ID of `current_user`:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8082/users/{currentUserId}/discover" `
  -Headers @{ Authorization = "Bearer $token" }
```

Expected behavior:

- `backend_user` should rank high because `current_user` wants `backend` and the candidate offers `Java`, `Spring Boot`, and `REST API`.
- Candidates with two-way matches should rank higher.
- Unrelated users should have a low score or not appear.
- The response must not contain raw embeddings.

Example expected response shape:

```json
[
  {
    "userId": "...",
    "username": "backend_user",
    "score": 87,
    "bestSimilarity": 0.91,
    "matchedSkills": ["Java", "Spring Boot"],
    "matchReason": "Your wanted skills match this user's offered skills."
  }
]
```

## 7. Verify Existing Search Still Works

The existing `/users/search` endpoint should still work:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8082/users/search?skills=backend" `
  -Headers @{ Authorization = "Bearer $token" }

Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8082/users/search?skills=SQL" `
  -Headers @{ Authorization = "Bearer $token" }
```

Expected behavior:

- Semantic search still works.
- Exact/partial keyword search still works.
- `SQL` exact match should still rank correctly.

## 8. Troubleshooting

- If no recommendations appear, check the `skill_embeddings` table.
- If backfill returns `403`, enable `embedding.backfill.enabled=true` locally.
- If Gemini fails, check that `embedding.enabled=true` and `GEMINI_API_KEY` is set and valid.
- If the backend cannot start, check the database connection.
- Unit tests must not call the real Gemini API.
- Do not publish or share API keys.

## 9. Final Verification

Run the full test suite:

```bash
mvn test
```

Expected result:

- all tests pass
- discover tests pass
- semantic search tests pass
- no real Gemini API call in tests
