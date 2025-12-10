
package org.example.tentrilliondollars.order.repository;


import org.example.tentrilliondollars.order.entity.Order;
import org.example.tentrilliondollars.order.entity.OrderDetail;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    //review 검증1
//    @Query("SELECT COUNT(od) FROM OrderDetail od WHERE od.order.userId= :userId AND od.productId = :productId")
//    long countByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    //관리자 페이지에 주문서를 불러오는 쿼리
    List<OrderDetail> findByProductId(Long productId);
    //유저의 Id와 상품의 Id로 주문서를 찾는 쿼리
    //List<OrderDetail> findByOrderUserIdAndProductId(Long userId, Long productId);
    //오더 디테일 가져오기
    /*
    @Query("SELECT od FROM OrderDetail od WHERE od.order.userId = :userId AND od.productId = :productId AND od.reviewed = false")
    List<OrderDetail> findByOrder_UserIdAndProductIdAndReviewedIsFalse(@Param("userId") Long userId, @Param("productId") Long productId);
*/

    @Query("SELECT od FROM OrderDetail od WHERE od.orderId IN (SELECT o.id FROM Order o WHERE o.userId = :userId) AND od.productId = :productId AND od.reviewed = false")
    List<OrderDetail> findByUserIdAndProductIdAndReviewedIsFalse(@Param("userId") Long userId, @Param("productId") Long productId);

    List<OrderDetail> findByOrderId(Long orderId);
    List<OrderDetail> findOrderDetailsByOrderId(Long orderId);
    OrderDetail findOrderDetailByOrderId(Long orderId);
}
