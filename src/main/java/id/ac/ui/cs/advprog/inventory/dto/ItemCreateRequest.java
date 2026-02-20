package id.ac.ui.cs.advprog.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ItemCreateRequest {
    @NotBlank private String name;
    @NotBlank private String description;
    @NotNull @Positive private Double price;
    @NotNull @Min(0) private Integer stock;

    private String originCountry;
    private LocalDate purchaseDate;
    private LocalDate returnDate;

    @NotBlank private String jastiperId;
}