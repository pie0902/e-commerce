package org.example.dollarorder.kakaopay.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;
@Getter
@AllArgsConstructor
public class CancelRequestDto {
    private String url;
    private Map<String, Object> body;
}
