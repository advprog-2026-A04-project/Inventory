package id.ac.ui.cs.advprog.inventory.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ReserveStockRequest;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.service.ProductService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final String ROLE_JASTIPER = "hasRole('JASTIPER')";
    private static final String ROLE_ADMIN = "hasRole('ADMIN')";
    private static final String ROLE_BUYER_OR_HIGHER = "hasAnyRole('TITIPER','JASTIPER','ADMIN')";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize(ROLE_JASTIPER)
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductCreateRequest request,
                                                 Authentication authentication) {
        Product created = productService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize(ROLE_JASTIPER)
    @PutMapping("/{productId}")
    public Product updateOwnProduct(@PathVariable UUID productId,
                                    @Valid @RequestBody ProductUpdateRequest request,
                                    Authentication authentication) {
        return productService.updateOwnedProduct(productId, request, authentication.getName());
    }

    @PreAuthorize(ROLE_JASTIPER)
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnProduct(@PathVariable UUID productId, Authentication authentication) {
        productService.deleteOwnedProduct(productId, authentication.getName());
    }

    @PreAuthorize(ROLE_JASTIPER)
    @GetMapping("/me")
    public List<Product> listMyProducts(Authentication authentication) {
        return productService.listOwnedBy(authentication.getName());
    }

    @PreAuthorize(ROLE_BUYER_OR_HIGHER)
    @GetMapping("/search")
    public List<Product> searchByProduct(@RequestParam(required = false) String keyword) {
        return productService.searchByProductName(keyword);
    }

    @PreAuthorize(ROLE_BUYER_OR_HIGHER)
    @GetMapping("/jastipers/{jastiperId}")
    public List<Product> searchByJastiper(@PathVariable String jastiperId) {
        return productService.listByJastiper(jastiperId);
    }

    @PreAuthorize(ROLE_BUYER_OR_HIGHER)
    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable UUID productId) {
        return productService.getById(productId);
    }

    @PreAuthorize(ROLE_ADMIN)
    @GetMapping
    public List<Product> monitorAllProducts() {
        return productService.listAll();
    }

    @PreAuthorize(ROLE_ADMIN)
    @PutMapping("/admin/{productId}")
    public Product adminUpdateProduct(@PathVariable UUID productId,
                                      @Valid @RequestBody ProductUpdateRequest request) {
        return productService.adminUpdateProduct(productId, request);
    }

    @PreAuthorize(ROLE_ADMIN)
    @DeleteMapping("/admin/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adminDeleteProduct(@PathVariable UUID productId) {
        productService.adminDeleteProduct(productId);
    }

    @PreAuthorize(ROLE_BUYER_OR_HIGHER)
    @PostMapping("/{productId}/reserve")
    public Product reserveStock(@PathVariable UUID productId, @Valid @RequestBody ReserveStockRequest request) {
        return productService.reserveStock(productId, request.quantity());
    }

    // Prompt 3: Inter-microservice communication
    @PreAuthorize(ROLE_BUYER_OR_HIGHER)
    @GetMapping("/inventory/{productId}")
    public Product getInventoryDetail(@PathVariable UUID productId) {
        return productService.getById(productId);
    }

    @PreAuthorize(ROLE_BUYER_OR_HIGHER)
    @PatchMapping("/inventory/reduce-stock")
    public Product reduceStock(@Valid @RequestBody ReserveStockRequest request) {
        return productService.reserveStock(request.productId(), request.quantity());
    }
}
