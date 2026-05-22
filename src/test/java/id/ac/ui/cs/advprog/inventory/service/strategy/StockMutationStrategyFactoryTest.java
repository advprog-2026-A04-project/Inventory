package id.ac.ui.cs.advprog.inventory.service.strategy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import id.ac.ui.cs.advprog.inventory.model.StockMutationType;
import java.util.List;
import org.junit.jupiter.api.Test;

class StockMutationStrategyFactoryTest {

    @Test
    void getStrategyShouldReturnMatchingStrategy() {
        ReduceStockStrategy reduceStockStrategy = new ReduceStockStrategy();
        RestoreStockStrategy restoreStockStrategy = new RestoreStockStrategy();
        StockMutationStrategyFactory factory = new StockMutationStrategyFactory(
                List.of(reduceStockStrategy, restoreStockStrategy)
        );

        assertSame(reduceStockStrategy, factory.getStrategy(StockMutationType.REDUCE));
        assertSame(restoreStockStrategy, factory.getStrategy(StockMutationType.RESTORE));
    }

    @Test
    void getStrategyShouldRejectUnknownType() {
        StockMutationStrategyFactory factory = new StockMutationStrategyFactory(List.of());

        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
    }
}
