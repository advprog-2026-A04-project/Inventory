package id.ac.ui.cs.advprog.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReserveStockRequest(
        @NotNull
        UUID productId,
        
        @NotNull
        @Min(1)
        Integer quantity
) {
}
