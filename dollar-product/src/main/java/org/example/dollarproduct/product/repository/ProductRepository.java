package org.example.dollarproduct.product.repository;


import java.util.List;
import org.example.dollarproduct.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {


    Page<Product> findAllByUserIdAndStateTrue(Long userId, Pageable pageable);

    Page<Product> findAllByStateTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStateTrue(String search, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN (:productIdList)")
    List<Product> findAllByProductIdList(List<Long> productIdList);
}
