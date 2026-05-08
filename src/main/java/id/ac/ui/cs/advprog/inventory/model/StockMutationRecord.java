package id.ac.ui.cs.advprog.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "stock_mutation_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_mutation_request_id", columnNames = "requestId")
        },
        indexes = {
                @Index(name = "idx_stock_mutation_order", columnList = "orderId"),
                @Index(name = "idx_stock_mutation_product", columnList = "productId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMutationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String requestId;

    @Column(nullable = false, length = 60)
    private String orderId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockMutationType mutationType;

    @Column(nullable = false, updatable = false)
    private Instant appliedAt;
}
