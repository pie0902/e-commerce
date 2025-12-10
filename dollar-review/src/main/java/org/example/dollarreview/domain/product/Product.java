package org.example.dollarreview.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.dollarreview.global.TimeStamped;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
    @Column
    private Long price;
    @Column
    private String description;
    @Column
    private Long stock;
    @Column
    private String imageKey;
    @Column
    private boolean state;
    @Column
    private Long userId;

    @Builder
    public Product(String name, Long price, String description, Long stock, String photo,
        Long userId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.userId = userId;
        this.state = true;
    }

}

