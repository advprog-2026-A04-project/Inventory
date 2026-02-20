package id.ac.ui.cs.advprog.inventory.integration;

import id.ac.ui.cs.advprog.inventory.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository repository;

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void createThenList_shouldPersistAndRenderItem() throws Exception {
        mockMvc.perform(post("/items")
                        .param("name", "Bag E2E")
                        .param("description", "End to end")
                        .param("price", "99")
                        .param("stock", "2")
                        .param("jastiperId", "demo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?jastiperId=demo"));

        mockMvc.perform(get("/items").param("jastiperId", "demo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bag E2E")))
                .andExpect(content().string(containsString("demo")));
    }
}
