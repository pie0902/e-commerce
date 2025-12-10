package org.example.dollarproduct.feign;

import java.util.List;
import java.util.Map;
import org.example.dollarproduct.entity.Order;
import org.example.dollarproduct.entity.OrderDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dollar-order", url = "${loadbalancer.order}/external")
public interface FeignOrderClient {


    // 쿼리 개선 후
    @PostMapping("/productLists/orderDetails")
    List<OrderDetail> findOrderDetailsByProductId(@RequestBody List<Long> productList);

    @PostMapping("/orders")
    Map<Long, Order> getAllById(@RequestBody List<Long> orderIdList);

    @GetMapping("/orderDetails")
    Map<Long, List<OrderDetail>> findOrderDetailsByProductIds(
        @RequestParam("productIds") List<Long> productIds);

    // 쿼리 개선 전
    @GetMapping("/{productId}/orderDetails")
    List<OrderDetail> findOrderDetailsByProductId(@PathVariable Long productId);

    @GetMapping("/notify-stock-update/{productId}/{productName}")
    ResponseEntity<String> notifyStockUpdate(@PathVariable Long productId,
        @PathVariable String productName);

    //    @GetMapping("/{productId}/orderDetails")
//    List<OrderDetail> XfindOrderDetailsByProductId(@PathVariable Long productId);
    @GetMapping("/orders/{orderId}")
    Order getById(@PathVariable Long orderId);

}

