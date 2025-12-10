package org.example.tentrilliondollars.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tentrilliondollars.global.TimeStamped;
import org.example.tentrilliondollars.review.dto.ReviewRequest;

@Entity
@Table(name="review")
@Getter
@NoArgsConstructor
public class Review extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String content;
    @Column
    private String imageUrl;
    @Column
    private int score;

    @Column
    Long userId;

    @Column
    Long productId;


    public Review(
        ReviewRequest reviewRequest,
        Long productId,
        Long userId
    ){
        this.content = reviewRequest.getContent();
        this.score = reviewRequest.getScore();
        this.productId = productId;
        this.userId = userId;
    }
    public void updateReview(
        ReviewRequest reviewRequest
    ) {
        this.content = reviewRequest.getContent();
        this.score = reviewRequest.getScore();
    }
    public void updateImageUrl(String imageUrl){
        this.imageUrl =imageUrl;
    }

}
