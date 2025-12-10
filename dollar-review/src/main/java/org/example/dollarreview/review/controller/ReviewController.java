package org.example.dollarreview.review.controller;

import java.io.IOException;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dollarreview.review.dto.ReviewRequest;
import org.example.dollarreview.review.dto.ReviewResponse;
import org.example.dollarreview.review.service.ReviewService;
import org.example.share.config.global.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products/{productId}")
@Tag(name = "리뷰", description = "상품 리뷰 CRUD 및 이미지 업로드 API")
public class ReviewController {
    private final ReviewService reviewService;
    @PostMapping("/reviews")
    @Operation(
        summary = "리뷰 작성",
        description = "특정 상품에 대한 리뷰를 작성합니다.",
        responses = {
            @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
        }
    )
    public ResponseEntity<String> createReview(
        @Parameter(description = "리뷰 대상 상품 ID", required = true) @PathVariable Long productId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "리뷰 내용 및 평점",
            required = true,
            content = @Content(schema = @Schema(implementation = ReviewRequest.class))
        ) @RequestBody ReviewRequest reviewRequest,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewService.createReview(productId, reviewRequest, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body("후기가 등록되었습니다.");
    }
    @GetMapping("/reviews")
    @Operation(
        summary = "리뷰 목록 조회",
        description = "특정 상품의 리뷰 목록을 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReviewResponse.class)))
        }
    )
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
        @Parameter(description = "리뷰 대상 상품 ID", required = true) @PathVariable Long productId
    ) {
        List<ReviewResponse> responseList = reviewService.getAllReviews(productId);
        return ResponseEntity.ok().body(responseList);
    }
    //리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    public ResponseEntity<String> deleteReview(
        @Parameter(description = "삭제할 리뷰 ID", required = true) @PathVariable Long reviewId,
        @Parameter(description = "리뷰 대상 상품 ID", required = true) @PathVariable Long productId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        reviewService.deleteReview(reviewId,userDetails.getUser().getId(),productId);
        return ResponseEntity.ok().body("리뷰가 삭제 되었습니다.");
    }
    //리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰 내용을 수정합니다.")
    public ResponseEntity<String> updateReview(
        @Parameter(description = "수정할 리뷰 ID", required = true) @PathVariable Long reviewId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 리뷰 내용 및 평점",
            required = true,
            content = @Content(schema = @Schema(implementation = ReviewRequest.class))
        ) @RequestBody ReviewRequest reviewRequest,
        @Parameter(description = "리뷰 대상 상품 ID", required = true) @PathVariable Long productId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewService.updateReview(reviewId,reviewRequest,userDetails.getUser().getId(),productId);
        return ResponseEntity.ok().body("리뷰가 수정 되었습니다.");
    }

    @GetMapping("reviews/{reviewId}/image")
    @Operation(summary = "리뷰 이미지 조회", description = "리뷰에 업로드된 이미지 URL을 반환합니다.")
    public String getReviewImage(@PathVariable Long reviewId) throws IOException{
        return reviewService.getReviewImage(reviewId);
    }

    @PostMapping("reviews/{reviewId}/image")
    @Operation(summary = "리뷰 이미지 업로드", description = "리뷰 이미지 파일을 업로드합니다.")
    public void uploadReviewImage(@PathVariable Long reviewId, @RequestParam("file") MultipartFile file) throws IOException {
        reviewService.uploadReviewImage(reviewId,file);
    }


}
