package id.ac.ui.cs.advprog.inventory.exception;

public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product " + productId
                + ". requested=" + requested + ", available=" + available);
    }
}
