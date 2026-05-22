package id.ac.ui.cs.advprog.inventory.repository;

import id.ac.ui.cs.advprog.inventory.model.Product;
import java.util.List;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.jastiperId = :jastiperId AND (p.deleted IS NULL OR p.deleted = false)")
    List<Product> findActiveByJastiperId(@Param("jastiperId") String jastiperId);

    @Query("""
            SELECT p FROM Product p
            WHERE (p.deleted IS NULL OR p.deleted = false)
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Product> searchByName(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND (p.deleted IS NULL OR p.deleted = false)")
    Optional<Product> findActiveById(@Param("id") UUID id);

    @Query("SELECT p FROM Product p WHERE p.deleted IS NULL OR p.deleted = false")
    List<Product> findAllActive();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND (p.deleted IS NULL OR p.deleted = false)")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);
}
