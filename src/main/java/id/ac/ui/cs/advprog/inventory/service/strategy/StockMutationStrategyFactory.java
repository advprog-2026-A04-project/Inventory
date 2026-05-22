package id.ac.ui.cs.advprog.inventory.service.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.inventory.model.StockMutationType;

@Component
public class StockMutationStrategyFactory {

    private final Map<StockMutationType, StockMutationStrategy> strategies;

    @Autowired
    public StockMutationStrategyFactory(List<StockMutationStrategy> strategyList) {
        strategies = new HashMap<>();
        for (StockMutationStrategy strategy : strategyList) {
            strategies.put(strategy.getMutationType(), strategy);
        }
    }

    public StockMutationStrategy getStrategy(StockMutationType type) {
        StockMutationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for mutation type: " + type);
        }
        return strategy;
    }
}
