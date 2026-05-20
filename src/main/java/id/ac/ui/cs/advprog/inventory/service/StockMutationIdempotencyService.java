package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.exception.IdempotencyConflictException;
import id.ac.ui.cs.advprog.inventory.model.StockMutationRecord;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;
import id.ac.ui.cs.advprog.inventory.repository.StockMutationRecordRepository;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMutationIdempotencyService {

    private final StockMutationRecordRepository stockMutationRecordRepository;
    private final JdbcTemplate jdbcTemplate;

    public StockMutationIdempotencyService(
            StockMutationRecordRepository stockMutationRecordRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.stockMutationRecordRepository = stockMutationRecordRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registerOrDetectDuplicate(
            UUID productId,
            int quantity,
            String orderId,
            String requestId,
            StockMutationType mutationType
    ) {
        try {
            jdbcTemplate.update(
                    """
                    insert into stock_mutation_records
                    (request_id, order_id, product_id, quantity, mutation_type, applied_at)
                    values (?, ?, ?, ?, ?, ?)
                    """,
                    requestId,
                    orderId,
                    productId,
                    quantity,
                    mutationType.name(),
                    Timestamp.from(Instant.now())
            );
            return false;
        } catch (DataIntegrityViolationException exception) {
            StockMutationRecord existing = stockMutationRecordRepository.findByRequestId(requestId)
                    .orElseThrow(() -> exception);
            assertSameMutation(existing, productId, quantity, orderId, mutationType);
            return true;
        }
    }

    private void assertSameMutation(
            StockMutationRecord existing,
            UUID productId,
            int quantity,
            String orderId,
            StockMutationType mutationType
    ) {
        boolean sameMutation = Objects.equals(existing.getProductId(), productId)
                && Objects.equals(existing.getQuantity(), quantity)
                && Objects.equals(existing.getOrderId(), orderId)
                && existing.getMutationType() == mutationType;
        if (!sameMutation) {
            throw new IdempotencyConflictException(existing.getRequestId());
        }
    }
}
