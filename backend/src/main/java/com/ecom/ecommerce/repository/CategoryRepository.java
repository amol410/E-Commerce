package com.ecom.ecommerce.repository;

import com.ecom.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Fetch only root categories (no parent)
    List<Category> findByParentIsNull();

    boolean existsByName(String name);

    // Product count per category
    @Query("SELECT c.id, COUNT(p) FROM Category c LEFT JOIN c.products p WHERE p.active = true GROUP BY c.id")
    List<Object[]> countProductsPerCategory();
}
