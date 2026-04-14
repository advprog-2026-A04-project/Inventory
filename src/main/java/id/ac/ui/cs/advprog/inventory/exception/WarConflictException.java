package id.ac.ui.cs.advprog.inventory.exception;

import java.util.UUID;

public class WarConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WarConflictException(UUID productId) {
        super("War mode conflict on product " + productId
                + ". You were too late, please retry.");
    }
}
