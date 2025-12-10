package org.example.dollarproduct.product.repository;

import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dollarproduct.product.entity.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProductBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void UpdateBulk(List<Product> productList) {
        String sql = "UPDATE product p SET p.name = ?, p.price = ?, p.description = ?, p.stock = ?, p.image_url = ?, p.state = ?, p.user_id = ? WHERE p.id = ?";

        jdbcTemplate.batchUpdate(sql,
            productList,
            productList.size(),
            (PreparedStatement ps, Product product) -> {
                ps.setString(1, product.getName());
                ps.setLong(2, product.getPrice());
                ps.setString(3, product.getDescription());
                ps.setLong(4, product.getStock());
                ps.setString(5, product.getImageUrl());
                ps.setBoolean(6, product.isState());
                ps.setLong(7, product.getUserId());
                ps.setLong(8, product.getId());
            });
    }

}
