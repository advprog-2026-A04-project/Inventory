package id.ac.ui.cs.advprog.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "items",
        indexes = {
                @Index(name = "idx_items_name", columnList = "name"),
                @Index(name = "idx_items_jastiper", columnList = "jastiperId")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, length=2000)
    private String description;

    @Column(nullable=false)
    private Double price;

    @Column(nullable=false)
    private Integer stock;

    private String originCountry;
    private LocalDate purchaseDate;
    private LocalDate returnDate;

    @Column(nullable=false)
    private String jastiperId;
}