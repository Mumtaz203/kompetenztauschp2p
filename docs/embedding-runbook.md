# Embedding Runbook

This guide explains how to run the project with and without Gemini embeddings.

## 1. Run Without API Key

Use this mode for normal development when embedding generation is not needed.

```powershell
git clone <repo-url>
cd kompetenztauschp2p
docker compose up --build
```

Backend URL:

```text
http://localhost:8081
```

No API key is required. The default config is:

```properties
embedding.enabled=false
```

The app still supports normal flows such as register, login, profile updates, and search. New embeddings are not generated.

## 2. Run With Gemini Embeddings

Use this mode when you want to generate real skill embeddings.

Set environment variables in PowerShell:

```powershell
$env:EMBEDDING_ENABLED="true"
$env:GEMINI_API_KEY="YOUR_REAL_GEMINI_KEY"
```

Start Docker:

```powershell
docker compose up --build
```

Backend URL:

```text
http://localhost:8081
```

Create a test user:

```powershell
$user = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8081/auth/register" `
  -ContentType "application/json" `
  -Body '{"username":"backend_user","email":"backend_user@test.com","password":"password123"}'
```

Add skills:

```powershell
Invoke-RestMethod -Method Put `
  -Uri "http://localhost:8081/users/$($user.id)/updateSkills" `
  -ContentType "application/json" `
  -Body '{"offeredSkills":["Java","Spring Boot","REST API"],"wantedSkills":["Math"]}'
```

Verify database embeddings:

```powershell
docker exec -it p2p-db psql -U postgres -d p2p_db
```

```sql
SELECT user_id, skill_text, skill_type
FROM skill_embeddings;
```

Exit psql:

```sql
\q
```

## 3. Run In Quarkus Dev Mode

Start only PostgreSQL:

```powershell
docker compose up -d postgres
```

Run without embeddings:

```powershell
mvn quarkus:dev
```

Run with embeddings:

```powershell
$env:GEMINI_API_KEY="YOUR_REAL_GEMINI_KEY"
mvn quarkus:dev "-Dembedding.enabled=true" "-Dembedding.backfill.enabled=true"
```

Dev backend URL:

```text
http://localhost:8080
```

## 4. Security Rules

Do not put real API keys in:

- `application.properties`
- `docker-compose.yml`
- `README.md`
- docs
- commits

Use environment variables only:

```powershell
$env:GEMINI_API_KEY="YOUR_REAL_GEMINI_KEY"
```

Environment variables are only active in the current terminal session. If you open a new terminal, set them again.

Before pushing, check that no real key is present:

```powershell
rg "AIza|AQ\\.|YOUR_REAL_GEMINI_KEY" .
```

The command should return no real key.
