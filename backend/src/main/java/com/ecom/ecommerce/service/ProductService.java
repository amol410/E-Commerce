package com.ecom.ecommerce.service;

import com.ecom.ecommerce.dto.ProductDto;
import com.ecom.ecommerce.entity.Category;
import com.ecom.ecommerce.entity.Product;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.CategoryRepository;
import com.ecom.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<Product> getAll(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        // If all filters are null, return all active products
        if (name == null && categoryId == null && minPrice == null && maxPrice == null) {
            return productRepository.findByActiveTrue(pageable);
        }
        if (name != null && categoryId == null && minPrice == null && maxPrice == null) {
            return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
        }
        if (categoryId != null && name == null && minPrice == null && maxPrice == null) {
            return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        }
        if (minPrice != null && maxPrice != null && name == null && categoryId == null) {
            return productRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice, pageable);
        }
        // Fallback — return all active
        return productRepository.findByActiveTrue(pageable);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public Product create(ProductDto dto) {
        Product product = buildFromDto(dto, new Product());
        return productRepository.save(product);
    }

    public Product update(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        buildFromDto(dto, product);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        // Soft delete
        product.setActive(false);
        productRepository.save(product);
    }

    private Product buildFromDto(ProductDto dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));
            product.setCategory(category);
        }
        return product;
    }
}
