package id.ac.ui.cs.advprog.inventory.service;

import id.ac.ui.cs.advprog.inventory.dto.ProductCreateRequest;
import id.ac.ui.cs.advprog.inventory.dto.ProductUpdateRequest;
import id.ac.ui.cs.advprog.inventory.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMutationMapper {

    public Product fromCreateRequest(ProductCreateRequest request, String jastiperId) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .originLocation(request.getOriginLocation())
                .purchaseDate(request.getPurchaseDate())
                .jastiperId(jastiperId)
                .build();
    }

    public void applyUpdate(Product product, ProductUpdateRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setOriginLocation(request.getOriginLocation());
        product.setPurchaseDate(request.getPurchaseDate());
    }
}
