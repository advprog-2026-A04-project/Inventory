# Inventory Service

Inventory and catalog service for Milestone `25%` and `50%`.

## Deployed URL

- `https://inventory-api-383620816191.us-central1.run.app`

## Implemented Scope

Buyer-facing endpoints:
- `GET /api/products/search`
- `GET /api/products/{productId}`

Internal checkout endpoints:
- `GET /api/products/inventory/{productId}`
- `PATCH /api/products/inventory/reduce-stock`
- `PATCH /api/products/inventory/restore-stock`
- `POST /api/products/{productId}/reserve`

The service uses JWT auth for browser clients and `X-Internal-Token` for Order-to-Inventory calls.

## Local Run

Prerequisites:
- Java `21`

Run:

```bash
./gradlew bootRun
```

PowerShell:

```powershell
.\gradlew.bat bootRun
```

Default local URL:
- `http://localhost:8080`

## Environment Variables

- `PORT`
- `DB_URL`
- `DB_DRIVER`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`
- `JWT_SECRET`
- `INTERNAL_API_TOKEN`

Defaults are configured for an H2 file database under `/tmp`.

## Test

```bash
./gradlew test
```

Includes:
- controller tests
- integration flow tests
- stock reservation concurrency coverage

## Cloud Run Deploy

```bash
gcloud run deploy inventory-api --source . --region us-central1 --allow-unauthenticated --max-instances=1 \
  --set-env-vars APP_CORS_ALLOWED_ORIGINS=https://advprog-frontend-m25-m50-383620816191.us-central1.run.app \
  --set-env-vars JWT_SECRET=<shared-jwt-secret> \
  --set-env-vars INTERNAL_API_TOKEN=<shared-internal-token>
```

## Notes

- Seeded catalog data is inserted automatically for the milestone demo.
- Stock changes are guarded so stock does not go negative under concurrent access.
