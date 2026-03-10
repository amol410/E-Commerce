package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.CategoryDto;
import com.ecom.ecommerce.entity.Category;
import com.ecom.ecommerce.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;

    @Test
    void getAll_publicEndpoint_returns200() throws Exception {
        Category cat = Category.builder().id(1L).name("Electronics").build();
        when(categoryService.getAll()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    void getTree_publicEndpoint_returns200() throws Exception {
        Category parent = Category.builder().id(1L).name("Electronics").build();
        when(categoryService.getCategoryTree()).thenReturn(List.of(parent));

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        Category cat = Category.builder().id(1L).name("Clothing").build();
        when(categoryService.getById(1L)).thenReturn(cat);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Clothing"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminUser_returns201() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Footwear");

        Category created = Category.builder().id(2L).name("Footwear").build();
        when(categoryService.create(any(CategoryDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Footwear"));
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Footwear");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_regularUser_returns403() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Footwear");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_missingName_returns400() throws Exception {
        CategoryDto dto = new CategoryDto();
        // name is blank

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminUser_returns200() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated Category");

        Category updated = Category.builder().id(1L).name("Updated Category").build();
        when(categoryService.update(eq(1L), any(CategoryDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminUser_returns200() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isForbidden());
    }
}
