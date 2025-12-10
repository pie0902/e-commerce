package org.example.tentrilliondollars.order.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.example.tentrilliondollars.address.entity.Address;
import org.example.tentrilliondollars.global.security.UserDetailsImpl;
import org.example.tentrilliondollars.product.entity.Product;
import org.example.tentrilliondollars.product.repository.ProductRepository;
import org.example.tentrilliondollars.user.entity.User;
import org.example.tentrilliondollars.user.entity.UserRoleEnum;
import org.example.tentrilliondollars.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
class OrderServiceTest {

    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private OrderService orderService;
    UserDetailsImpl userDetails;
    User user;
    Product product;
    Address address;
    Map<Long, Long> basket = new HashMap<>();

    @BeforeEach
    void setup() {
        user = new User(1L, "tester", "test@test.com", UserRoleEnum.USER);
        userDetails = new UserDetailsImpl(user);
        product = new Product("test",1000L,"test",1000L,user.getId());
        ReflectionTestUtils.setField(product,"id",1L);
        productRepository.save(product);
        address = new Address(1L, "test", "test", "test", user.getId());
        basket.put(1L, 1L);
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        // productRepository.getReferenceById가 호출될 때 product 객체를 반환하도록 설정
        Mockito.when(productRepository.getReferenceById(1L)).thenReturn(product);
    }
    @Test
    @DisplayName("병렬로 100번 실행하여 재고 업데이트 테스트")
    void test2() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            try {
                // createOrder 메서드로부터 Order의 ID를 받아온다
                orderService.createOrder(basket, userDetails, address.getId());
            } catch (Exception e) {
                Assertions.fail("주문 생성 중 예외 발생: " + e.getMessage());
            }
        });
    }
}










//    @BeforeEach
//        //public void createOrder(Map<Long, Long> basket, UserDetailsImpl userDetails, Long addressId)
//    void set() throws NoSuchFieldException {
//        user = new User(1L, "tester", "test@test.com", UserRoleEnum.USER);
//        userDetails = new UserDetailsImpl(user);
//        product = new Product("test",1000L,"test",1000L,user.getId());
//        ReflectionTestUtils.setField(product,"id",1L);
//        productRepository.save(product);
//        address = new Address(1L, "test", "test", "test", user.getId());
//        basket.put(1L, 1L);
//        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(product));
//        // productRepository.getReferenceById가 호출될 때 product 객체를 반환하도록 설정
//        Mockito.when(productRepository.getReferenceById(1L)).thenReturn(product);
//    }
//    @Test
//    @DisplayName("순차적으로 100번 실행하여 재고 업데이트 테스트")
//    void test() {
//        for (int i = 0; i < 100; i++) {
//            try {
//                orderService.createOrder(basket, userDetails, address.getId());
//            } catch (Exception e) {
//                Assertions.fail("주문 생성 중 예외 발생: " + e.getMessage());
//            }
//        }
//    }
    //테스트 코드2
//    @Test
//    @DisplayName("병렬로 100번 실행하여 재고 업데이트 테스트")
//    void test2() {
//        IntStream.range(0, 100).parallel().forEach(i -> {
//            try {
//                // createOrder 메서드로부터 Order의 ID를 받아온다
//                orderService.createOrder(basket, userDetails, address.getId());
//            } catch (Exception e) {
//                Assertions.fail("주문 생성 중 예외 발생: " + e.getMessage());
//            }
//        });
//    }
//}


//    public void createMultipleUsers() {
//        for (int i = 1; i <= 100; i++) {
//            String username = "user" + i;
//            String email = "user" + i + "@example.com";
//            String password = "Test1234!"; // 예시 비밀번호, 실제로는 각 사용자별로 고유한 비밀번호를 설정해야 함
//
//            // 비밀번호 암호화
//            String encodedPassword = passwordEncoder.encode(password);
//
//            // 사용자 역할 설정 (예시에서는 모든 사용자를 일반 사용자로 설정)
//            UserRoleEnum role = UserRoleEnum.USER;
//
//            // 사용자 생성 및 저장
//            userRepository.save(User.builder()
//                .email(email)
//                .username(username)
//                .password(encodedPassword)
//                .role(role)
//                .build());
//        }
//    }

