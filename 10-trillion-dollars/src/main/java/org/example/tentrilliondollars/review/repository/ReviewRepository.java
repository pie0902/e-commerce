package org.example.tentrilliondollars.review.repository;

import java.util.List;
import org.example.tentrilliondollars.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    //별점 내림차순 정렬
    List<Review> findAllByOrderByScoreDesc();
    //삭제가 안된 상품만 찾기
    List<Review> findByProductId(Long productId);
    Long countByUserIdAndProductId(Long userId,Long productId);
}
