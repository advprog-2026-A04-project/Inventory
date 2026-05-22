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

## Software Quality

Project ini juga disiapkan untuk memenuhi rubrik **software quality** melalui beberapa teknik berikut:

### Clean Code

- Logika bisnis dipisahkan ke `ProductService`, `ProductMutationMapper`, `StockMutationIdempotencyService`, dan strategi stok.
- Error handling dipusatkan di `ApiExceptionHandler` agar respons API konsisten.
- Validasi request dilakukan di DTO dan service untuk menjaga input tetap aman.

### Unit Testing

- `ProductServiceTest`
- `ProductMutationMapperTest`
- `JwtServiceTest`
- `SecurityConfigTest`

Test ini memverifikasi perilaku unit kecil secara terisolasi dengan mock.

### Functional / Integration Testing

- `ProductFlowIntegrationTest`

Test ini menjalankan alur end-to-end dengan `MockMvc`, mulai dari create, search, reserve, reduce-stock, sampai restore-stock.

### Regression Testing

- `ProductServiceConcurrencyTest`
- idempotency coverage pada `ProductFlowIntegrationTest`

Test ini menjaga agar perbaikan tidak merusak perilaku lama, terutama untuk concurrency, optimistic locking, dan retry request.

### Secure Coding

- JWT dipakai untuk akses browser client.
- `X-Internal-Token` dipakai untuk komunikasi internal Order-to-Inventory.
- `ForbiddenProductAccessException` membatasi akses owner vs non-owner.
- `InsufficientStockException` dan optimistic locking mencegah stok menjadi negatif.
- Workflow OSSF Scorecard disediakan di `.github/workflows/scorecard.yml` untuk pemeriksaan supply-chain security.
- Workflow ini diambil dari template resmi GitHub/OSSF Scorecard dan dipakai sebagai bukti tooling security tambahan.

### Profiling / Monitoring

- Spring Boot Actuator diaktifkan.
- Endpoint metrics diekspos lewat `/actuator/metrics`.

Contoh endpoint yang bisa dipakai untuk observasi performa:

- `/actuator/metrics/http.server.requests`
- `/actuator/metrics/jvm.memory.used`

Dengan metrik tersebut, performa request dan penggunaan memori bisa dianalisis saat dibutuhkan.

### Laporan Pencapaian

Seluruh kriteria pada poin ini sudah divalidasi dengan `./gradlew checkCoverage` dan berhasil melewati ambang **90%** untuk coverage yang diminta oleh konfigurasi JaCoCo.

## Software Deployment

Project ini juga sudah menerapkan rubrik **software deployment** melalui pipeline dan observability berikut:

### CI/CD

- `CI` workflow menjalankan test, JaCoCo coverage verification, PMD, dan Checkstyle.
- `CD` workflow melakukan deployment otomatis ke Cloud Run setelah `CI` sukses.
- Deployment dipisah untuk branch `staging` dan `main`, sehingga alur release lebih aman dan terkontrol.

### Containerized Deployment

- Aplikasi dibangun menggunakan multi-stage [Dockerfile](Dockerfile) agar image runtime tetap kecil.
- Runtime memakai JRE 21, sedangkan proses build memakai JDK 21.
- Deployment Cloud Run menggunakan environment variables untuk konfigurasi database, JWT, dan token internal.

### Provisioning dan Environment

- Cloud Run dipakai sebagai target deployment utama.
- Konfigurasi environment disiapkan lewat `APP_CORS_ALLOWED_ORIGINS`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, dan `INTERNAL_API_TOKEN`.
- Branch `staging` dan `main` masing-masing bisa dipakai untuk deployment staging dan production.

### Monitoring dan Observability

- Spring Boot Actuator diaktifkan untuk endpoint health, info, metrics, dan prometheus.
- Metrics bisa diambil dari `/actuator/metrics` dan `/actuator/prometheus`.
- Endpoint ini bisa dipakai untuk dashboard monitoring di sistem eksternal seperti Prometheus/Grafana.

### Quality Guard Saat Deployment

- CI menolak perubahan yang gagal test atau tidak lolos coverage, PMD, dan Checkstyle.
- Jadi deployment hanya berjalan setelah quality gate terpenuhi.

## Implementasi Design Pattern

Project ini sudah memenuhi deskripsi tugas karena mengimplementasikan minimal **3 design pattern** pada proses manajemen stok:

### 1. Strategy Pattern

Dipakai untuk memisahkan perilaku perubahan stok berdasarkan jenis mutasi.

- `ReduceStockStrategy` → mengurangi stok dan memvalidasi agar stok tidak menjadi negatif.
- `RestoreStockStrategy` → menambah stok kembali.

Dengan pola ini, logika `reduce` dan `restore` tidak bercampur di satu metode besar, sehingga lebih mudah dikembangkan jika nanti ada jenis mutasi stok lain.

### 2. Factory Pattern

`StockMutationStrategyFactory` bertugas memilih strategi yang sesuai berdasarkan `StockMutationType`.

Alur yang dipakai:

1. `ProductService` menerima permintaan mutasi stok.
2. Service meminta strategi ke `StockMutationStrategyFactory`.
3. Factory mengembalikan implementasi yang sesuai (`ReduceStockStrategy` atau `RestoreStockStrategy`).
4. Strategi tersebut mengeksekusi perubahan stok.

Pola ini membuat `ProductService` tidak perlu tahu detail implementasi tiap jenis mutasi.

### 3. Observer Pattern

Observer dipakai untuk memberi notifikasi ketika stok produk habis.

- `OutOfStockEvent` menjadi event/pesan yang dikirim.
- `OutOfStockEventListener` menerima event tersebut dan menjalankan aksi lanjutan (misalnya logging atau notifikasi ke sistem lain).

Saat stok hasil mutasi menjadi `0`, `ProductService` akan mem-publish event, lalu listener akan menanganinya tanpa mengganggu alur utama service.

### Ringkasan Alur Implementasi

Untuk proses `reduce-stock` / `restore-stock`:

1. `ProductService` menerima request.
2. Service melakukan validasi data.
3. Service mengambil strategi dari `StockMutationStrategyFactory`.
4. Strategi menjalankan perubahan stok.
5. Jika stok menjadi habis, service mem-publish `OutOfStockEvent`.
6. `OutOfStockEventListener` menerima event dan menjalankan respons yang diperlukan.

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
