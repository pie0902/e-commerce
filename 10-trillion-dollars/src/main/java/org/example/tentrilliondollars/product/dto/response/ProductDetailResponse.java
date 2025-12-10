package org.example.tentrilliondollars.product.dto.response;

import lombok.Getter;
import org.example.tentrilliondollars.product.entity.Product;

@Getter
public class ProductDetailResponse {

    private Long id;
    private String name;
    private Long price;
    private String description;
    private Long stock;
    private String adminname;
    private String imageUrl;

    public ProductDetailResponse(Product product, String username) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.stock = product.getStock();
        this.imageUrl = product.getImageUrl();
        this.adminname = username;
    }
}
