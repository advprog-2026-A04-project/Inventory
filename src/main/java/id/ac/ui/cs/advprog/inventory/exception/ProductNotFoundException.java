package id.ac.ui.cs.advprog.inventory.exception;

public class ProductNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);
    }
}
