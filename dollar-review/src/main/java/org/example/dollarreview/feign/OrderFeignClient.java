package org.example.dollarreview.feign;

import feign.FeignException;
import feign.FeignException.FeignClientException;
import java.util.List;
import org.example.dollarreview.domain.order.OrderDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dollar-order", url = "${loadbalancer.order}/external")
public interface OrderFeignClient {

    @GetMapping("/users/{userId}/products/{productId}")
    Long countByUserIdAndProductId(@PathVariable Long userId, @PathVariable Long productId);
    @GetMapping("/order/users/{userId}/products/{productId}")
    String checkOrderState(@PathVariable Long userId,@PathVariable Long productId);

    @GetMapping("/users/{userId}/products/{productId}/orders")
    List<OrderDetail> getOrderDetails(@PathVariable Long userId, @PathVariable Long productId);

    @PostMapping("/orders/orderDetail/reviewState")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, maxDelay = 5000)
        , noRetryFor = FeignClientException.class
        , recover = "recoversaveOrderDetailReviewedState"
    )
    void saveOrderDetailReviewedState(@RequestBody OrderDetail orderDetail);

    @Recover
    default void recoversaveOrderDetailReviewedState(FeignException e) {
        Logger logger = LoggerFactory.getLogger(OrderFeignClient.class);
        logger.error("All retries failed., error = {}", e.getMessage());
    }
}
