package id.ac.ui.cs.advprog.inventory.repository;

import id.ac.ui.cs.advprog.inventory.model.StockMutationRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMutationRecordRepository extends JpaRepository<StockMutationRecord, Long> {

    Optional<StockMutationRecord> findByRequestId(String requestId);
}
