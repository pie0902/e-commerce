package org.example.tentrilliondollars.order.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.tentrilliondollars.order.entity.Order;
import org.example.tentrilliondollars.order.entity.OrderDetail;
import org.example.tentrilliondollars.order.entity.OrderState;
import org.example.tentrilliondollars.order.repository.OrderDetailRepository;
import org.example.tentrilliondollars.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    public void changeState(int requestState,Long orderId) {
        Order order = orderRepository.getReferenceById(orderId);
        if (requestState == 0) {
            order.changeState(OrderState.PREPARING);
        } else if (requestState == 1) {
            order.changeState(OrderState.SHIPPING);
        } else if (requestState == 2) {
            order.changeState(OrderState.DELIVERED);
        } else if (requestState == 3) {
            order.changeState(OrderState.NOTPAYED);
        }
        orderRepository.save(order);
    }
    public List<OrderDetail> findOrderDetailsByProductId(Long productId) {
        return orderDetailRepository.findByProductId(productId);
    }
}
