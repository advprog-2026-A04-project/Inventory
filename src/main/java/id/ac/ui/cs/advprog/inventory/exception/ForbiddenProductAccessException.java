package id.ac.ui.cs.advprog.inventory.exception;

public class ForbiddenProductAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ForbiddenProductAccessException(Long productId, String actorId) {
        super("User " + actorId + " cannot modify product " + productId);
    }
}
