package id.ac.ui.cs.advprog.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemUpdateRequest {
    @NotBlank private String description;
    @NotNull @Positive private Double price;
    @NotNull @Min(0) private Integer stock;
}