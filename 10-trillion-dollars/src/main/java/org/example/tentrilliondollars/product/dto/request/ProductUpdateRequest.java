package org.example.tentrilliondollars.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductUpdateRequest {
    private String name;

    private Long price;

    private String description;

    private String photo;
}
