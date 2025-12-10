package org.example.dollarproduct.product.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dollarproduct.product.entity.Product;
import org.example.dollarproduct.product.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external")
public class FiegnProductController {

    private final ProductService productService;

    @GetMapping("/products/{productId}")
    public Product getProducts(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    @PostMapping("/products")
    void save(@RequestBody Product product) {
        productService.save(product);
    }

    @PostMapping("/products/productIdList")
    List<Product> getProductList(@RequestBody List<Long> productIdList) {
        return productService.getAllProductsByProductIdList(productIdList);
    }

    @PostMapping("/products/updateBulk")
    void saveBulk(@RequestBody List<Product> productList) {
        productService.UpdateBulk(productList);
    }
}
