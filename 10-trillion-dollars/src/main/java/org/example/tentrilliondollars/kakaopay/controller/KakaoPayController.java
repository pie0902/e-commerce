
package org.example.tentrilliondollars.kakaopay.controller;

import lombok.RequiredArgsConstructor;
import org.example.tentrilliondollars.kakaopay.dto.response.CancelResDto;
import org.example.tentrilliondollars.kakaopay.service.KakaoPayService;
import org.example.tentrilliondollars.kakaopay.dto.response.PayApproveResDto;
import org.example.tentrilliondollars.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;

    @GetMapping("/ready/{orderId}")
    public ResponseEntity<?> getRedirectUrl(@PathVariable Long orderId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
            .body(kakaoPayService.getRedirectUrl(orderId));
    }

    @GetMapping("/success/{orderId}")
    public ResponseEntity<?> afterGetRedirectUrl(@PathVariable Long orderId,
        @RequestParam("pg_token") String pgToken) throws Exception {
        PayApproveResDto kakaoApprove = kakaoPayService.getApprove(pgToken,orderId);
        return ResponseEntity.status(HttpStatus.OK)
            .body("결제가 완료되었습니다 해당 페이지를 종료해주십시요.");
    }

    @GetMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancel(@PathVariable Long orderId) throws Exception {
        CancelResDto cancelResDto = kakaoPayService.kakaoCancel(orderId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(cancelResDto);
    }

}

