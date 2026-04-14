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
import org.springframework.lang.NonNull;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByJastiperId(String jastiperId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @NonNull
    @Override
    Optional<Product> findById(@NonNull UUID id);
}
