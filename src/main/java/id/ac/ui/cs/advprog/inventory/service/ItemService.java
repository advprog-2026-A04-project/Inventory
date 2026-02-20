package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ItemCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ItemUpdateRequest;
import id.ac.ui.cs.advprog.inventory.model.Item;
import id.ac.ui.cs.advprog.inventory.repository.ItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public Item create(ItemCreateRequest req) {
        Item item = Item.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stock(req.getStock())
                .originCountry(req.getOriginCountry())
                .purchaseDate(req.getPurchaseDate())
                .returnDate(req.getReturnDate())
                .jastiperId(req.getJastiperId())
                .build();
        return repo.save(item);
    }

    public List<Item> listByJastiper(String jastiperId) {
        return repo.findByJastiperId(jastiperId);
    }

    public List<Item> searchByName(String keyword) {
        return repo.searchByName(keyword == null ? "" : keyword);
    }

    public List<Item> listAll() {
        return repo.findAll();
    }

    public Item getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
    }

    @Transactional
    public Item update(Long id, ItemUpdateRequest req) {
        Item item = getById(id);
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setStock(req.getStock());
        return repo.save(item);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public void reserve(Long itemId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        int updated = repo.decreaseStockIfAvailable(itemId, qty);
        if (updated == 0) throw new IllegalStateException("Insufficient stock");
    }
}