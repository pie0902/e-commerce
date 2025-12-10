package org.example.tentrilliondollars.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tentrilliondollars.address.entity.Address;
import org.example.tentrilliondollars.global.TimeStamped;
import org.example.tentrilliondollars.user.entity.User;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends TimeStamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(value = EnumType.STRING)
    private OrderState state;

    @Column
    private Long userId;

    @Column
    private Long addressId;

    @Column
    private String KakaoTid;
    public Order(Long userId,OrderState state,Long addressId){
        this.userId = userId;
        this.state = state;
        this.addressId = addressId;
    }

    public void changeState(OrderState state){
        this.state = state;
    }
    public void updateTid(String tid){
        this.KakaoTid=tid;
    }

}
