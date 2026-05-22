package id.ac.ui.cs.advprog.inventory.service.event;

import id.ac.ui.cs.advprog.inventory.model.Product;

public class OutOfStockEvent {

    private final Product product;

    public OutOfStockEvent(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
