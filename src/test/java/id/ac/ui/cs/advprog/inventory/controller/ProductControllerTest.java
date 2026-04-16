package id.ac.ui.cs.advprog.inventory.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.ui.cs.advprog.inventory.config.InternalTokenAuthenticationFilter;
import id.ac.ui.cs.advprog.inventory.config.JwtAuthenticationFilter;
import id.ac.ui.cs.advprog.inventory.config.JwtService;
import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.exception.WarConflictException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.service.ProductService;

@WebMvcTest(ProductController.class)
@Import({
        id.ac.ui.cs.advprog.inventory.config.SecurityConfig.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        InternalTokenAuthenticationFilter.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=json-milestone-secret-json-milestone-secret",
        "app.internal-token=json-internal-token",
        "app.cors.allowed-origins=http://localhost:5173"
})
class ProductControllerTest {

    private static final String USER_JASTIPER = "jastiper1";
    private static final String USER_TITIPER = "titiper1";
    private static final String USER_ADMIN = "admin1";
    private static final String PRODUCT_NAME_BAG = "Bag";
    private static final String LOCATION_JAPAN = "Japan";
    private static final UUID PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME_BAG)
                .description("Travel bag")
                .price(new BigDecimal("120.00"))
                .stock(5)
                .originLocation(LOCATION_JAPAN)
                .purchaseDate(LocalDate.of(2026, 3, 1))
                .jastiperId(USER_JASTIPER)
                .version(1L)
                .build();
    }

    @Test
    @WithMockUser(username = USER_JASTIPER, roles = "JASTIPER")
    void createProduct_shouldWorkForJastiper() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(PRODUCT_NAME_BAG);
        request.setDescription("Travel bag");
        request.setPrice(new BigDecimal("120.00"));
        request.setStock(5);
        request.setOriginLocation(LOCATION_JAPAN);
        request.setPurchaseDate(LocalDate.of(2026, 3, 1));

        when(productService.create(any(ProductCreateRequest.class), eq(USER_JASTIPER)))
                .thenReturn(sampleProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME_BAG))
                .andExpect(jsonPath("$.jastiperId").value(USER_JASTIPER));
    }

    @Test
    @WithMockUser(username = USER_TITIPER, roles = "TITIPER")
    void createProduct_shouldBeForbiddenForTitiper() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(PRODUCT_NAME_BAG);
        request.setDescription("Travel bag");
        request.setPrice(new BigDecimal("120.00"));
        request.setStock(5);
        request.setOriginLocation(LOCATION_JAPAN);
        request.setPurchaseDate(LocalDate.of(2026, 3, 1));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(productService, never()).create(any(ProductCreateRequest.class), any());
    }

    @Test
    @WithMockUser(username = USER_JASTIPER, roles = "JASTIPER")
    void listMyProducts_shouldReturnOwnedProducts() throws Exception {
        when(productService.listOwnedBy(USER_JASTIPER)).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(PRODUCT_NAME_BAG))
                .andExpect(jsonPath("$[0].jastiperId").value(USER_JASTIPER));
    }

    @Test
    @WithMockUser(username = USER_TITIPER, roles = "TITIPER")
    void searchByProduct_shouldWorkForTitiper() throws Exception {
        when(productService.searchByProductName("bag")).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/search").param("keyword", "bag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(PRODUCT_NAME_BAG));
    }

    @Test
    @WithMockUser(username = USER_ADMIN, roles = "ADMIN")
    void monitorAllProducts_shouldWorkForAdmin() throws Exception {
        when(productService.listAll()).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(PRODUCT_NAME_BAG));
    }

    @Test
    @WithMockUser(username = USER_TITIPER, roles = "TITIPER")
    void getProductById_shouldWorkForTitiper() throws Exception {
        when(productService.getById(PRODUCT_ID)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/products/" + PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME_BAG))
                .andExpect(jsonPath("$.jastiperId").value(USER_JASTIPER));
    }

    @Test
    @WithMockUser(username = USER_ADMIN, roles = "ADMIN")
    void adminUpdateProduct_shouldUpdateProduct() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName(PRODUCT_NAME_BAG);
        request.setDescription("Updated");
        request.setPrice(new BigDecimal("150.00"));
        request.setStock(7);
        request.setOriginLocation(LOCATION_JAPAN);
        request.setPurchaseDate(LocalDate.of(2026, 3, 1));

        Product updated = Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME_BAG)
                .description("Updated")
                .price(new BigDecimal("150.00"))
                .stock(7)
                .originLocation(LOCATION_JAPAN)
                .purchaseDate(LocalDate.of(2026, 3, 1))
                .jastiperId(USER_JASTIPER)
                .version(2L)
                .build();
        when(productService.adminUpdateProduct(eq(PRODUCT_ID), any(ProductUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/products/admin/" + PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    @WithMockUser(username = "order-service", roles = "INTERNAL")
    void reserveStock_shouldReturnConflictWhenWarConflictOccurs() throws Exception {
        when(productService.reserveStock(PRODUCT_ID, 1)).thenThrow(new WarConflictException(PRODUCT_ID));

        mockMvc.perform(post("/api/products/" + PRODUCT_ID + "/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\", \"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WAR_CONFLICT"));
    }

    @Test
    @WithMockUser(username = USER_ADMIN, roles = "ADMIN")
    void adminDeleteProduct_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/admin/" + PRODUCT_ID))
                .andExpect(status().isNoContent());

        verify(productService).adminDeleteProduct(PRODUCT_ID);
    }

    @Test
    @WithMockUser(username = USER_JASTIPER, roles = "JASTIPER")
    void updateOwnProductShouldDelegateForOwner() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated");
        request.setDescription("Updated");
        request.setPrice(new BigDecimal("150.00"));
        request.setStock(3);
        request.setOriginLocation(LOCATION_JAPAN);
        request.setPurchaseDate(LocalDate.of(2026, 4, 1));
        Product updated = Product.builder()
                .id(PRODUCT_ID)
                .name("Updated")
                .description("Updated")
                .price(new BigDecimal("150.00"))
                .stock(3)
                .originLocation(LOCATION_JAPAN)
                .purchaseDate(LocalDate.of(2026, 4, 1))
                .jastiperId(USER_JASTIPER)
                .build();
        when(productService.updateOwnedProduct(eq(PRODUCT_ID), any(ProductUpdateRequest.class), eq(USER_JASTIPER)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/products/" + PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(username = USER_JASTIPER, roles = "JASTIPER")
    void deleteOwnProductShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/" + PRODUCT_ID))
                .andExpect(status().isNoContent());

        verify(productService).deleteOwnedProduct(PRODUCT_ID, USER_JASTIPER);
    }

    @Test
    @WithMockUser(username = USER_TITIPER, roles = "TITIPER")
    void searchByJastiperShouldReturnMatchingProducts() throws Exception {
        when(productService.listByJastiper(USER_JASTIPER)).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/jastipers/" + USER_JASTIPER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jastiperId").value(USER_JASTIPER));
    }

    @Test
    @WithMockUser(username = "order-service", roles = "INTERNAL")
    void inventoryDetailShouldReturnProductForInternalRole() throws Exception {
        when(productService.getById(PRODUCT_ID)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/products/inventory/" + PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()));
    }

    @Test
    @WithMockUser(username = "order-service", roles = "INTERNAL")
    void reduceStockShouldDelegateToReserveStock() throws Exception {
        when(productService.reserveStock(PRODUCT_ID, 2)).thenReturn(sampleProduct);

        mockMvc.perform(patch("/api/products/inventory/reduce-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\", \"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME_BAG));
    }

    @Test
    @WithMockUser(username = "order-service", roles = "INTERNAL")
    void restoreStockShouldDelegateToService() throws Exception {
        when(productService.restoreStock(PRODUCT_ID, 2)).thenReturn(sampleProduct);

        mockMvc.perform(patch("/api/products/inventory/restore-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + PRODUCT_ID + "\", \"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME_BAG));
    }

    @Test
    @WithMockUser(username = USER_JASTIPER, roles = "JASTIPER")
    void createProductShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "Travel bag",
                                  "price": 120.00,
                                  "stock": 0,
                                  "originLocation": "Japan",
                                  "purchaseDate": "2026-03-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
