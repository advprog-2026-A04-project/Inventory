package id.ac.ui.cs.advprog.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.OptimisticLockingFailureException;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.exception.ForbiddenProductAccessException;
import id.ac.ui.cs.advprog.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.inventory.exception.WarConflictException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.repository.ProductRepository;

class ProductServiceTest {

    private static final String JASTIPER_1 = "jastiper1";

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void create_shouldSaveProductWithActorAsOwner() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Sushi");
        request.setDescription("Fresh sushi");
        request.setPrice(new BigDecimal("120000"));
        request.setStock(5);
        request.setOriginLocation("Japan");
        request.setPurchaseDate(LocalDate.of(2026, 3, 1));

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.create(request, JASTIPER_1);

        assertEquals("Sushi", created.getName());
        assertEquals(JASTIPER_1, created.getJastiperId());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void listOwnedBy_shouldReturnProductsOfOwner() {
        Product product = Product.builder().id(UUID.randomUUID()).jastiperId(JASTIPER_1).build();
        when(productRepository.findAllByJastiperId(JASTIPER_1)).thenReturn(List.of(product));

        List<Product> products = productService.listOwnedBy(JASTIPER_1);

        assertEquals(1, products.size());
        verify(productRepository).findAllByJastiperId(JASTIPER_1);
    }

    @Test
    void updateOwnedProduct_shouldThrowWhenUserIsNotOwner() {
        UUID productId = UUID.fromString("00000000-0000-0000-0000-000000000007");
        Product existing = Product.builder().id(productId).jastiperId("other").build();
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated Name");
        request.setDescription("updated");
        request.setPrice(new BigDecimal("11"));
        request.setStock(2);
        request.setOriginLocation("Korea");
        request.setPurchaseDate(LocalDate.of(2026, 4, 1));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        assertThrows(
                ForbiddenProductAccessException.class,
                () -> productService.updateOwnedProduct(productId, request, JASTIPER_1)
        );
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void reserveStock_shouldThrowWhenInsufficient() {
        UUID productId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Product existing = Product.builder().id(productId).stock(1).jastiperId(JASTIPER_1).build();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(existing));

        assertThrows(InsufficientStockException.class, () -> productService.reserveStock(productId, 2));
        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void reserveStock_shouldThrowWarConflictOnOptimisticFailure() {
        UUID productId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Product existing = Product.builder().id(productId).stock(5).jastiperId(JASTIPER_1).build();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(existing));
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThrows(WarConflictException.class, () -> productService.reserveStock(productId, 1));
    }
}
