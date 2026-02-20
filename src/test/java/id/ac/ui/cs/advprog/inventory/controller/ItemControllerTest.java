package id.ac.ui.cs.advprog.inventory.controller;

import id.ac.ui.cs.advprog.inventory.model.Item;
import id.ac.ui.cs.advprog.inventory.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService service;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = Item.builder()
                .id(1L)
                .name("Bag")
                .description("Travel bag")
                .price(120.0)
                .stock(5)
                .originCountry("ID")
                .purchaseDate(LocalDate.of(2025, 1, 1))
                .returnDate(LocalDate.of(2025, 1, 10))
                .jastiperId("demo")
                .build();
    }

    @Test
    void listByJastiper_shouldRenderItemsPage() throws Exception {
        when(service.listByJastiper("demo")).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/items").param("jastiperId", "demo"))
                .andExpect(status().isOk())
                .andExpect(view().name("Items"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attribute("jastiperId", "demo"));

        verify(service).listByJastiper("demo");
    }

    @Test
    void newForm_shouldRenderCreateForm() throws Exception {
        mockMvc.perform(get("/items/new").param("jastiperId", "demo"))
                .andExpect(status().isOk())
                .andExpect(view().name("ItemForm"))
                .andExpect(model().attributeExists("req"));
    }

    @Test
    void create_shouldRedirectWhenRequestValid() throws Exception {
        when(service.create(any())).thenReturn(sampleItem);

        mockMvc.perform(post("/items")
                        .param("name", "Bag")
                        .param("description", "Travel bag")
                        .param("price", "120")
                        .param("stock", "5")
                        .param("jastiperId", "demo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?jastiperId=demo"));

        verify(service).create(any());
    }

    @Test
    void create_shouldReturnFormWhenValidationFails() throws Exception {
        mockMvc.perform(post("/items")
                        .param("name", "")
                        .param("description", "")
                        .param("jastiperId", "demo"))
                .andExpect(status().isOk())
                .andExpect(view().name("ItemForm"));

        verify(service, never()).create(any());
    }

    @Test
    void editForm_shouldRenderEditForm() throws Exception {
        when(service.getById(1L)).thenReturn(sampleItem);

        mockMvc.perform(get("/items/1/edit").param("jastiperId", "demo"))
                .andExpect(status().isOk())
                .andExpect(view().name("ItemEdit"))
                .andExpect(model().attribute("id", 1L))
                .andExpect(model().attribute("jastiperId", "demo"))
                .andExpect(model().attributeExists("req"));
    }

    @Test
    void update_shouldRedirectWhenRequestValid() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(sampleItem);

        mockMvc.perform(post("/items/1")
                        .param("jastiperId", "demo")
                        .param("description", "Updated")
                        .param("price", "150")
                        .param("stock", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?jastiperId=demo"));

        verify(service).update(eq(1L), any());
    }

    @Test
    void update_shouldReturnEditFormWhenValidationFails() throws Exception {
        mockMvc.perform(post("/items/1")
                        .param("jastiperId", "demo")
                        .param("description", "")
                        .param("price", "-1")
                        .param("stock", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("ItemEdit"));

        verify(service, never()).update(eq(1L), any());
    }

    @Test
    void delete_shouldRedirectToItemsPage() throws Exception {
        mockMvc.perform(post("/items/1/delete").param("jastiperId", "demo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?jastiperId=demo"));

        verify(service).delete(1L);
    }

    @Test
    void search_shouldRenderSearchPage() throws Exception {
        when(service.searchByName("bag")).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/items/search").param("keyword", "bag"))
                .andExpect(status().isOk())
                .andExpect(view().name("Search"))
                .andExpect(model().attribute("keyword", "bag"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void search_shouldRenderSearchPageWithEmptyKeywordWhenNull() throws Exception {
        when(service.searchByName(null)).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("Search"))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void reserve_shouldRedirectWhenSuccessful() throws Exception {
        mockMvc.perform(post("/items/1/reserve")
                        .param("qty", "1")
                        .param("jastiperId", "demo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?jastiperId=demo"));

        verify(service).reserve(1L, 1);
    }

    @Test
    void reserve_shouldRenderItemsWithErrorWhenFailed() throws Exception {
        doThrow(new IllegalStateException("Insufficient stock"))
                .when(service).reserve(1L, 10);
        when(service.listByJastiper("demo")).thenReturn(List.of(sampleItem));

        mockMvc.perform(post("/items/1/reserve")
                        .param("qty", "10")
                        .param("jastiperId", "demo"))
                .andExpect(status().isOk())
                .andExpect(view().name("Items"))
                .andExpect(model().attribute("jastiperId", "demo"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attribute("error", "Insufficient stock"));
    }

    @Test
    void listAll_shouldRenderErrorForNonAdmin() throws Exception {
        mockMvc.perform(get("/items/all").param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("Error"))
                .andExpect(model().attribute("message", "Forbidden: ADMIN only"));
    }

    @Test
    void listAll_shouldRenderItemsForAdmin() throws Exception {
        when(service.listAll()).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/items/all").param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(view().name("ItemsAll"))
                .andExpect(model().attributeExists("items"));

        verify(service).listAll();
    }
}
