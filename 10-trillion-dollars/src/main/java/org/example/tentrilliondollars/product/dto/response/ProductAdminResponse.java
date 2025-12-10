package org.example.tentrilliondollars.product.dto.response;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.example.tentrilliondollars.order.dto.OrderDetailAdminResponse;
import org.example.tentrilliondollars.order.dto.OrderDetailResponseDto;
import org.example.tentrilliondollars.order.dto.OrderResponseDto;
import org.example.tentrilliondollars.order.entity.OrderDetail;
import org.example.tentrilliondollars.product.entity.Product;


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
