package id.ac.ui.cs.advprog.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InventoryStockMutationRequest(
        @NotNull
        UUID productId,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotBlank
        String orderId,

        @NotBlank
        String requestId
) {
}
