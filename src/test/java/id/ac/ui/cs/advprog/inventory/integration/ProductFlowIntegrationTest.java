package id.ac.ui.cs.advprog.inventory.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.inventory.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ProductFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
    }

    @Test
    void createSearchAndReserve_shouldWork() throws Exception {
        String createPayload = """
                {
                  "name": "Bag E2E",
                  "description": "End to end",
                  "price": 99.50,
                  "stock": 2,
                  "originLocation": "Japan",
                  "purchaseDate": "2026-03-01"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.user("jastiper1").roles("JASTIPER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bag E2E"))
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String productId = createBody.get("id").asText();

        mockMvc.perform(get("/api/products/search")
                        .with(SecurityMockMvcRequestPostProcessors.user("titiper1").roles("TITIPER"))
                        .param("keyword", "bag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bag E2E"));

        mockMvc.perform(post("/api/products/" + productId + "/reserve")
                        .with(SecurityMockMvcRequestPostProcessors.user("order-service").roles("INTERNAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\", \"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(1));
    }

    @Test
    void reduceAndRestoreStock_shouldBeIdempotentPerRequestId() throws Exception {
        String createPayload = """
                {
                  "name": "Bag Idempotent",
                  "description": "Mutation retries",
                  "price": 150.00,
                  "stock": 3,
                  "originLocation": "Japan",
                  "purchaseDate": "2026-03-01"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.user("jastiper1").roles("JASTIPER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String productId = createBody.get("id").asText();

        String reducePayload = """
                {
                  "productId": "%s",
                  "quantity": 2,
                  "orderId": "301",
                  "requestId": "reduce-301-%s"
                }
                """.formatted(productId, productId);

        mockMvc.perform(patch("/api/products/inventory/reduce-stock")
                        .with(SecurityMockMvcRequestPostProcessors.user("order-service").roles("INTERNAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reducePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(1));

        mockMvc.perform(patch("/api/products/inventory/reduce-stock")
                        .with(SecurityMockMvcRequestPostProcessors.user("order-service").roles("INTERNAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reducePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(1));

        String restorePayload = """
                {
                  "productId": "%s",
                  "quantity": 2,
                  "orderId": "301",
                  "requestId": "restore-301-%s"
                }
                """.formatted(productId, productId);

        mockMvc.perform(patch("/api/products/inventory/restore-stock")
                        .with(SecurityMockMvcRequestPostProcessors.user("order-service").roles("INTERNAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restorePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));

        mockMvc.perform(patch("/api/products/inventory/restore-stock")
                        .with(SecurityMockMvcRequestPostProcessors.user("order-service").roles("INTERNAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restorePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
    }
}
