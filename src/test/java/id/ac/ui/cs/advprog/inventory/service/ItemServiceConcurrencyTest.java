package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.model.Item;
import id.ac.ui.cs.advprog.inventory.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ItemServiceConcurrencyTest {

    @Autowired
    private ItemService service;

    @Autowired
    private ItemRepository repo;

    @AfterEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    void reserve_concurrently_shouldNotOversell() throws Exception {

        Item item = repo.save(Item.builder()
                .name("Sepatu")
                .description("desc")
                .price(100.0)
                .stock(5)
                .originCountry("Indonesia")
                .purchaseDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(1))
                .jastiperId("demo")
                .build());

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();

                    try {
                        service.reserve(item.getId(), 1);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    }

                } catch (InterruptedException ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();

        executor.shutdownNow();

        assertEquals(5, success.get());
        assertEquals(5, failed.get());

        Item after = repo.findById(item.getId()).orElseThrow();
        assertEquals(0, after.getStock());
    }
}