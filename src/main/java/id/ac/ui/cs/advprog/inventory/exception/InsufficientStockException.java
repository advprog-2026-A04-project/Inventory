package id.ac.ui.cs.advprog.inventory.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientStockException(UUID productId, int requested, int available) {
        super("Insufficient stock for product " + productId
                + ". requested=" + requested + ", available=" + available);
    }
}
