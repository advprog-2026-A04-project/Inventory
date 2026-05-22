package id.ac.ui.cs.advprog.inventory.service.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;

@Component
public class RestoreStockStrategy implements StockMutationStrategy {

    @Override
    public void execute(Product product, int quantity) {
        product.setStock(product.getStock() + quantity);
    }

    @Override
    public StockMutationType getMutationType() {
        return StockMutationType.RESTORE;
    }
}
