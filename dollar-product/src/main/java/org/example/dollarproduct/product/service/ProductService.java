package org.example.dollarproduct.product.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dollarproduct.entity.Order;
import org.example.dollarproduct.entity.OrderDetail;
import org.example.dollarproduct.feign.FeignOrderClient;
import org.example.dollarproduct.feign.FeignUserClient;
import org.example.dollarproduct.product.dto.request.ProductRequest;
import org.example.dollarproduct.product.dto.request.ProductUpdateRequest;
import org.example.dollarproduct.product.dto.request.StockUpdateRequest;
import org.example.dollarproduct.product.dto.response.OrderDetailAdminResponse;
import org.example.dollarproduct.product.dto.response.ProductAdminResponse;
import org.example.dollarproduct.product.dto.response.ProductDetailResponse;
import org.example.dollarproduct.product.dto.response.ProductResponse;
import org.example.dollarproduct.product.entity.Product;
import org.example.dollarproduct.product.repository.ProductBulkRepository;
import org.example.dollarproduct.product.repository.ProductRepository;
import org.example.dollarproduct.s3.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.example.share.config.global.entity.user.User;
import org.example.share.config.global.entity.user.UserRoleEnum;
import org.example.share.config.global.exception.AccessDeniedException;
import org.example.share.config.global.exception.NotFoundException;
import org.example.share.config.global.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductBulkRepository productBulkRepository;
    private final FeignUserClient feignUserClient;
    private final FeignOrderClient feignOrderClient;
    private final S3Service s3Service;
    @Value("${product.bucket.name:local}")
    String bucketName;

    @Value("${storage.mode:local}")
    String storageMode;

    @Value("${storage.local.base-path:uploads}")
    String localBasePath;

    @Value("${app.base-url:http://localhost:8080/api/product}")
    String appBaseUrl;

    // 사용자 상품 관련 서비스

    public List<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByStateTrue(pageable);
        return getPageResponse(productPage);
    }

    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = getProduct(productId);
        checkProductStateIsFalse(product);
        User user = feignUserClient.findById(product.getUserId());
        return new ProductDetailResponse(product, user.getUsername());
    }

    public List<ProductResponse> getAllProductsBySearch(String search, Pageable pageable) {
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseAndStateTrue(
            search, pageable);
        return getPageResponse(productPage);
    }

    // 관리자 상품 관련 서비스

    public void createAdminProduct(ProductRequest productRequest, User user) {
        validateUserRole(user);

        Product product = Product.builder()
            .name(productRequest.getName())
            .price(productRequest.getPrice())
            .description(productRequest.getDescription())
            .stock(productRequest.getStock())
            .userId(user.getId())
            .build();

        productRepository.save(product);
    }

    public List<ProductAdminResponse> getAdminProducts(User user, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByUserIdAndStateTrue(user.getId(),
            pageable);
        return getAdminPageResponse(productPage);
    }

    @Transactional
    public void updateAdminProduct(Long productId, ProductUpdateRequest productRequest, User user) {
        Product product = getProduct(productId);

        checkProductStateIsFalse(product);

        validateProductOwner(user, product);

        product.update(productRequest);
    }

//    @Transactional
//    public void updateAdminProductStock(Long productId, StockUpdateRequest stockupdateRequest,
//        User user) {
//        Product product = getProduct(productId);
//
//        checkProductStateIsFalse(product);
//
//        validateProductOwner(user, product);
//
//        product.updateStock(stockupdateRequest);
//    }
@Transactional
public void updateAdminProductStock(Long productId, StockUpdateRequest stockupdateRequest,
    User user)
    throws NotFoundException {
    //상품 조회
    Product product = getProduct(productId);
    //상품 상태 검증 (삭제 상태인지)
    checkProductStateIsFalse(product);
    //상품 소유 권한 검증
    validateProductOwner(user, product);
    //상품 수량 업데이트
    product.updateStock(stockupdateRequest);
    String productName = getProductDetail(productId).getName();
    try {
        feignOrderClient.notifyStockUpdate(productId, productName);
    } catch (Exception e) {
        log.warn("Stock updated, but notify to order failed: productId={}, name={}, err={}",
            productId, productName, e.toString());
        // 알림 실패는 롤백 사유가 아님
    }
}
    @Transactional
    public void deleteAdminProduct(Long productId, User user) {
        Product product = getProduct(productId);

        checkProductStateIsFalse(product);

        validateProductOwner(user, product);

        product.delete();
    }

    // 검증 로직

    public void checkProductStateIsFalse(Product product) {
        if (!product.isState()) {
            throw new NotFoundException("해당 상품은 삭제되었습니다.");
        }
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(
            () -> new NotFoundException("해당 상품이 존재하지 않습니다.")
        );
    }

    private void validateUserRole(User user) {
        if (!user.getRole().equals(UserRoleEnum.SELLER)) {
            throw new UnauthorizedAccessException("인증되지 않은 유저입니다.");
        }
    }

    private void validateProductOwner(User user, Product product) {
        if (!product.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("해당 상품의 권한유저가 아닙니다.");
        }
    }

    // 기타 메소드

    private List<ProductResponse> getPageResponse(Page<Product> productPage) {
        return productPage.getContent().stream()
            .map(ProductResponse::new)
            .collect(Collectors.toList());
    }

    /**
     * todo : feignOrderClient.getById가 쿼리 N+1 문제를 발생 중 추후 해결
     */
    private List<ProductAdminResponse> getAdminPageResponse(Page<Product> productPage) {
        // product를 1페이지에 나타낼 수 있는 만큼 가져오기
        List<Product> productList = productPage.getContent();

        // 해당 페이지의 product들의 모든 ID를 productIdList에 담기
        List<Long> productIdList = new ArrayList<>();
        for (Product product : productList) {
            productIdList.add(product.getId());
        }

        // productIdList 안에 담긴 상품들 중 주문된 orderDetail을 orderDetailList에 담기
        List<OrderDetail> orderDetailList;
        try {
            orderDetailList = feignOrderClient.findOrderDetailsByProductId(productIdList);
        } catch (Exception e) {
            // 주문 서비스 연결 실패 시에도 관리자 상품 목록은 반환 (주문정보는 비움)
            orderDetailList = java.util.Collections.emptyList();
        }

        // orderDetail의 orderId를 orderIdList에 담기
        Map<Long, Long> orderIdMap = new HashMap<>();
        for (OrderDetail orderDetail : orderDetailList) {
            orderIdMap.put(orderDetail.getOrderId(), orderDetail.getOrderId());
        }
        List<Long> orderIdList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : orderIdMap.entrySet()) {
            orderIdList.add(entry.getKey());
        }

        // orderIdList 안에 담긴 orderId로 orderList에 담기
        Map<Long, Order> orderList;
        try {
            orderList = feignOrderClient.getAllById(orderIdList);
        } catch (Exception e) {
            orderList = new java.util.HashMap<>();
        }

        // orderDetailList와 orderList에서 각각 한개씩 꺼내서 OrderDetailAdminResponse에 담기
        List<OrderDetailAdminResponse> orderDetailResponseDtos = new ArrayList<>();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetailResponseDtos.add(
                new OrderDetailAdminResponse(orderDetail, orderList.get(orderDetail.getOrderId())));
        }

        Map<Long, List<OrderDetailAdminResponse>> orderDetailResponseMap = new HashMap<>();

        for (OrderDetailAdminResponse orderDetail : orderDetailResponseDtos) {
            Long productId = orderDetail.getProductId();
            if (!orderDetailResponseMap.containsKey(productId)) {
                orderDetailResponseMap.put(productId, new ArrayList<>());
            }
            orderDetailResponseMap.get(productId).add(orderDetail);
        }

        List<ProductAdminResponse> productAdminResponseList = new ArrayList<>();

        for (Product product : productList) {
            List<OrderDetailAdminResponse> orderDetailsForProduct = orderDetailResponseMap.get(
                product.getId());
            productAdminResponseList.add(
                new ProductAdminResponse(product, orderDetailsForProduct));
        }

        return productAdminResponseList;
    }

//    private List<ProductAdminResponse> getAdminPageResponse(Page<Product> productPage) {
//        return productPage.getContent().stream()
//            .map(product -> {
//                List<OrderDetail> orderDetails = feignOrderClient.XfindOrderDetailsByProductId(
//                    product.getId());
//                List<OrderDetailAdminResponse> orderDetailResponseDtos = new ArrayList<>();
//                for (OrderDetail orderDetail : orderDetails) {
//                    Order order = feignOrderClient.getById(orderDetail.getOrderId());
//                    orderDetailResponseDtos.add(new OrderDetailAdminResponse(orderDetail, order));
//                }
//                return new ProductAdminResponse(product, orderDetailResponseDtos);
//            })
//            .collect(Collectors.toList());
//    }

    public void uploadProductImage(Long productId, MultipartFile file) throws IOException {
        String imageKey = UUID.randomUUID().toString();
        String ext = "jpg";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String maybeExt = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            if (!maybeExt.isBlank()) {
                ext = maybeExt.toLowerCase();
            }
        } else if (file.getContentType() != null) {
            String ct = file.getContentType().toLowerCase();
            if (ct.contains("png")) ext = "png";
            else if (ct.contains("jpeg") || ct.contains("jpg")) ext = "jpg";
        }
        String format = "product-images/%s/%s.%s".formatted(productId, imageKey, ext);
        s3Service.putObject(bucketName, format, file);
        String url;
        if ("s3".equalsIgnoreCase(storageMode)) {
            url = "https://" + bucketName + ".s3" + ".ap-northeast-2.amazonaws.com/" + format;
        } else {
            // Serve via static resource handler: /uploads/** -> file:{localBasePath}
            url = appBaseUrl + "/uploads/" + format;
        }
        Product product = getProduct(productId);
        product.updateImageUrl(url);
        productRepository.save(product);
    }

    // 외부 feign 호출 메소드
    public void save(Product product) {
        productRepository.save(product);
    }

    public String getProductImage(Long productId) {
        try {
            return getProduct(productId).getImageUrl();
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("요청한 상품 이미지가 S3 버킷에 존재하지 않습니다. 이미지 키를 확인해주세요.");
        }
    }

    public List<Product> getAllProductsByProductIdList(List<Long> productIdList) {
        return productRepository.findAllByProductIdList(productIdList);
    }

    public void updateStockAfterOrder(Map<Long, Long> basket) {
        for (Map.Entry<Long, Long> entry : basket.entrySet()) {
            Long productId = entry.getKey();
            Long quantity = entry.getValue();
        }
    }

    public void UpdateBulk(List<Product> productList) {
        productBulkRepository.UpdateBulk(productList);
    }
}
