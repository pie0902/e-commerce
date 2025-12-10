package org.example.dollarproduct.feign;

import feign.FeignException.FeignClientException;
import org.example.share.config.global.entity.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "dollar-user", url = "${loadbalancer.user}/external")
public interface FeignUserClient {

    @GetMapping("/users/{userId}")
    User findById(@PathVariable Long userId);
}
