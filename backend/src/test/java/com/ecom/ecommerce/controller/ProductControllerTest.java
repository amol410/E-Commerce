package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.ProductDto;
import com.ecom.ecommerce.entity.Product;
import com.ecom.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.ecom.ecommerce.config.TestMvcConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestMvcConfig.class)

class ProductControllerTest {

    @Autowired private MockMvcTester mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ProductService productService;

    @Test
    void getAll_publicEndpoint_returns200() {
        Product p = Product.builder().id(1L).name("T-Shirt").price(new BigDecimal("499")).stock(10).active(true).build();
        when(productService.getAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));

        assertThat(mockMvc.get().uri("/api/products").exchange())
                .hasStatus(200)
                .bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void getById_existingProduct_returns200() {
        Product p = Product.builder().id(1L).name("Jeans").price(new BigDecimal("999")).stock(5).active(true).build();
        when(productService.getById(1L)).thenReturn(p);

        MvcTestResult result = mockMvc.get().uri("/api/products/1").exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.data.name").isEqualTo("Jeans");
    }

    @Test
    void create_adminUser_returns201() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("New Shirt");
        dto.setPrice(new BigDecimal("799"));
        dto.setStock(20);

        Product created = Product.builder().id(2L).name("New Shirt").price(new BigDecimal("799")).stock(20).active(true).build();
        when(productService.create(any(ProductDto.class))).thenReturn(created);

        MvcTestResult result = mockMvc.post().uri("/api/products")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange();

        assertThat(result).hasStatus(201);
        assertThat(result).bodyJson().extractingPath("$.data.name").isEqualTo("New Shirt");
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Unauthorized Product");
        dto.setPrice(new BigDecimal("100"));
        dto.setStock(5);

        assertThat(mockMvc.post().uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange())
                .hasStatus(403);
    }
}
