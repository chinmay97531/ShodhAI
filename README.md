# ShodhAI

ShodhAI is a full-stack programming contest prototype that combines a Spring Boot
backend with a Next.js frontend. The backend exposes REST APIs for managing
contests and powering a live code judge, while the frontend delivers an
interactive contest experience with live polling for submissions and the
leaderboard.

## Getting Started

### Prerequisites

- Node.js 18+
- npm or pnpm
- Java 17+
- Maven 3.9+
- (Optional) Docker, if you want to run submissions inside the provided
  execution image. The judge falls back to local runtimes for Python and
  JavaScript when Docker is not available.

### Environment Variables

Copy the example environment file and adjust the API endpoint if needed:

```bash
cp .env.example .env.local
```

### Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

The backend exposes the REST API on `http://localhost:8080` and ships with an
in-memory H2 database that is pre-populated with a sample contest and three
problems.

### Frontend (Next.js)

```bash
npm install
npm run dev
```

The app will be available at `http://localhost:3000`. Use the contest identifier
`winter-open` to explore the sample data.

### Docker Judge Image

To build the optional runtime image that can be used by the judge service for
isolated execution:

```bash
cd backend/docker
docker build -t shodhai/judge:latest -f Dockerfile.judge .
```

You can then update the application configuration to invoke the containerized
runtime instead of the local fallback.

### Hosting with Docker Compose

The repository ships with a `docker-compose.yaml` file that builds production
containers for both the Spring Boot backend and the Next.js frontend. This is
the quickest way to host the full stack locally or on a container platform that
supports Docker Compose specs.

1. Ensure Docker is installed and running.
2. (Optional) Update `.env.local` or pass the `NEXT_PUBLIC_API_BASE_URL`
   environment variable if your backend will be exposed on a different host.
3. Build and start both services:

   ```bash
   docker compose up --build
   ```

The backend will be available on port `8080` and the frontend on port `3000`.
When deployed to a Compose-compatible host, traffic to the frontend is routed
through the service network to the backend using the internal hostname
`backend:8080`.
