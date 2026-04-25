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

### Step 1 – Start database (Docker)

```bash
docker compose up -d postgres
```

### Step 2 – Start backend (Quarkus Dev Mode)

```bash
./mvnw quarkus:dev
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

### Database

- Each developer runs their **own local database**
- Schema is created automatically via **Flyway**
- Data is **NOT shared** between developers


### CORS

Already enabled in backend.


## 2. Docker Mode (OPTIONAL)

Used for **production-like testing and validation** before creating a merge request



### Step 1 – Build backend

```bash
./mvnw clean package
```

### Step 2 – Start full system

```bash
docker compose up --build
```

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
git clone https://git.fiw.fhws.de/ss25ppkompetenztauschp2p/kompetenztauschp2p.git
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
