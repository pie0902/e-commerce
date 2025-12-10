package org.example.dollarorder.kakaopay.service;

import lombok.RequiredArgsConstructor;
import org.example.dollarorder.kakaopay.dto.request.CancelRequestDto;
import org.example.dollarorder.kakaopay.dto.request.PayInfoDto;
import org.example.dollarorder.kakaopay.dto.request.PayRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;

@Component
@RequiredArgsConstructor
public class MakeRequest {

    @Value("${order.public-base-url}")
    private String publicBaseUrl;

    public PayRequestDto getReadyRequest(PayInfoDto payInfoDto, Long orderId){
        Map<String, Object> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("partner_order_id", orderId.toString());
        body.put("partner_user_id", "ten");
        body.put("item_name", payInfoDto.getItemName());
        body.put("quantity", 1);
        body.put("total_amount", payInfoDto.getPrice());
        body.put("tax_free_amount", 0);
        body.put("approval_url", publicBaseUrl + "/payment/success/" + orderId);
        body.put("cancel_url", publicBaseUrl + "/payment/cancel/" + orderId);
        body.put("fail_url", publicBaseUrl + "/payment/fail");
        return new PayRequestDto("https://open-api.kakaopay.com/online/v1/payment/ready", body, orderId);
    }

    public PayRequestDto getApproveRequest(String tid, String pgToken, Long orderId){
        Map<String, Object> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", tid);
        body.put("partner_order_id", orderId.toString());
        body.put("partner_user_id", "ten");
        body.put("pg_token", pgToken);
        return new PayRequestDto("https://open-api.kakaopay.com/online/v1/payment/approve", body, orderId);
    }

    public CancelRequestDto getCancelRequest(String tid, Long price){
        Map<String, Object> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", tid);
        body.put("cancel_amount", price);
        body.put("cancel_tax_free_amount", 0);
        body.put("cancel_vat_amount", 0);
        return new CancelRequestDto("https://open-api.kakaopay.com/online/v1/payment/cancel", body);
    }
}
