package id.ac.ui.cs.advprog.inventory.service;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import id.ac.ui.cs.advprog.inventory.exception.IdempotencyConflictException;
import id.ac.ui.cs.advprog.inventory.model.StockMutationRecord;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;
import id.ac.ui.cs.advprog.inventory.repository.StockMutationRecordRepository;

class StockMutationIdempotencyServiceTest {

    private StockMutationRecordRepository stockMutationRecordRepository;
    private JdbcTemplate jdbcTemplate;
    private StockMutationIdempotencyService stockMutationIdempotencyService;

    @BeforeEach
    void setUp() {
        stockMutationRecordRepository = Mockito.mock(StockMutationRecordRepository.class);
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        stockMutationIdempotencyService = new StockMutationIdempotencyService(stockMutationRecordRepository, jdbcTemplate);
    }

    @Test
    void registerOrDetectDuplicateShouldReturnFalseForFreshRequest() {
        when(jdbcTemplate.update(any(String.class), any(), any(), any(), any(), any(), any())).thenReturn(1);

        boolean duplicate = stockMutationIdempotencyService.registerOrDetectDuplicate(
                UUID.randomUUID(),
                2,
                "88",
                "request-88",
                StockMutationType.REDUCE
        );

        assertFalse(duplicate);
    }

    @Test
    void registerOrDetectDuplicateShouldReturnTrueForMatchingRetry() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-99";

        when(jdbcTemplate.update(any(String.class), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(stockMutationRecordRepository.findByRequestId(requestId)).thenReturn(Optional.of(
                StockMutationRecord.builder()
                        .requestId(requestId)
                        .orderId("99")
                        .productId(productId)
                        .quantity(2)
                        .mutationType(StockMutationType.RESTORE)
                        .build()
        ));

        boolean duplicate = stockMutationIdempotencyService.registerOrDetectDuplicate(
                productId,
                2,
                "99",
                requestId,
                StockMutationType.RESTORE
        );

        assertTrue(duplicate);
    }

    @Test
    void registerOrDetectDuplicateShouldRejectRequestReuseWithDifferentPayload() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-101";

        mockDuplicateRecord(requestId, productId, "101", 2, StockMutationType.REDUCE);

        assertThrows(
                IdempotencyConflictException.class,
                () -> stockMutationIdempotencyService.registerOrDetectDuplicate(
                        productId,
                        1,
                        "101",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }

    @Test
    void registerOrDetectDuplicateShouldRejectDifferentProductReuse() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-product";

        mockDuplicateRecord(requestId, UUID.randomUUID(), "102", 2, StockMutationType.REDUCE);

        assertThrows(
                IdempotencyConflictException.class,
                () -> stockMutationIdempotencyService.registerOrDetectDuplicate(
                        productId,
                        2,
                        "102",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }

    @Test
    void registerOrDetectDuplicateShouldRejectDifferentOrderReuse() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-order";

        mockDuplicateRecord(requestId, productId, "existing-order", 2, StockMutationType.REDUCE);

        assertThrows(
                IdempotencyConflictException.class,
                () -> stockMutationIdempotencyService.registerOrDetectDuplicate(
                        productId,
                        2,
                        "new-order",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }

    @Test
    void registerOrDetectDuplicateShouldRejectDifferentMutationTypeReuse() {
        UUID productId = UUID.randomUUID();
        String requestId = "request-type";

        mockDuplicateRecord(requestId, productId, "103", 2, StockMutationType.RESTORE);

        assertThrows(
                IdempotencyConflictException.class,
                () -> stockMutationIdempotencyService.registerOrDetectDuplicate(
                        productId,
                        2,
                        "103",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }

    @Test
    void registerOrDetectDuplicateShouldRethrowWhenDuplicateRecordCannotBeLoaded() {
        String requestId = "request-missing";
        DataIntegrityViolationException duplicate = new DataIntegrityViolationException("duplicate");
        when(jdbcTemplate.update(any(String.class), any(), any(), any(), any(), any(), any())).thenThrow(duplicate);
        when(stockMutationRecordRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> stockMutationIdempotencyService.registerOrDetectDuplicate(
                        UUID.randomUUID(),
                        2,
                        "104",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
        verify(stockMutationRecordRepository).findByRequestId(requestId);
    }

    private void mockDuplicateRecord(
            String requestId,
            UUID productId,
            String orderId,
            int quantity,
            StockMutationType mutationType
    ) {
        when(jdbcTemplate.update(any(String.class), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(stockMutationRecordRepository.findByRequestId(requestId)).thenReturn(Optional.of(
                StockMutationRecord.builder()
                        .requestId(requestId)
                        .orderId(orderId)
                        .productId(productId)
                        .quantity(quantity)
                        .mutationType(mutationType)
                        .build()
        ));
    }
}
