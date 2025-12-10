package org.example.dollarreview.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 작성/수정 요청 바디")
public class ReviewRequest {
    @Schema(description = "리뷰 내용", example = "정말 만족합니다!")
    private String content;

    @Schema(description = "평점(1~5)", example = "5", minimum = "1", maximum = "5")
    private int score;
}
