package id.ac.ui.cs.advprog.inventory.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(UUID productId) {
        super("Product with ID " + productId + " was not found.");
    }
}

