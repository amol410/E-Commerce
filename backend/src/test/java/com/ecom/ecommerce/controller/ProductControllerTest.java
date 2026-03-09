package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.ProductDto;
import com.ecom.ecommerce.entity.Product;
import com.ecom.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProductService productService;

    @Test
    void getAll_publicEndpoint_returns200() throws Exception {
        Product p = Product.builder().id(1L).name("T-Shirt").price(new BigDecimal("499")).stock(10).active(true).build();
        when(productService.getAll(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getById_existingProduct_returns200() throws Exception {
        Product p = Product.builder().id(1L).name("Jeans").price(new BigDecimal("999")).stock(5).active(true).build();
        when(productService.getById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Jeans"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminUser_returns201() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("New Shirt");
        dto.setPrice(new BigDecimal("799"));
        dto.setStock(20);

        Product created = Product.builder().id(2L).name("New Shirt").price(new BigDecimal("799")).stock(20).active(true).build();
        when(productService.create(any(ProductDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("New Shirt"));
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Unauthorized Product");
        dto.setPrice(new BigDecimal("100"));
        dto.setStock(5);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
