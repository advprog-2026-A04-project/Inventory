package id.ac.ui.cs.advprog.inventory.service.strategy;

import id.ac.ui.cs.advprog.inventory.model.Product;
import id.ac.ui.cs.advprog.inventory.model.StockMutationType;

public interface StockMutationStrategy {
    void execute(Product product, int quantity);
    StockMutationType getMutationType();
}
