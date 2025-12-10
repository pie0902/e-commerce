package org.example.tentrilliondollars.product.dto.response;

import lombok.Getter;
import org.example.tentrilliondollars.product.entity.Product;

@Getter
public class ProductResponse {
    private Long id;
    private String name;
    private Long price;
    private String imageUrl;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
    }
}
