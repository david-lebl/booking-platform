# Deployment

## Configuration

All configuration is provided via environment variables (with defaults in `application.conf`):

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_HOST` | `0.0.0.0` | HTTP server bind host |
| `SERVER_PORT` | `8080` | HTTP server bind port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/booking` | PostgreSQL JDBC URL |
| `DATABASE_USER` | `postgres` | Database user |
| `DATABASE_PASSWORD` | `postgres` | Database password |

## Running Locally

### Without Database (In-Memory)

```bash
sbt app/run
```

All data is stored in-memory and lost on restart. Suitable for development and API exploration.

### With PostgreSQL

```bash
# Start PostgreSQL
docker run -d --name booking-db \
  -e POSTGRES_DB=booking \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Run with database (once DB repositories are implemented)
DATABASE_URL=jdbc:postgresql://localhost:5432/booking sbt app/run
```

## Docker

### Build Docker Image

```dockerfile
# Dockerfile (to be created)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY app/target/universal/stage/ /app/

EXPOSE 8080

ENTRYPOINT ["/app/bin/app"]
```

### Build Steps

```bash
# Package the application
sbt app/stage

# Build Docker image
docker build -t booking-platform .

# Run
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/booking \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=postgres \
  booking-platform
```

### Docker Compose

```yaml
# docker-compose.yml (to be created)
version: '3.8'

services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: booking
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SERVER_HOST: "0.0.0.0"
      SERVER_PORT: "8080"
      DATABASE_URL: "jdbc:postgresql://db:5432/booking"
      DATABASE_USER: "postgres"
      DATABASE_PASSWORD: "postgres"
    depends_on:
      - db

volumes:
  pgdata:
```

## Production Considerations

### Database

- Use a managed PostgreSQL service (AWS RDS, Azure Database, etc.)
- Enable SSL connections
- Set up connection pooling (HikariCP via Typo/ZIO JDBC)
- Configure appropriate `max_connections`
- Set up automated backups

### Authentication

Replace `StubAuthService` with a proper implementation:
- JWT token validation
- OAuth2 / OpenID Connect integration
- Token refresh mechanism

### Monitoring

- Health endpoint: `GET /api/v1/health`
- Add Prometheus metrics via ZIO HTTP middleware
- Structured logging via zio-logging + SLF4J → Logback
- Request tracing with correlation IDs

### Security

- Enable CORS (currently not configured)
- Rate limiting on public endpoints
- Input validation (already in place via value objects)
- SQL injection prevention (via parameterized queries / Typo)
- HTTPS termination at load balancer

### Scaling

The application is stateless (except in-memory repos in dev mode):
- Horizontal scaling behind a load balancer
- Sticky sessions not required
- Share state via PostgreSQL
- Future: Redis for caching session data

### Frontend Deployment

```bash
# Build production JS bundle
sbt ui/fullLinkJS

# Output at: ui/target/scala-3.6.4/ui-opt/
# Serve via CDN or web server (Nginx, CloudFront, etc.)
```

Serve the frontend separately from the API:
- API: `api.example.com`
- Frontend: `app.example.com`
- Configure CORS headers on the API

## Migration Path

### In-Memory → PostgreSQL

1. Set up PostgreSQL instance
2. Run Flyway migrations: `sbt "app/runMain org.flywaydb.core.Flyway"`
3. Generate Typo code from schema
4. Implement PostgreSQL repository adapters (wrapping Typo repos)
5. Replace in-memory layers with PostgreSQL layers in `AppLayers`
6. Run integration tests to verify

### Single-Tenant → Multi-Tenant

1. Add `tenant_id` column to all tables (migration V7+)
2. Add composite indexes including `tenant_id`
3. Create `TenantContext` in ZIO environment
4. Scope all repository queries with tenant filter
5. Extract tenant from JWT claims in auth middleware
