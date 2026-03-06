package id.ac.ui.cs.advprog.inventory.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.exception.WarConflictException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.service.ProductService;

@WebMvcTest(ProductController.class)
@Import(id.ac.ui.cs.advprog.inventory.config.SecurityConfig.class)
class ProductControllerTest {

    private static final String USER_JASTIPER = "jastiper1";
    private static final String USER_TITIPER = "titiper1";
    private static final String USER_ADMIN = "admin1";
    private static final String PRODUCT_NAME_BAG = "Bag";
    private static final String LOCATION_JAPAN = "Japan";

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
                .id(1L)
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
        when(productService.getById(1L)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/products/1"))
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
                .id(1L)
                .name(PRODUCT_NAME_BAG)
                .description("Updated")
                .price(new BigDecimal("150.00"))
                .stock(7)
                .originLocation(LOCATION_JAPAN)
                .purchaseDate(LocalDate.of(2026, 3, 1))
                .jastiperId(USER_JASTIPER)
                .version(2L)
                .build();
        when(productService.adminUpdateProduct(eq(1L), any(ProductUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/products/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    @WithMockUser(username = USER_TITIPER, roles = "TITIPER")
    void reserveStock_shouldReturnConflictWhenWarConflictOccurs() throws Exception {
        when(productService.reserveStock(1L, 1)).thenThrow(new WarConflictException(1L));

        mockMvc.perform(post("/api/products/1/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WAR_CONFLICT"));
    }

    @Test
    @WithMockUser(username = USER_ADMIN, roles = "ADMIN")
    void adminDeleteProduct_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/admin/1"))
                .andExpect(status().isNoContent());

        verify(productService).adminDeleteProduct(1L);
    }
}
