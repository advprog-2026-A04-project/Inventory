package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.exception.IdempotencyConflictException;
import id.ac.ui.cs.advprog.inventory.model.StockMutationRecord;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;
import id.ac.ui.cs.advprog.inventory.repository.StockMutationRecordRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMutationIdempotencyService {

    private final StockMutationRecordRepository stockMutationRecordRepository;

    public StockMutationIdempotencyService(StockMutationRecordRepository stockMutationRecordRepository) {
        this.stockMutationRecordRepository = stockMutationRecordRepository;
    }

    @Transactional(readOnly = true)
    public boolean isDuplicate(
            UUID productId,
            int quantity,
            String orderId,
            String requestId,
            StockMutationType mutationType
    ) {
        return stockMutationRecordRepository.findByRequestId(requestId)
                .map(existing -> {
                    assertSameMutation(existing, productId, quantity, orderId, mutationType);
                    return true;
                })
                .orElse(false);
    }

    public void recordApplied(
            UUID productId,
            int quantity,
            String orderId,
            String requestId,
            StockMutationType mutationType
    ) {
        try {
            stockMutationRecordRepository.saveAndFlush(StockMutationRecord.builder()
                    .requestId(requestId)
                    .orderId(orderId)
                    .productId(productId)
                    .quantity(quantity)
                    .mutationType(mutationType)
                    .appliedAt(Instant.now())
                    .build());
        } catch (DataIntegrityViolationException exception) {
            throw new IdempotencyConflictException(requestId);
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
