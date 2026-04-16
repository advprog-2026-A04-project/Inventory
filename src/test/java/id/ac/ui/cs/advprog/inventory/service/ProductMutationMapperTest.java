package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductMutationMapperTest {

    private final ProductMutationMapper mapper = new ProductMutationMapper();

    @Test
    void fromCreateRequestShouldPopulateProductFields() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Shoes");
        request.setDescription("Limited drop");
        request.setPrice(new BigDecimal("150000"));
        request.setStock(3);
        request.setOriginLocation("Japan");
        request.setPurchaseDate(LocalDate.of(2026, 4, 1));

        Product product = mapper.fromCreateRequest(request, "jastiper-1");

        assertEquals("Shoes", product.getName());
        assertEquals("Limited drop", product.getDescription());
        assertEquals(new BigDecimal("150000"), product.getPrice());
        assertEquals(3, product.getStock());
        assertEquals("Japan", product.getOriginLocation());
        assertEquals(LocalDate.of(2026, 4, 1), product.getPurchaseDate());
        assertEquals("jastiper-1", product.getJastiperId());
    }

    @Test
    void applyUpdateShouldOverwriteMutableFields() {
        Product product = Product.builder()
                .name("Old")
                .description("Old desc")
                .price(new BigDecimal("1"))
                .stock(1)
                .originLocation("Old")
                .purchaseDate(LocalDate.of(2026, 1, 1))
                .jastiperId("jastiper")
                .build();
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("New");
        request.setDescription("Fresh");
        request.setPrice(new BigDecimal("250000"));
        request.setStock(8);
        request.setOriginLocation("Korea");
        request.setPurchaseDate(LocalDate.of(2026, 5, 1));

        mapper.applyUpdate(product, request);

        assertEquals("New", product.getName());
        assertEquals("Fresh", product.getDescription());
        assertEquals(new BigDecimal("250000"), product.getPrice());
        assertEquals(8, product.getStock());
        assertEquals("Korea", product.getOriginLocation());
        assertEquals(LocalDate.of(2026, 5, 1), product.getPurchaseDate());
    }
}
