package com.ecom.ecommerce.service;

import com.ecom.ecommerce.dto.ProductDto;
import com.ecom.ecommerce.entity.Product;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.CategoryRepository;
import com.ecom.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("499.00"))
                .stock(10)
                .active(true)
                .build();
    }

    @Test
    void getById_existingProduct_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
    }

    @Test
    void getById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    @Test
    void create_validDto_savesAndReturnsProduct() {
        ProductDto dto = new ProductDto();
        dto.setName("New Product");
        dto.setPrice(new BigDecimal("999.00"));
        dto.setStock(5);

        Product created = Product.builder().id(2L).name("New Product").price(new BigDecimal("999.00")).stock(5).active(true).build();
        when(productRepository.save(any(Product.class))).thenReturn(created);

        Product result = productService.create(dto);

        assertThat(result.getName()).isEqualTo("New Product");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void delete_existingProduct_softDeletes() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.delete(1L);

        verify(productRepository).save(argThat(p -> !p.getActive()));
    }

    @Test
    void getById_inactiveProduct_throwsNotFound() {
        sampleProduct.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
