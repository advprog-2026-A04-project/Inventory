package id.ac.ui.cs.advprog.inventory.exception;

import java.util.UUID;

public class ForbiddenProductAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ForbiddenProductAccessException(UUID productId, String actorId) {
        super("User " + actorId + " is not authorized to access product " + productId);
    }
}

