package org.example.dollarproduct.product.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class ProductRequest {

    private String name;

    private Long price;

    private String description;

    private Long stock;

}
