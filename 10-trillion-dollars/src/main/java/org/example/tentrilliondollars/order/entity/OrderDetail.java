package org.example.tentrilliondollars.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long productId;

    @Column
    private String productName;

    @Column
    private Long price;

    @Column
    private Long orderId;

    @Column
    private Long quantity;
    @Column(nullable = false)
    private boolean reviewed = false;


    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public OrderDetail(Long orderId,Long productId,Long quantity,Long price,String productName) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.orderId = orderId;
        this.price = price;
    }


}


