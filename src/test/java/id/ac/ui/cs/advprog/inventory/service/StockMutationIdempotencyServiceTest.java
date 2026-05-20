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
import org.springframework.jdbc.core.JdbcTemplate;

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

        when(jdbcTemplate.update(any(String.class), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
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
                () -> stockMutationIdempotencyService.registerOrDetectDuplicate(
                        productId,
                        1,
                        "101",
                        requestId,
                        StockMutationType.REDUCE
                )
        );
    }
}
