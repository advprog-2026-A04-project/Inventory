package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.exception.ForbiddenProductAccessException;
import id.ac.ui.cs.advprog.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.inventory.exception.WarConflictException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;
import id.ac.ui.cs.advprog.inventory.repository.ProductRepository;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final StockMutationIdempotencyService stockMutationIdempotencyService;
    private final ProductMutationMapper productMutationMapper;

    public ProductService(
            ProductRepository productRepository,
            StockMutationIdempotencyService stockMutationIdempotencyService,
            ProductMutationMapper productMutationMapper
    ) {
        this.productRepository = productRepository;
        this.stockMutationIdempotencyService = stockMutationIdempotencyService;
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
        validateQuantity(quantity);
        return applyStockReduction(productId, quantity);
    }

    public Product getById(UUID productId) {
        return findProductOrThrow(productId);
    }

    @Transactional
    public Product restoreStock(UUID productId, int quantity) {
        validateQuantity(quantity);
        return applyStockRestoration(productId, quantity);
    }

    @Transactional
    public Product reduceStock(UUID productId, int quantity, String orderId, String requestId) {
        validateQuantity(quantity);
        validateMutationMetadata(orderId, requestId);
        return applyIdempotentMutation(productId, quantity, orderId, requestId, StockMutationType.REDUCE, product -> {
            int available = product.getStock();
            if (available < quantity) {
                throw new InsufficientStockException(productId, quantity, available);
            }
            product.setStock(available - quantity);
        });
    }

    @Transactional
    public Product restoreStock(UUID productId, int quantity, String orderId, String requestId) {
        validateQuantity(quantity);
        validateMutationMetadata(orderId, requestId);
        return applyIdempotentMutation(productId, quantity, orderId, requestId, StockMutationType.RESTORE,
                product -> product.setStock(product.getStock() + quantity));
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Product findProductForUpdateOrThrow(UUID productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Product applyStockReduction(UUID productId, int quantity) {
        Product product = findProductForUpdateOrThrow(productId);
        int available = product.getStock();
        if (available < quantity) {
            throw new InsufficientStockException(productId, quantity, available);
        }
        product.setStock(available - quantity);
        return saveProduct(productId, product);
    }

    private Product applyStockRestoration(UUID productId, int quantity) {
        Product product = findProductForUpdateOrThrow(productId);
        product.setStock(product.getStock() + quantity);
        return saveProduct(productId, product);
    }

    private Product applyIdempotentMutation(
            UUID productId,
            int quantity,
            String orderId,
            String requestId,
            StockMutationType mutationType,
            Consumer<Product> mutation
    ) {
        if (isDuplicateMutation(productId, quantity, orderId, requestId, mutationType)) {
            log.info(
                    "Skipping duplicate inventory mutation requestId={} orderId={} productId={} type={}",
                    requestId,
                    orderId,
                    productId,
                    mutationType
            );
            return findProductOrThrow(productId);
        }

        Product product = findProductForUpdateOrThrow(productId);
        mutation.accept(product);
        Product saved = saveProduct(productId, product);
        log.info(
                "Applied inventory mutation requestId={} orderId={} productId={} type={} quantity={} resultingStock={}",
                requestId,
                orderId,
                productId,
                mutationType,
                quantity,
                saved.getStock()
        );
        return saved;
    }

    private boolean isDuplicateMutation(
            UUID productId,
            int quantity,
            String orderId,
            String requestId,
            StockMutationType mutationType
    ) {
        return stockMutationIdempotencyService.registerOrDetectDuplicate(
                productId,
                quantity,
                orderId,
                requestId,
                mutationType
        );
    }

    private Product saveProduct(UUID productId, Product product) {
        try {
            return productRepository.saveAndFlush(product);
        } catch (OptimisticLockingFailureException ex) {
            throw new WarConflictException(productId);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }

    private void validateMutationMetadata(String orderId, String requestId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must be provided");
        }
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must be provided");
        }
    }
}
