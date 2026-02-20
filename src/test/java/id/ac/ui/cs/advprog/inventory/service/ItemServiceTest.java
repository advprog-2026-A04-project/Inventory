package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ItemCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ItemUpdateRequest;
import id.ac.ui.cs.advprog.inventory.model.Item;
import id.ac.ui.cs.advprog.inventory.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
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

    @Test
    void reserve_shouldThrowException_whenQtyNonPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> service.reserve(1L, 0));
        verify(repo, never()).decreaseStockIfAvailable(anyLong(), anyInt());
    }

    @Test
    void listByJastiper_shouldReturnRepositoryResult() {
        Item item = Item.builder().id(1L).jastiperId("demo").build();
        when(repo.findByJastiperId("demo")).thenReturn(List.of(item));

        List<Item> result = service.listByJastiper("demo");

        assertEquals(1, result.size());
        assertEquals("demo", result.get(0).getJastiperId());
        verify(repo).findByJastiperId("demo");
    }

    @Test
    void searchByName_shouldUseEmptyKeywordWhenNull() {
        when(repo.searchByName("")).thenReturn(List.of());

        List<Item> result = service.searchByName(null);

        assertTrue(result.isEmpty());
        verify(repo).searchByName("");
    }

    @Test
    void searchByName_shouldUseProvidedKeywordWhenNotNull() {
        when(repo.searchByName("bag")).thenReturn(List.of());

        List<Item> result = service.searchByName("bag");

        assertTrue(result.isEmpty());
        verify(repo).searchByName("bag");
    }

    @Test
    void listAll_shouldReturnRepositoryResult() {
        when(repo.findAll()).thenReturn(List.of(Item.builder().id(1L).build()));

        List<Item> result = service.listAll();

        assertEquals(1, result.size());
        verify(repo).findAll();
    }

    @Test
    void getById_shouldReturnItemWhenFound() {
        Item item = Item.builder().id(1L).build();
        when(repo.findById(1L)).thenReturn(Optional.of(item));

        Item result = service.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getById(99L));
    }

    @Test
    void update_shouldModifyItemAndSave() {
        Item existing = Item.builder()
                .id(1L)
                .description("old")
                .price(10.0)
                .stock(1)
                .build();
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.setDescription("new");
        req.setPrice(20.0);
        req.setStock(5);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item updated = service.update(1L, req);

        assertEquals("new", updated.getDescription());
        assertEquals(20.0, updated.getPrice());
        assertEquals(5, updated.getStock());
        verify(repo).save(existing);
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        service.delete(1L);

        verify(repo).deleteById(1L);
    }
}
