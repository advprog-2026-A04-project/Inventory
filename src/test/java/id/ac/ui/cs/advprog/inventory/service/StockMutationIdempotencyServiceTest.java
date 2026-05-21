package id.ac.ui.cs.advprog.inventory.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.inventory.exception.IdempotencyConflictException;
import id.ac.ui.cs.advprog.inventory.model.StockMutationRecord;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;
import id.ac.ui.cs.advprog.inventory.repository.StockMutationRecordRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

class StockMutationIdempotencyServiceTest {

    private StockMutationRecordRepository stockMutationRecordRepository;
    private StockMutationIdempotencyService stockMutationIdempotencyService;

    @BeforeEach
    void setUp() {
        stockMutationRecordRepository = Mockito.mock(StockMutationRecordRepository.class);
        stockMutationIdempotencyService = new StockMutationIdempotencyService(stockMutationRecordRepository);
    }

    @Test
    void isDuplicateShouldReturnFalseForFreshRequest() {
        String requestId = "request-88";
        when(stockMutationRecordRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

        boolean duplicate = stockMutationIdempotencyService.isDuplicate(
                UUID.randomUUID(),
                2,
                "88",
                requestId,
                StockMutationType.REDUCE
        );

        assertFalse(duplicate);
    }

    @Test
    void isDuplicateShouldReturnTrueForMatchingRetry() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-99";

        when(stockMutationRecordRepository.findByRequestId(requestId)).thenReturn(Optional.of(
                StockMutationRecord.builder()
                        .requestId(requestId)
                        .orderId("99")
                        .productId(productId)
                        .quantity(2)
                        .mutationType(StockMutationType.RESTORE)
                        .build()
        ));

        boolean duplicate = stockMutationIdempotencyService.isDuplicate(
                productId,
                2,
                "99",
                requestId,
                StockMutationType.RESTORE
        );

        assertTrue(duplicate);
    }

    @Test
    void isDuplicateShouldRejectRequestReuseWithDifferentPayload() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-101";

        when(stockMutationRecordRepository.findByRequestId(requestId)).thenReturn(Optional.of(
                StockMutationRecord.builder()
                        .requestId(requestId)
                        .orderId("101")
                        .productId(productId)
                        .quantity(2)
                        .mutationType(StockMutationType.REDUCE)
                        .build()
        ));

        assertThrows(
                IdempotencyConflictException.class,
                () -> stockMutationIdempotencyService.isDuplicate(
                        productId,
                        1,
                        "101",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }

    @Test
    void recordAppliedShouldPersistMutationRecord() {
        UUID productId = UUID.randomUUID();
        when(stockMutationRecordRepository.saveAndFlush(any(StockMutationRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMutationIdempotencyService.recordApplied(
                productId,
                2,
                "102",
                "request-102",
                StockMutationType.REDUCE
        );

        Mockito.verify(stockMutationRecordRepository).saveAndFlush(any(StockMutationRecord.class));
    }

    @Test
    void recordAppliedShouldRejectConcurrentRequestReuse() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-103";

        when(stockMutationRecordRepository.saveAndFlush(any(StockMutationRecord.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(
                IdempotencyConflictException.class,
                () -> stockMutationIdempotencyService.recordApplied(
                        productId,
                        2,
                        "103",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }
}
