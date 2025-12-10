package org.example.dollarorder.order.service;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dollarorder.domain.address.entity.Address;
import org.example.dollarorder.domain.product.entity.Product;
import org.example.dollarorder.feign.AddressFeignClient;
import org.example.dollarorder.feign.ProductFeignClient;
import org.example.dollarorder.order.dto.OrderDetailResponseDto;
import org.example.dollarorder.order.dto.OrderResponseDto;
import org.example.dollarorder.order.entity.Order;
import org.example.dollarorder.order.entity.OrderDetail;
import org.example.dollarorder.order.entity.OrderState;
import org.example.dollarorder.order.repository.OrderDetailBulkRepository;
import org.example.dollarorder.order.repository.OrderDetailRepository;
import org.example.dollarorder.order.repository.OrderRepository;
import org.example.dollarorder.order.service.EmailService.EmailType;
import org.example.share.config.global.entity.user.User;
import org.example.share.config.global.exception.BadRequestException;
import org.example.share.config.global.exception.NotFoundException;
import org.example.share.config.global.security.UserDetailsImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailBulkRepository orderDetailBulkRepository;
    private final AddressFeignClient addressFeignClient;
    private final ProductFeignClient productFeignClient;
    private final EntityManager entityManager;
    private final RedissonClient redissonClient;
    private final EmailService emailService;



    @Transactional
    public void createOrder(
        Map<Long, Long> basket, //상품<상품id,수량>
        UserDetailsImpl userDetails,
        Long addressId
    ) {
        //동시성
        String lockKey = "order_lock";
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //대기시간 5초, 락을 유지하는 시간 10초
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("락 획득에 실패했습니다.");

            }
            System.out.println(userDetails.getUser().getId()+"번 유저가 주문을 합니다.");
            // 주문 객체 생성
            Order order = new Order(userDetails.getUser().getId(), OrderState.NOTPAYED, addressId);
            // 상품 수량 검증 코드
            checkBasket(basket, order);
            // 주문 객체 저장
            Order savedOrder = orderRepository.save(order);

            updateStockAndCreateOrderDetail(basket, savedOrder);

        } catch (InterruptedException e) {
            throw new RuntimeException("락 획득 중 오류가 발생했습니다.", e);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }



    //주문 상세 생성 및 재고 업데이트
    @Transactional
    public void updateStockAndCreateOrderDetail(Map<Long, Long> basket, Order order) {
        //영속성 컨텍스트를 초기화
        entityManager.clear();

        List<Long> productIdList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : basket.entrySet()) {
            Long productId = entry.getKey();
            productIdList.add(productId);
        }

        //상품 객체 생성
        List<Product> productList = productFeignClient.getProductList(productIdList);

        //상품 수량 수정
        for(Product product : productList){
            product.updateStockAfterOrder(basket.get(product.getId()));
            System.out.println("현재 상품 수량: " + product.getStock());
        }

        //수정된 상품 저장
        productFeignClient.updateBulk(productList);

        //상품 상세정보 객체 저장
        List<OrderDetail> orderDetail = new ArrayList<>();
        for(Product product : productList){
            orderDetail.add(new OrderDetail(order.getId(), product.getId(), basket.get(product.getId()), product.getPrice(), product.getName()));
        }
        //orderDetail 저장
        orderDetailBulkRepository.saveBulk(orderDetail);
    }


    public List<OrderDetailResponseDto> getOrderDetailList(
        Long orderId
    ) {
        List<OrderDetail> listOfOrderedProducts = orderDetailRepository.findOrderDetailsByOrderId(
            orderId);
        return listOfOrderedProducts.stream().map(OrderDetailResponseDto::new).toList();
    }
    @Transactional
    public void deleteOrder(
        Long orderId
    ){
        orderDetailRepository.deleteAll(orderDetailRepository.findOrderDetailsByOrderId(orderId));
        Order order = orderRepository.findById(orderId).orElseThrow(()->new NotFoundException("주문을 찾을 수 없습니다."));
        orderRepository.delete(order);
    }
    public boolean checkUser(
        UserDetailsImpl userDetails,
        Long orderId
    ){
        Order order = orderRepository.findById(orderId).orElseThrow(()->new NotFoundException("주문을 찾을 수 없습니다"));
        return Objects.equals(userDetails.getUser().getId(),order.getUserId());
    }

//수량 검증 메서드
public void checkBasket(Map<Long, Long> basket, Order order) {
    List<Long> productIdList = new ArrayList<>();
    for (Map.Entry<Long, Long> entry : basket.entrySet()) {
        Long productId = entry.getKey();
        productIdList.add(productId);
    }



    List<Product> productList = productFeignClient.getProductList(productIdList);
    //String orderDetails = "Order ID: " + order.getId(); // 주문 상세 내용 //레디스로
    for(Product product : productList){
        Long stock = product.getStock();

        if (stock < basket.get(product.getId())) {
            //User user = addressFeignClient.getUser(order.getUserId());
            //String email = user.getEmail();// 주문한 사용자의 이메일 주소 가져오기
            //emailService.sendCancellationEmail(email, orderDetails, EmailType.STOCK_OUT); // 취소 이메일 발송
            //emailService.saveStock_Out_UserInfoToRedis(email, product.getId());
            System.out.println("재고부족");
            throw new BadRequestException(
                "상품 ID: " + product.getId() + ", 재고가 부족합니다. 요청 수량: " + basket.get(product.getId()) + ", 현재 재고: "
                    + stock);
        }
    }
}

    //주문서 조회 메서드
    public List<OrderResponseDto> getOrderList(
        UserDetailsImpl userDetails
    ) {
        List<Order> orderList = orderRepository.findOrdersByUserId(userDetails.getUser().getId());
        List<OrderResponseDto> ResponseList = new ArrayList<>();
        for (Order order : orderList) {
            Address address = addressFeignClient.findOne(order.getAddressId());
            OrderResponseDto orderResponseDto = new OrderResponseDto(order, address);
            ResponseList.add(orderResponseDto);
        }
        return ResponseList;
    }

    //가격의 합을 계산하는 메서드
    public long getTotalPrice(
        long orderId
    ) {
        List<OrderDetail> ListofOrderDetail = orderDetailRepository.findOrderDetailsByOrderId(orderId);
        long totalPrice = 0L;
        for (OrderDetail orderDetail : ListofOrderDetail) {
            totalPrice += orderDetail.getPrice() * orderDetail.getQuantity();
        }
        return totalPrice;
    }
    //주문 상세를 조회 하는 메서드
    public List<OrderDetail> getOrderDetails(
        Long userId,
        Long productId
    ) {
        return orderDetailRepository.findByUserIdAndProductIdAndReviewedIsFalse(userId, productId);
    }

    @Transactional
    public void saveOrderDetailReviewedState(
        OrderDetail orderDetail
    ) {
        orderDetail.setReviewed(true);
        orderDetailRepository.save(orderDetail);
    }

    // 1. @Transactional 제거!
    @Scheduled(fixedDelay = 300000)
    public void cancelUnpaidOrdersAndRestoreStock() {
        LocalDateTime minutesAgo = LocalDateTime.now().minusSeconds(10);
        List<Order> unpaidOrders = orderRepository.findUnpaidOrdersOlderThan(minutesAgo);

        for (Order order : unpaidOrders) {
            try {
                // 2. 개별 주문 처리를 별도 메서드로 분리하여 호출
                // 여기서 에러가 나도 catch에서 잡아서 다음 주문(for문)은 계속 진행됨
                processSingleOrderCancellation(order);
            } catch (Exception e) {
                // 로그만 남기고 에러를 삼킴 (스케줄러 중단 방지)
                log.error("주문 ID {} 취소 처리 중 오류 발생 (다음 주문 처리는 계속됨)", order.getId(), e);
            }
        }
    }

    // 3. 개별 주문 처리 로직 (private 메서드로 분리)
    private void processSingleOrderCancellation(Order order) {
        if (order.getState() == OrderState.NOTPAYED) {

            // [핵심 1] 주문 상태 변경 & 저장 (DB 커밋)
            // 상위 메서드에 @Transactional이 없으므로 save() 즉시 DB에 반영됨
            order.changeState(OrderState.CANCELLED);
            orderRepository.save(order);

            // [핵심 2] 재고 복구 (외부 서비스 호출)
            // 이 메서드 내부의 분산 락 로직은 정상 동작함
            restoreStock(order);

            // [핵심 3] 이메일 발송 (안전 장치 추가)
            // 여기서 SES 에러가 터져도 위 1, 2번은 이미 끝났으므로 롤백되지 않음
            try {
                User user = addressFeignClient.getUser(order.getUserId());
                OrderDetail orderDetail = orderDetailRepository.findOrderDetailByOrderId(order.getId());
                String orderDetails = "Order ID: " + orderDetail.getProductName();

                emailService.sendCancellationEmail(user.getEmail(), orderDetails, EmailType.PAYMENT_TIMEOUT);
            } catch (Exception e) {
                // 이메일 실패는 치명적이지 않으므로 WARN 로그만 찍고 넘어감
                log.warn("이메일 발송 실패 (주문 취소 및 재고 복구는 완료됨) - Order ID: {}, 원인: {}", order.getId(), e.getMessage());
            }
        }
    }
    @Transactional
    public void restoreStock(
        Order order
    ) {
        //주문서의 id를 가진 주문 상세를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        //주문 상세표들을 순회
        for (OrderDetail detail : orderDetails) {
            //100명이 한꺼번에 주문하면 한꺼번에 취소하는 상황에도 동시성이 필요함
            //락 적용
            String lockKey = "productLock:" + detail.getProductId();
            RLock rLock = redissonClient.getLock(lockKey);
            boolean isLocked = false;

            try {
                isLocked = rLock.tryLock(5, 10, TimeUnit.SECONDS);
                if (!isLocked) {
                    throw new RuntimeException("Unable to lock the product stock for product ID: "
                        + detail.getProductId());
                }
                //주문 상세에 있는 상품 객체를 불러옴
                //상품 객체의 수량을 복구
                Product product = productFeignClient.getProduct(detail.getProductId());
                product.updateStockAfterOrder(-detail.getQuantity());
                System.out.println("복구" + product.getStock());
                productFeignClient.save(product);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock acquisition interrupted", e);

            } finally {
                if (isLocked && rLock.isLocked()) {
                    rLock.unlock();
                }
            }
        }
    }
    public Order getById(Long orderId){
        return orderRepository.findById(orderId).orElseThrow();
    }

    public Map<Long, Order> getAllById(List<Long> orderIdList){
        List<Order> orderList = orderRepository.findAllByOrderId(orderIdList);

        Map<Long, Order> orderMap = new HashMap<>();

        for (int i=0; i<orderIdList.size(); i++){
            orderMap.put(orderIdList.get(i), orderList.get(i));
        }

        return orderMap;
    }
}

////@Transactional
////**********************스케쥴 메서드 수정*************************//
//@Scheduled(fixedDelay = 300000) // 5분에 한번씩 실행
//public void cancelUnpaidOrdersAndRestoreStock(
//) {
//    //시간 설정 변수 선언
//    LocalDateTime minutesAgo = LocalDateTime.now().minusSeconds(10);
//    // MinutesAgo 변수 설정 시간 이상 미결제 주문 조회
//    List<Order> unpaidOrders = orderRepository.findUnpaidOrdersOlderThan(minutesAgo);
//    //일정 시간이 지난 order 리스트를 순회 하며 주문을 취소 시키고 재고를 복구함
//    for (Order order : unpaidOrders) {
//        if (order.getState() == OrderState.NOTPAYED) {
//            order.changeState(OrderState.CANCELLED);
//            orderRepository.save(order);
//            restoreStock(order); // 재고 복구 로직
//            User user = addressFeignClient.getUser(order.getUserId());
//            String email = user.getEmail();// 주문한 사용자의 이메일 주소 가져오기
//            OrderDetail orderDetail = orderDetailRepository.findOrderDetailByOrderId(order.getId());
//            String orderDetails = "Order ID: " + orderDetail.getProductName(); // 주문 상세 내용
//            //emailService.sendCancellationEmail(email, orderDetails,EmailType.PAYMENT_TIMEOUT); // 취소 이메일 발송
//        }
//    }
//}



//    @Transactional
//    public void createOrder(
//        Map<Long, Long> basket,
//        UserDetailsImpl userDetails,
//        Long addressId
//    ) {
//        String lockKey = "order_lock";
//        RLock lock = redissonClient.getLock(lockKey);
//        try {
//            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
//            if (!isLocked) {
//                throw new RuntimeException("락 획득에 실패했습니다.");
//            }
//            System.out.println(userDetails.getUser().getId() + "번 유저가 주문을 합니다.");
//            // 주문 객체 생성
//            Order order = new Order(userDetails.getUser().getId(), OrderState.NOTPAYED, addressId);
//            // 상품 수량 검증 코드
//            checkBasket(basket, order);
//            // 주문 객체 저장
//            orderRepository.save(order);
//
//            // 장바구니를 순회하며
//            // 주문한 상품과 상품 개수를 상품 상세정보 테이블에 업데이트
//            // 상품 테이블의 상품 수량 업데이트
//            for (Map.Entry<Long, Long> entry : basket.entrySet()) {
//                Long productId = entry.getKey();
//                Long quantity = entry.getValue();
//                // 상태를 업데이트하는 메서드
//                updateStockAndCreateOrderDetail(productId, quantity, order);
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException("락 획득 중 오류가 발생했습니다.", e);
//        } finally {
//            if (lock.isLocked()) {
//                lock.unlock();
//            }
//        }
//    }

//    public void checkBasket(Map<Long, Long> basket, Order order) {
//        for (Map.Entry<Long, Long> entry : basket.entrySet()) {
//            Long productId = entry.getKey();
//            Long quantity = entry.getValue();
//            User user = addressFeignClient.getUser(order.getUserId());
//            String email = user.getEmail();// 주문한 사용자의 이메일 주소 가져오기
//            String orderDetails = "Order ID: " + order.getId(); // 주문 상세 내용
//            //레디스로
//            Long stock = productFeignClient.getProduct(productId).getStock();
//            if (stock == 0) {
//                System.out.println("재고부족");
//                emailService.sendCancellationEmail(email, orderDetails,
//                    EmailType.STOCK_OUT); // 취소 이메일 발송
//                emailService.saveStock_Out_UserInfoToRedis(email, productId);
//                throw new BadRequestException("상품 ID: " + productId + ", 재고가 없습니다.");
//            }
//            if (stock < quantity) {
//                System.out.println("재고부족2");
//                emailService.sendCancellationEmail(email, orderDetails,
//                    EmailType.STOCK_OUT); // 취소 이메일 발송
//                emailService.saveStock_Out_UserInfoToRedis(email, productId);
//                throw new BadRequestException(
//                    "상품 ID: " + productId + ", 재고가 부족합니다. 요청 수량: " + quantity + ", 현재 재고: "
//                        + stock);
//            }
//        }
//    }
//    상태를 업데이트하는 메서드
//    @Transactional
//    public void updateStockAndCreateOrderDetail(Long productId, Long quantity, Order order) {
//        //영속성 컨텍스트를 초기화
//        entityManager.clear();
//        //상품 객체 생성
//        Product product = productFeignClient.getProduct(productId);
//        //상품 수량 수정
//        product.updateStockAfterOrder(quantity);
//        System.out.println("현재 상품 수량: " + product.getStock());
//        //수정된 상품 저장
//        productFeignClient.save(product);
//        //상품 상세정보 객체 저장
//        OrderDetail orderDetail = new OrderDetail(order.getId(), productId, quantity, product.getPrice(), product.getName());
//        orderDetailRepository.save(orderDetail);
//    }
