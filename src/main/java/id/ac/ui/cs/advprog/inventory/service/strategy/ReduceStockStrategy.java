package id.ac.ui.cs.advprog.inventory.service.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;

@Component
public class ReduceStockStrategy implements StockMutationStrategy {

    @Override
    public void execute(Product product, int quantity) {
        int available = product.getStock();
        if (available < quantity) {
            throw new InsufficientStockException(product.getId(), quantity, available);
        }
        product.setStock(available - quantity);
    }

    @Override
    public StockMutationType getMutationType() {
        return StockMutationType.REDUCE;
    }
}
