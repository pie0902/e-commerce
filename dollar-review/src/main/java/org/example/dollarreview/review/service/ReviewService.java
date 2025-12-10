package org.example.dollarreview.review.service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.dollarreview.domain.order.OrderDetail;
import org.example.dollarreview.domain.product.Product;
import org.example.dollarreview.feign.OrderFeignClient;
import org.example.dollarreview.feign.ProductFeignClient;
import org.example.dollarreview.review.dto.ReviewRequest;
import org.example.dollarreview.review.dto.ReviewResponse;
import org.example.dollarreview.review.entity.Review;
import org.example.dollarreview.review.repository.ReviewRepository;
import org.example.dollarreview.s3.S3Service;
import org.example.share.config.global.exception.AccessDeniedException;
import org.example.share.config.global.exception.BadRequestException;
import org.example.share.config.global.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductFeignClient productService;
    private final OrderFeignClient orderService;
    private final S3Service s3Service;

    @Value("${review.bucket.name}")
    String bucketName;
    //리뷰 생성
    @Transactional
    public void createReview(
        Long productId,
        ReviewRequest reviewRequest,
        Long userId
    ) {
        //상품 객체 생성
        Product product = productService.getProduct(productId);
        //userId와 productId를 전달해서 해당하는 주문 상세를 가져옴
        List<OrderDetail> orderDetails = orderService.getOrderDetails(userId, productId);
        //삭제된 상품인지 아닌지 확인
        checkProductStateIsFalse(product);
        //주문 상세가 없으면
        if (orderDetails.isEmpty()) {
            throw new BadRequestException("주문한 상품에 대해서만 리뷰 작성이 가능합니다.");
        }
        //주문 상세중에서 제일 첫번째를 가져옴
        //orderDetailRepository 쿼리에서 리뷰 작성 가능한 글만 긁어옴
        //그 중 첫번째를 가져옴
        OrderDetail orderDetail = orderDetails.get(0);
        //리뷰 작성 여부를 확인함(한번 더 검증)
        if (orderDetail.isReviewed()) {
            throw new BadRequestException("이미 작성한 리뷰 입니다.");
        }
        //score 1~5 사이로 작성했는지 검증
        validateReviewScore(reviewRequest.getScore());
        //리뷰 객체 생성
        Review review = new Review(reviewRequest, productId, userId);
        //orderDetail 리뷰 작성 상태를 업로드
        orderService.saveOrderDetailReviewedState(orderDetail);
        //리뷰 저장
        reviewRepository.save(review);
    }


    // 게시글 전체 조회
    public List<ReviewResponse> getAllReviews(
        Long productId
    ) {
        //상품 객체 생성
        Product product = productService.getProduct(productId);
        //삭제된 상품인지 아닌지 확인
        checkProductStateIsFalse(product);
        //가져온 리뷰 List, DTO 로 변환해서 반환
        List<Review> reviewList = reviewRepository.findByProductId(productId);
        return reviewList.stream()
            .map(ReviewResponse::new)
            .collect(Collectors.toList());
    }

    //리뷰 삭제
    public void deleteReview(
        Long reviewId,
        Long userId,
        Long productId
    ) {
        //상품 객체 생성
        Product product = productService.getProduct(productId);
        //삭제된 상품인지 아닌지 확인
        checkProductStateIsFalse(product);
        //리뷰 객체 가져옴
        Review review = findReviewByIdOrThrow(reviewId);
        //권한 확인 (본인이 작성한 글인지)
        checkAuthorization(review, userId);
        //리뷰 삭제
        reviewRepository.delete(review);
    }

    @Transactional
    public void updateReview(
        Long reviewId,
        ReviewRequest reviewRequest,
        Long userId,
        Long productId
    ) {
        //리뷰 객체 생성
        Product product = productService.getProduct(productId);
        //삭제된 상품인지 아닌지 확인
        checkProductStateIsFalse(product);
        //리뷰 객체 가져옴
        Review review = findReviewByIdOrThrow(reviewId);
        //유저 권한 확인
        checkAuthorization(review, userId);
        //score 1~5 사이로 작성했는지 검증
        validateReviewScore(reviewRequest.getScore());
        //리뷰 업데이트
        review.updateReview(reviewRequest);
    }

    //=====================예외 처리 메서드================================//
    //리뷰 유무 메서드
    public Review findReviewByIdOrThrow(
        Long reviewId
    ) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("리뷰를 찾을 수 없습니다."));
    }

    //유저 권한 확인 메서드
    public void checkAuthorization(
        Review review,
        Long userId
    ) {
        if (!review.getUserId().equals(userId)) {
            throw new AccessDeniedException("다른 유저의 게시글을 수정/삭제할 수 없습니다.");
        }
    }
    private void validateReviewScore(
        int score
    ) {
        if (score < 1 || score > 5) {
            throw new BadRequestException("1점부터 5점까지 입력해주세요");
        }
    }

    //리뷰 가져오기
    public Review getReview(
        Long reviewId
    ) {
        return reviewRepository.findById(reviewId).orElseThrow(
            () -> new NotFoundException("해당 상품이 존재하지 않습니다.")
        );
    }

    //리뷰 이미지 업로드
    public void uploadReviewImage(
        Long reviewId,
        MultipartFile file
    ) throws IOException {
        String imageKey = UUID.randomUUID().toString();
        String format = "review-images/%s/%s".formatted(reviewId, imageKey) + ".PNG";
        s3Service.putObject(bucketName, format, file);
        String url = "https://" + bucketName + ".s3" + ".ap-northeast-2.amazonaws.com/" + format;
        Review review = getReview(reviewId);
        review.updateImageUrl(url);
        reviewRepository.save(review);
    }

    //리뷰 이미지 가져오기
    public String getReviewImage(
        Long reviewId
    ) {
        try {
            return getReview(reviewId).getImageUrl();
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("요청한 리뷰 이미지가 S3 버킷에 존재하지 않습니다. 이미지 키를 확인해주세요.");
        }
    }
    //상품 soft delete 확인
    public void checkProductStateIsFalse(
        Product product
    ) {
        if (!product.isState()) {
            throw new NotFoundException("해당 상품은 삭제되었습니다.");
        }
    }

}
