package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ItemCreateRequest;
import id.ac.ui.cs.advprog.inventory.model.Item;
import id.ac.ui.cs.advprog.inventory.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    private ItemRepository repo;
    private ItemService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(ItemRepository.class);
        service = new ItemService(repo);
    }

    @Test
    void createItem_shouldSaveToRepository() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setName("Test");
        req.setDescription("Desc");
        req.setPrice(100.0);
        req.setStock(5);
        req.setJastiperId("demo");

        when(repo.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        Item saved = service.create(req);

        assertEquals("Test", saved.getName());
        verify(repo, times(1)).save(any(Item.class));
    }

    @Test
    void reserve_shouldThrowException_whenStockInsufficient() {
        when(repo.decreaseStockIfAvailable(1L, 10)).thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> service.reserve(1L, 10));
    }
}