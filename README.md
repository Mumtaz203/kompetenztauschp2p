# KompetenztauschP2P

A peer-to-peer platform where users can exchange skills instead of money.  
Users can offer skills, search for others, and get matched based on relevance.


## Getting Started

### Tech Stack

- Backend: Quarkus (Java)
- Database: PostgreSQL (Docker)
- Frontend: Flutter (Dart)


## Development Setup (IMPORTANT)

We use two modes:

## 1. Development Mode (DEFAULT)

Used for daily development.

### Step 1 – Start database

Use either your local PostgreSQL on port `5432`, or start the Docker PostgreSQL service. The Docker service is exposed on host port `5433` to avoid conflicts with a local PostgreSQL install.

```bash
docker compose up -d postgres
```

### Step 2 – Start backend (Quarkus Dev Mode)

If you use a local PostgreSQL on `5432`:

```bash
./mvnw quarkus:dev
```

If you use the Docker PostgreSQL service on `5433`:

```bash
DEV_DATABASE_URL=jdbc:postgresql://localhost:5433/p2p_db ./mvnw quarkus:dev
```

Backend runs on:

```
http://localhost:8080
```

### Step 3 – Start frontend (Flutter)

```bash
flutter run -d chrome
```

Frontend should connect to:

```
http://localhost:8080
```

If you need a different backend URL, pass it at startup:

```bash
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8081
```

## Full Development Flow

```
Frontend (Flutter)
        ↓
http://localhost:8080
        ↓
Quarkus Backend (Dev Mode)
        ↓
PostgreSQL (Docker)
```

## Important Notes

### Optional Embeddings

- The app works without a Gemini API key.
- Embedding generation is disabled by default with `embedding.enabled=false`.
- To test embeddings locally, set `EMBEDDING_ENABLED=true` and `GEMINI_API_KEY` in your shell before starting the backend or Docker Compose.
- Do not put real API keys in `application.properties`, `docker-compose.yml`, docs, or commits.

### Database

- Each developer runs their **own local database**
- Schema is created automatically via **Flyway**
- Data is **NOT shared** between developers


### CORS

Already enabled in backend. Restart Quarkus after changing CORS settings.

For deployment, set `CORS_ORIGINS` to the public frontend origin, for example:

```bash
CORS_ORIGINS=http://16.171.23.58 docker compose up --build -d backend
```


## 2. Docker Mode (OPTIONAL)

Used for **production-like testing and validation** before creating a merge request



### Step 1 – Start full system

```bash
docker compose up --build
```

The Dockerfile builds the Quarkus package inside Docker, so changes in `src/main/resources/application.properties` are included by `docker compose up --build`.

Backend runs on:

```
http://localhost:8081
```

## Docker Flow

```
Frontend
        ↓
http://localhost:8081
        ↓
Docker Backend
        ↓
PostgreSQL (Docker)
```

## 3. Flutter Web Deployment

The Flutter frontend reads its backend URL from `API_BASE_URL`.

Build the web frontend locally for a deployed backend:

```bash
cd frontend
flutter build web --dart-define=API_BASE_URL=http://SERVER_IP:8081
cd ..
```

Copy `frontend/build/web` to the server, or run the same Flutter build command on the server before starting the `web` compose profile. The `web` service serves `./frontend/build/web`; this generated folder is not committed to Git.

Run backend + PostgreSQL on the server:

```bash
CORS_ORIGINS=http://SERVER_IP docker compose up --build -d backend
```

Serve the already-built Flutter web output through Nginx:

```bash
CORS_ORIGINS=http://SERVER_IP docker compose --profile web up -d web
```

The web app runs on:

```
http://SERVER_IP
```

The backend API runs on:

```
http://SERVER_IP:8081
```

On AWS, the security group must allow:

```
HTTP        TCP 80    0.0.0.0/0
Custom TCP  TCP 8081  0.0.0.0/0
SSH         TCP 22    your IP only
```

---

## Dev vs Docker Mode

| Mode       | Port  | Speed | Purpose                     |
|------------|-------|------|-----------------------------|
| Dev Mode   | 8080  | Fast | Daily development           |
| Docker     | 8081  | Slow | Production-like validation  |

---

## Workflow Recommendation

### Daily Work

```
Frontend → http://localhost:8080
```

### Before Merge Request (GitLab)

```
Frontend → http://localhost:8081
```

---

## Git Workflow

1. Create feature branch
2. Develop using Dev Mode (8080)
3. Test using Docker Mode (8081)
4. Create Merge Request in GitLab
5. Review & merge

## Installation

```bash
git clone https://github.com/Mumtaz203/kompetenztauschp2p.git
cd kompetenztauschp2p
```

## Requirements

- Java 21
- Maven (or use `./mvnw`)
- Docker
- Flutter SDK

## Example API Usage

```http
POST http://localhost:8080/auth/register
```

## Takeaway

- Use **Dev Mode for development**
- Use **Docker Mode for validation**
- Do NOT mix both randomly
