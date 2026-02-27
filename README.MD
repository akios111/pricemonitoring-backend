# <center>PriceMonitoring Backend</center>

## Quick start (Docker only, recommended)
### Prerequisites
- Docker Desktop (with `docker compose`)

### Run (backend + mysql + rabbitmq)
From the `PriceMonitoringBackend` folder:

```bash
docker compose -f docker-compose.backend.yml up -d --build
```

### Run from Docker Hub (no local build)
```bash
docker compose -f docker-compose.dockerhub.yml up -d
```

Check it's up:

```bash
curl http://localhost:8085/actuator/health
```

Stop:

```bash
docker compose -f docker-compose.backend.yml down
```

Reset DB (drops MySQL volume):

```bash
docker compose -f docker-compose.backend.yml down -v
```

### Notes about email verification (dev profile)
When running with `SPRING_PROFILES_ACTIVE=dev`, the backend uses a no-op email service. The verification JWT is **logged** by the backend instead of being sent via email.

## Existing full stack compose
This repository also contains a larger `docker-compose.yml` with extra services (grafana/prometheus/loki/scrapper). Use it only if you need the whole stack.

## Run backend locally (without building Docker image)
This is the most reliable option on Windows if Docker image builds fail.

From the `PriceMonitoringBackend` folder:

```bash
docker compose up -d mysql rabbitmq
./gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

Check it's up:

```bash
curl http://localhost:8085/actuator/health
```

## Ports (default)
- MySQL: `13306` (host) → `3306` (container)
- RabbitMQ: `5672`, `15672` (UI), `15692`
- Backend: `8085`

### Database
docker-compose.yml defines the price_monitoring database that is created after container is started.

### Connecting to mysql
docker-compose.yml specifies the root password with MYSQL_ROOT_PASSWORD env. variable.   
If you want to connect to the mysql server, follow these steps:
1. Run (sudo) `docker compose exec mysql bash`.
2. Run `mysql -u root -p` and provide root password.
