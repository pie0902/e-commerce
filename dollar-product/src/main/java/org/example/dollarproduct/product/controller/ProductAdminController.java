package org.example.dollarproduct.product.controller;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dollarproduct.product.dto.request.ProductRequest;
import org.example.dollarproduct.product.dto.request.ProductUpdateRequest;
import org.example.dollarproduct.product.dto.request.StockUpdateRequest;
import org.example.dollarproduct.product.dto.response.ProductAdminResponse;
import org.example.dollarproduct.product.service.ProductService;
import org.example.share.config.global.security.UserDetailsImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/products")
//@Secured("ROLE_SELLER")
@Secured({"ROLE_SELLER"})
public class ProductAdminController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<String> createAdminProduct(
        @RequestBody ProductRequest productRequest,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        productService.createAdminProduct(productRequest, userDetails.getUser());

        return ResponseEntity.status(201)
            .body("Product created successfully");
    }

    @GetMapping
    public ResponseEntity<?> getAdminProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        Pageable pageable = PageRequest.of(page, size);
        //본래의 get method를 관리자 전용으로
        List<ProductAdminResponse> list = productService.getAdminProducts(userDetails.getUser(), pageable);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<String> updateAdminProduct(
        @PathVariable Long productId,
        @RequestBody ProductUpdateRequest productRequest,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        productService.updateAdminProduct(productId, productRequest, userDetails.getUser());

        return ResponseEntity.status(200)
            .body("Product update successfully");
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<String> updateAdminProductStock(
        @PathVariable Long productId,
        @RequestBody StockUpdateRequest stockupdateRequest,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        productService.updateAdminProductStock(productId, stockupdateRequest,
            userDetails.getUser());

        return ResponseEntity.status(200)
            .body("Product stock update successfully");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteAdminProduct(
        @PathVariable Long productId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        productService.deleteAdminProduct(productId, userDetails.getUser());

        return ResponseEntity.status(200)
            .body("Product delete successfully");
    }


    @PostMapping("{productId}/image")
    public ResponseEntity<String> uploadProductImage(@PathVariable Long productId,
        @RequestParam("file") MultipartFile file) throws IOException {
        productService.uploadProductImage(productId, file);

        return ResponseEntity.status(200)
            .body("Product Image update successfully");
    }

}
