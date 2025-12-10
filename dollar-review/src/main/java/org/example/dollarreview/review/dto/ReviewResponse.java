package org.example.dollarreview.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.dollarreview.review.entity.Review;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 응답 객체")
public class ReviewResponse {
    @Schema(description = "리뷰 ID", example = "123")
    private Long id;

    @Schema(description = "리뷰 내용", example = "정말 만족합니다!")
    private String content;

    @Schema(description = "평점(1~5)", example = "5")
    private int score;

    public ReviewResponse(Review review){
        this.id = review.getId();
        this.content = review.getContent();
        this.score = review.getScore();
    }
}
