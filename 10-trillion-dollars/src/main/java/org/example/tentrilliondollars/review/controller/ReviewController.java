package org.example.tentrilliondollars.review.controller;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.tentrilliondollars.global.security.UserDetailsImpl;
import org.example.tentrilliondollars.review.dto.ReviewRequest;
import org.example.tentrilliondollars.review.dto.ReviewResponse;
import org.example.tentrilliondollars.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products/{productId}")
public class ReviewController {
    private final ReviewService reviewService;
    @PostMapping("/reviews")
    public ResponseEntity<String> createReview(
        @PathVariable Long productId,
        @RequestBody ReviewRequest reviewRequest,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewService.createReview(productId, reviewRequest, userDetails.getUser().getId());
        return ResponseEntity.ok().body("후기가 등록 되었습니다.");
    }
    @GetMapping("/reviews")
    //리뷰 전체 조회
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
        @PathVariable Long productId
    ) {
        List<ReviewResponse> responseList = reviewService.getAllReviews(productId);
        return ResponseEntity.ok().body(responseList);
    }
    //리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(
        @PathVariable Long reviewId,
        @PathVariable Long productId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        reviewService.deleteReview(reviewId,userDetails.getUser().getId(),productId);
        return ResponseEntity.ok().body("리뷰가 삭제 되었습니다.");
    }
    //리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(
        @PathVariable Long reviewId,
        @RequestBody ReviewRequest reviewRequest,
        @PathVariable Long productId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reviewService.updateReview(reviewId,reviewRequest,userDetails.getUser().getId(),productId);
        return ResponseEntity.ok().body("리뷰가 수정 되었습니다.");
    }

    @GetMapping("reviews/{reviewId}/image")
    public String getReviewImage(@PathVariable Long reviewId) throws IOException{
        return reviewService.getReviewImage(reviewId);
    }

    @PostMapping("reviews/{reviewId}/image")
    public void uploadReviewImage(@PathVariable Long reviewId, @RequestParam("file") MultipartFile file) throws IOException {
        reviewService.uploadReviewImage(reviewId,file);
    }


}
