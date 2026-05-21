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

Runtime H2 defaults have been removed. Set these variables to a PostgreSQL or
Cloud SQL PostgreSQL connection before running the service outside tests.

## Test

```bash
./gradlew test
```

Includes:
- controller tests
- integration flow tests
- stock reservation concurrency coverage

## Cloud Run Deploy

### Local deploy

```bash
gcloud run deploy inventory-api --source . --region us-central1 --allow-unauthenticated --max-instances=1 \
  --set-env-vars APP_CORS_ALLOWED_ORIGINS=https://advprog-frontend-m25-m50-383620816191.us-central1.run.app \
  --set-env-vars DB_URL=<postgres-or-cloud-sql-jdbc-url> \
  --set-env-vars DB_USERNAME=<db-user> \
  --set-env-vars DB_PASSWORD=<db-password> \
  --set-env-vars JWT_SECRET=<shared-jwt-secret> \
  --set-env-vars INTERNAL_API_TOKEN=<shared-internal-token>
```

### GitHub Actions deploy

The CD workflow uses GitHub Actions OIDC with Google Cloud Workload Identity Federation. Do not use `GCP_SA_KEY`: JSON service account key creation is intentionally blocked by Google Cloud organization policy through `constraints/iam.disableServiceAccountKeyCreation`, and long-lived service account JSON keys should not be recreated or committed.

Required GitHub repository or environment secrets:

- `GCP_PROJECT_ID`
- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_SERVICE_ACCOUNT`
- `APP_CORS_ALLOWED_ORIGINS`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `INTERNAL_API_TOKEN`

The workflow grants GitHub Actions OIDC access through:

```yaml
permissions:
  contents: read
  id-token: write
```

The Google Cloud authentication step uses:

```yaml
- name: Authenticate to Google Cloud
  uses: google-github-actions/auth@v3
  with:
    project_id: ${{ secrets.GCP_PROJECT_ID }}
    workload_identity_provider: ${{ secrets.GCP_WORKLOAD_IDENTITY_PROVIDER }}
    service_account: ${{ secrets.GCP_SERVICE_ACCOUNT }}
```

Current project values used by the demo deployment:

- `GCP_PROJECT_ID=project-58e5335e-d6a4-4499-b08`
- `GCP_WORKLOAD_IDENTITY_PROVIDER=projects/383620816191/locations/global/workloadIdentityPools/github-pool/providers/github-provider`
- `GCP_SERVICE_ACCOUNT=cloudrun-deployer@project-58e5335e-d6a4-4499-b08.iam.gserviceaccount.com`

## Notes

- Seeded catalog data is inserted automatically for the milestone demo.
- Stock changes are guarded so stock does not go negative under concurrent access.
