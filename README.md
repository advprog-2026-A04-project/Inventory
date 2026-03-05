# Inventory Service (JSON)

Spring Boot 3.x microservice untuk modul Inventory & Katalog pada proyek **JaStip Online Nasional (JSON)**.

Service ini menangani:
- Manajemen katalog produk oleh Jastiper
- Pencarian produk oleh Titiper
- Monitoring katalog oleh Admin
- Pengurangan stok aman saat mode "War" dengan optimistic locking

## Tech Stack
- Java 21
- Spring Boot 3.5.x (`web`, `validation`, `data-jpa`, `security`)
- MySQL 8 (runtime)
- H2 (test)
- JUnit 5, MockMvc

## Fitur Utama
- Entity `Product` dengan field: `id`, `name`, `description`, `price`, `stock`, `originLocation`, `purchaseDate`, `jastiperId`
- `@Version` untuk optimistic locking (anti overselling)
- API JSON berbasis RBAC (`TITIPER`, `JASTIPER`, `ADMIN`)
- Service `reserveStock(productId, quantity)` dengan `@Transactional`
- Global exception handler untuk response error JSON konsisten

## Menjalankan Lokal
1. Siapkan MySQL, lalu jalankan script:
   - `docs/sql/mysql_reset_inventory_db.sql`
2. Set environment variable (PowerShell):
   ```powershell
   $env:DB_USERNAME="inventory_app"
   $env:DB_PASSWORD="inventory_pass"
   ./gradlew bootRun
   ```
3. Service jalan di:
   - `http://localhost:8085`

Konfigurasi datasource menggunakan env var di `src/main/resources/application.properties`:
- `DB_URL` (default: `jdbc:mysql://localhost:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default kosong)

## Kredensial RBAC (Development)
Autentikasi saat ini masih local/in-memory untuk development:
- `titiper1 / titiper123` -> role `TITIPER`
- `jastiper1 / jastiper123` -> role `JASTIPER`
- `admin1 / admin123` -> role `ADMIN`

Gunakan Basic Auth saat akses endpoint.

## API Endpoint
- `POST /api/products` (`JASTIPER`) create produk
- `PUT /api/products/{productId}` (`JASTIPER`, owner only) update produk sendiri
- `DELETE /api/products/{productId}` (`JASTIPER`, owner only) delete produk sendiri
- `GET /api/products/me` (`JASTIPER`) list katalog milik sendiri
- `GET /api/products/search?keyword=...` (`TITIPER/JASTIPER/ADMIN`) search by product name
- `GET /api/products/jastipers/{jastiperId}` (`TITIPER/JASTIPER/ADMIN`) search by jastiper
- `GET /api/products` (`ADMIN`) monitor semua produk
- `PUT /api/products/admin/{productId}` (`ADMIN`) update produk apa pun
- `DELETE /api/products/admin/{productId}` (`ADMIN`) delete produk apa pun
- `POST /api/products/{productId}/reserve` (`TITIPER/JASTIPER/ADMIN`) reserve stock

## War Mode & Concurrency
- Method service: `reserveStock(Long productId, int quantity)`
- Transaksional: `@Transactional`
- Jika stok kurang: `InsufficientStockException`
- Jika kalah race condition: `WarConflictException` (mapped dari `OptimisticLockingFailureException`)

## Test & Quality
Jalankan:
```powershell
./gradlew test
./gradlew check
```

Mencakup:
- Unit test service
- Controller test RBAC + API response
- Integration test flow utama
- Concurrency test reserve stock
- PMD, Checkstyle, JaCoCo
