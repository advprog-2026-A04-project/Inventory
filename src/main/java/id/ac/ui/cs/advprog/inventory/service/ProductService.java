package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.exception.ForbiddenProductAccessException;
import id.ac.ui.cs.advprog.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.inventory.exception.WarConflictException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.repository.ProductRepository;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMutationMapper productMutationMapper;

    public ProductService(ProductRepository productRepository, ProductMutationMapper productMutationMapper) {
        this.productRepository = productRepository;
        this.productMutationMapper = productMutationMapper;
    }

    public Product create(ProductCreateRequest request, String jastiperId) {
        return productRepository.save(productMutationMapper.fromCreateRequest(request, jastiperId));
    }

    public List<Product> listOwnedBy(String jastiperId) {
        return productRepository.findAllByJastiperId(jastiperId);
    }

    public List<Product> searchByProductName(String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        return productRepository.searchByName(safeKeyword);
    }

    public List<Product> listByJastiper(String jastiperId) {
        return productRepository.findAllByJastiperId(jastiperId);
    }

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    @Transactional
    public Product updateOwnedProduct(UUID productId, ProductUpdateRequest request, String actorId) {
        Product product = findProductOrThrow(productId);
        if (!product.getJastiperId().equals(actorId)) {
            throw new ForbiddenProductAccessException(productId, actorId);
        }
        productMutationMapper.applyUpdate(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteOwnedProduct(UUID productId, String actorId) {
        Product product = findProductOrThrow(productId);
        if (!product.getJastiperId().equals(actorId)) {
            throw new ForbiddenProductAccessException(productId, actorId);
        }
        productRepository.delete(product);
    }

    @Transactional
    public Product adminUpdateProduct(UUID productId, ProductUpdateRequest request) {
        Product product = findProductOrThrow(productId);
        productMutationMapper.applyUpdate(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public void adminDeleteProduct(UUID productId) {
        Product product = findProductOrThrow(productId);
        productRepository.delete(product);
    }

    @Transactional
    public Product reserveStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }

        Product product = findProductForUpdateOrThrow(productId);
        int available = product.getStock();
        if (available < quantity) {
            throw new InsufficientStockException(productId, quantity, available);
        }

        product.setStock(available - quantity);
        try {
            return productRepository.saveAndFlush(product);
        } catch (OptimisticLockingFailureException ex) {
            throw new WarConflictException(productId);
        }
    }

    public Product getById(UUID productId) {
        return findProductOrThrow(productId);
    }

    @Transactional
    public Product restoreStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }

        Product product = findProductForUpdateOrThrow(productId);
        product.setStock(product.getStock() + quantity);
        return productRepository.saveAndFlush(product);
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Product findProductForUpdateOrThrow(UUID productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
