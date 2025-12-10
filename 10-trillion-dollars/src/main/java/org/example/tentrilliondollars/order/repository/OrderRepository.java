package org.example.tentrilliondollars.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.tentrilliondollars.order.entity.Order;
import org.example.tentrilliondollars.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByUserId(Long userId);
    @Query("SELECT o FROM Order o WHERE o.state = 'NOTPAYED' AND o.createdAt < :cutoff")
    List<Order> findUnpaidOrdersOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
