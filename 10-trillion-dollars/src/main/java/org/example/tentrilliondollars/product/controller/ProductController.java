package org.example.tentrilliondollars.product.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.tentrilliondollars.product.dto.response.ProductDetailResponse;
import org.example.tentrilliondollars.product.dto.response.ProductResponse;
import org.example.tentrilliondollars.product.service.ProductCacheService;
import org.example.tentrilliondollars.product.service.ProductService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductCacheService productCacheService;


    @GetMapping
    public List<ProductResponse> getAllProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        //return productService.getAllProducts(pageable);
        return productCacheService.getAllProducts(pageable);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
        @PathVariable Long productId
    ) throws NotFoundException {
        return ResponseEntity.status(200)
            .body(productService.getProductDetail(productId));
    }

    @GetMapping("/search")
    public List<ProductResponse> getSearchProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getAllProductsBySearch(search, pageable);
    }

    @GetMapping("{productId}/image")
    public String getProductImage(@PathVariable Long productId) throws IOException {
      return productService.getProductImage(productId);
    }

}
