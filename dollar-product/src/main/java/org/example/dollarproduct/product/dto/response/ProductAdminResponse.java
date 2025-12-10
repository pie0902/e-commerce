package org.example.dollarproduct.product.dto.response;


import java.util.List;
import lombok.Getter;
import org.example.dollarproduct.product.entity.Product;

@Getter
public class ProductAdminResponse {
    private Long id;
    private String name;
    private Long price;
    private Long stock;
    private List<OrderDetailAdminResponse> orderDetails;

    public ProductAdminResponse(Product product,List<OrderDetailAdminResponse> orderDetails) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.orderDetails = orderDetails;
    }
}
