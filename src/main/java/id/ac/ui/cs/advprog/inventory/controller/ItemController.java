package id.ac.ui.cs.advprog.inventory.controller;

import id.ac.ui.cs.advprog.inventory.dto.ItemCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ItemUpdateRequest;
import id.ac.ui.cs.advprog.inventory.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    // List katalog milik jastiper
    @GetMapping
    public String listByJastiper(@RequestParam String jastiperId, Model model) {
        model.addAttribute("jastiperId", jastiperId);
        model.addAttribute("items", service.listByJastiper(jastiperId));
        return "Items";
    }

    // Form create
    @GetMapping("/new")
    public String newForm(@RequestParam String jastiperId, Model model) {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setJastiperId(jastiperId);
        model.addAttribute("req", req);
        return "ItemForm";
    }

    // Create
    @PostMapping
    public String create(@Valid @ModelAttribute("req") ItemCreateRequest req,
                         BindingResult binding) {
        if (binding.hasErrors()) {
            return "ItemForm";
        }
        service.create(req);
        return "redirect:/items?jastiperId=" + req.getJastiperId();
    }

    // Form edit
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam String jastiperId,
                           Model model) {
        var item = service.getById(id);

        ItemUpdateRequest req = new ItemUpdateRequest();
        req.setDescription(item.getDescription());
        req.setPrice(item.getPrice());
        req.setStock(item.getStock());

        model.addAttribute("id", id);
        model.addAttribute("jastiperId", jastiperId);
        model.addAttribute("req", req);
        return "ItemEdit";
    }

    // Update
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String jastiperId,
                         @Valid @ModelAttribute("req") ItemUpdateRequest req,
                         BindingResult binding) {
        if (binding.hasErrors()) {
            return "ItemEdit";
        }
        service.update(id, req);
        return "redirect:/items?jastiperId=" + jastiperId;
    }

    // Delete
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam String jastiperId) {
        service.delete(id);
        return "redirect:/items?jastiperId=" + jastiperId;
    }

    // Search by product name
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("items", service.searchByName(keyword));
        return "Search";
    }

    // Reserve stock (dummy order)
    @PostMapping("/{id}/reserve")
    public String reserve(@PathVariable Long id,
                          @RequestParam int qty,
                          @RequestParam String jastiperId,
                          Model model) {
        try {
            service.reserve(id, qty);
            return "redirect:/items?jastiperId=" + jastiperId;
        } catch (Exception e) {
            model.addAttribute("jastiperId", jastiperId);
            model.addAttribute("items", service.listByJastiper(jastiperId));
            model.addAttribute("error", e.getMessage());
            return "Items";
        }
    }

    // Dummy admin list all (guard: role=ADMIN)
    @GetMapping("/all")
    public String listAll(@RequestParam(defaultValue = "USER") String role, Model model) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            model.addAttribute("message", "Forbidden: ADMIN only");
            return "Error";
        }
        model.addAttribute("items", service.listAll());
        return "ItemsAll";
    }
}