package id.ac.ui.cs.advprog.inventory.exception;

public class WarConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WarConflictException(Long productId) {
        super("War mode conflict on product " + productId
                + ". You were too late, please retry.");
    }
}
