# Payments Platform (Spring Boot Microservices)

A payment-processing system built with Spring Boot 3.4 and Java 17 that integrates PayPal (sandbox) through a separate provider service.

## Architecture

| Service | Port | Role |
|---|---|---|
| `eureka-service-registry` | 8761 | Service discovery (Netflix Eureka) |
| `payment-processing-service` | 8082 | Transaction lifecycle, MySQL persistence, status-processor pattern, Resilience4j circuit breaker |
| `paypal-provider-service` | 8083 | PayPal REST integration (OAuth token caching, create/capture order) |
| `infra/` | - | Docker Compose: services + Loki, Grafana, Alloy, MinIO (centralised logging) |

**Payment flow:** `CREATED -> INITIATED -> PENDING -> APPROVED -> SUCCESS / FAILED`
Each status change goes through a dedicated `TxnStatusProcessor` and is written to a transaction log table.

## Tech stack
Java 17, Spring Boot 3.4, Spring Cloud (Eureka, LoadBalancer), Resilience4j, Spring JDBC, MySQL, ModelMapper, Lombok, Docker, Grafana Loki.

## Run locally

**Prerequisites:** Java 17, Maven 3.9+, MySQL 8, PayPal sandbox credentials.

1. **Database:** edit the password in `payment-processing-service/src/main/resources/db/migration/v1__paymentdb_setup_ddl.sql` (replace `CHANGE_ME`), then run `v1` and `v2` in MySQL. (v1 drops and recreates the `payments` DB.)
2. **Environment variables:**
   ```bash
   export DB_PASSWORD=your_db_password
   export P_CLIENT_ID=your_paypal_client_id
   export P_CLIENT_SECRET=your_paypal_client_secret
   ```
3. **Start, in order:**
   ```bash
   cd eureka-service-registry && mvn spring-boot:run
   cd paypal-provider-service && mvn spring-boot:run
   cd payment-processing-service && mvn spring-boot:run
   ```
   Eureka dashboard: http://localhost:8761

## Run with Docker
```bash
# build jars and images (image names must match infra/docker-compose.yml)
for s in eureka-service-registry payment-processing-service paypal-provider-service; do
  (cd $s && mvn -DskipTests package && docker build -t $s .)
done
cd infra && cp .env.example .env   # fill in values
docker compose up
```
The `docker-local` profile expects MySQL on the host (`host.docker.internal:3306`).

## API (payment-processing-service)

| Method | Path | Description |
|---|---|---|
| POST | `/v1/payments` | Create a transaction, returns `txnReference` |
| POST | `/v1/payments/{txnReference}/initiate` | Create PayPal order, returns `redirectUrl` |
| POST | `/v1/payments/{txnReference}/capture` | Capture the approved order |

## Known limitations / roadmap
- State-transition guards in `AbstractTxnStatusProcessor` are commented out (marked TODO) and should be re-enabled.
- Kafka event on final status is a stub.
- Needs input validation, idempotency keys and unit tests for status processors.
- Secrets come from environment variables (local) or AWS Secrets Manager (`dev` profile).
