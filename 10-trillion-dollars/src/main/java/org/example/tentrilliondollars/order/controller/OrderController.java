package org.example.tentrilliondollars.order.controller;


import lombok.RequiredArgsConstructor;
import org.example.tentrilliondollars.global.security.UserDetailsImpl;
import org.example.tentrilliondollars.order.dto.CommonResponseDto;
import org.example.tentrilliondollars.order.dto.OrderRequestDto;
import org.example.tentrilliondollars.order.dto.OrderDetailResponseDto;
import org.example.tentrilliondollars.order.dto.OrderResponseDto;
import org.example.tentrilliondollars.order.entity.OrderState;
import org.example.tentrilliondollars.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("")
    public ResponseEntity<CommonResponseDto> makeOrder(@RequestBody OrderRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
        orderService.createOrder(requestDto.getBasket(),userDetails, requestDto.getAddressId());
        return ResponseEntity.status(200).body(new CommonResponseDto(200,"주문이 완료됐습니다."));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderDetailResponseDto>> getOrder(@PathVariable Long orderId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        if(orderService.checkUser(userDetails,orderId)){
            return ResponseEntity.status(200).body(orderService.getOrderDetailList(orderId));}
        throw new RuntimeException("접속 권한 없음");
    }
    @GetMapping("/userorder")
    public ResponseEntity<List<OrderResponseDto>> getUserOrder(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.status(200).body(orderService.getOrderList(userDetails));}


    @DeleteMapping("/{orderId}")
    public ResponseEntity<CommonResponseDto> cancelOrder(@PathVariable Long orderId,@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (orderService.checkUser(userDetails,orderId)) {
            orderService.deleteOrder(orderId);
            return ResponseEntity.status(200).body(new CommonResponseDto(200, "주문을 취소했습니다"));
        }
        throw new RuntimeException("접속 권한 없음");
    }


}

