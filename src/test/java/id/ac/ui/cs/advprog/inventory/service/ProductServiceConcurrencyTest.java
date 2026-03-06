package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.inventory.exception.WarConflictException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
    }

    @Test
    void reserveStock_concurrently_shouldKeepStockNonNegative() throws InterruptedException {
        int initialStock = 5;
        Product product = productRepository.save(Product.builder()
                .name("Limited Shoes")
                .description("drop")
                .price(new BigDecimal("99.99"))
                .stock(initialStock)
                .originLocation("Japan")
                .purchaseDate(LocalDate.of(2026, 3, 1))
                .jastiperId("jastiper1")
                .build());

        int threads = 10;
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executorService.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        try {
                            productService.reserveStock(product.getId(), 1);
                            successCount.incrementAndGet();
                        } catch (InsufficientStockException | WarConflictException ex) {
                            failedCount.incrementAndGet();
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            done.await();
        }

        Product persisted = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(threads, successCount.get() + failedCount.get());
        assertTrue(successCount.get() <= initialStock);
        assertTrue(persisted.getStock() >= 0);
        assertEquals(initialStock - successCount.get(), persisted.getStock());
    }
}
