package id.ac.ui.cs.advprog.inventory.config;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CatalogSeedConfig {

    @Bean
    public CommandLineRunner seedCatalog(JdbcTemplate jdbcTemplate) {
        return args -> {
            Integer existingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
            if (existingCount != null && existingCount > 0) {
                return;
            }

            LocalDate purchaseDate = LocalDate.of(2026, 4, 1);
            LocalDateTime now = LocalDateTime.now();

            insertProduct(jdbcTemplate, product(
                    "11111111-1111-1111-1111-111111111111",
                    "Nike SB Dunk Low Travis Scott",
                    "Limited sneakers for milestone demo.",
                    "4500000.00",
                    12,
                    "United States",
                    "2001",
                    purchaseDate,
                    now
            ));
            insertProduct(jdbcTemplate, product(
                    "22222222-2222-2222-2222-222222222222",
                    "Adidas Samba OG Wales Bonner",
                    "Vintage sneakers for wallet validation.",
                    "3250000.00",
                    9,
                    "United Kingdom",
                    "2001",
                    purchaseDate,
                    now
            ));
            insertProduct(jdbcTemplate, product(
                    "33333333-3333-3333-3333-333333333333",
                    "Coldplay Concert Ticket CAT 1",
                    "Concert ticket for successful checkout.",
                    "2100000.00",
                    6,
                    "Singapore",
                    "2002",
                    purchaseDate,
                    now
            ));
            insertProduct(jdbcTemplate, product(
                    "44444444-4444-4444-4444-444444444444",
                    "Taylor Swift The Eras Tour Ticket",
                    "Low-stock item for inventory validation.",
                    "6850000.00",
                    3,
                    "Japan",
                    "2002",
                    purchaseDate,
                    now
            ));
            insertProduct(jdbcTemplate, product(
                    "55555555-5555-5555-5555-555555555555",
                    "Dior Addict Lip Glow Set",
                    "Beauty product for a lower-value happy path.",
                    "1350000.00",
                    20,
                    "France",
                    "2003",
                    purchaseDate,
                    now
            ));
            insertProduct(jdbcTemplate, product(
                    "66666666-6666-6666-6666-666666666666",
                    "Rare Sonny Angel Winter Wonderland",
                    "Collectible for milestone 25% browsing and checkout.",
                    "780000.00",
                    15,
                    "South Korea",
                    "2003",
                    purchaseDate,
                    now
            ));
        };
    }

    private void insertProduct(JdbcTemplate jdbcTemplate, ProductSeed seed) {
        jdbcTemplate.update("""
                        INSERT INTO products (
                            id, name, description, price, stock, origin_location, purchase_date,
                            jastiper_id, version, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                seed.id(),
                seed.name(),
                seed.description(),
                seed.price(),
                seed.stock(),
                seed.originLocation(),
                seed.purchaseDate(),
                seed.jastiperId(),
                0L,
                Timestamp.valueOf(seed.createdAt()),
                Timestamp.valueOf(seed.updatedAt())
        );
    }

    private ProductSeed product(
            String id,
            String name,
            String description,
            String price,
            int stock,
            String originLocation,
            String jastiperId,
            LocalDate purchaseDate,
            LocalDateTime now
    ) {
        return new ProductSeed(
                UUID.fromString(id),
                name,
                description,
                new BigDecimal(price),
                stock,
                originLocation,
                purchaseDate,
                jastiperId,
                now,
                now
        );
    }

    private record ProductSeed(
            UUID id,
            String name,
            String description,
            BigDecimal price,
            int stock,
            String originLocation,
            LocalDate purchaseDate,
            String jastiperId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
