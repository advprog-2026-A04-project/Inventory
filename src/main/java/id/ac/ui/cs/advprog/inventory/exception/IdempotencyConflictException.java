package id.ac.ui.cs.advprog.inventory.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String requestId) {
        super("Request id %s was already used for a different inventory mutation.".formatted(requestId));
    }
}
