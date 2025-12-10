package org.example.tentrilliondollars.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.tentrilliondollars.address.entity.Address;
import org.example.tentrilliondollars.order.entity.Order;
import org.example.tentrilliondollars.order.entity.OrderDetail;
import org.example.tentrilliondollars.order.entity.OrderState;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class OrderResponseDto {
    private Long orderId;
    private OrderState state;
    private String fullAddress;

    public OrderResponseDto(Order order, Address address){
       this.orderId = order.getId();
       this.state = order.getState();
       this.fullAddress = address.getCity()+" "+address.getVillage()+" "+address.getProvince();
    }
}
