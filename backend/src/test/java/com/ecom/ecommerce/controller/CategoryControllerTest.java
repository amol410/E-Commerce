package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.CategoryDto;
import com.ecom.ecommerce.entity.Category;
import com.ecom.ecommerce.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.ecom.ecommerce.config.TestMvcConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestMvcConfig.class)

class CategoryControllerTest {

    @Autowired private MockMvcTester mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CategoryService categoryService;

    @Test
    void getAll_publicEndpoint_returns200() {
        Category cat = Category.builder().id(1L).name("Electronics").build();
        when(categoryService.getAll()).thenReturn(List.of(cat));

        MvcTestResult result = mockMvc.get().uri("/api/categories").exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data[0].name").isEqualTo("Electronics");
    }

    @Test
    void getTree_publicEndpoint_returns200() {
        Category parent = Category.builder().id(1L).name("Electronics").build();
        when(categoryService.getCategoryTree()).thenReturn(List.of(parent));

        MvcTestResult result = mockMvc.get().uri("/api/categories/tree").exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data[0].name").isEqualTo("Electronics");
    }

    @Test
    void getById_existingId_returns200() {
        Category cat = Category.builder().id(1L).name("Clothing").build();
        when(categoryService.getById(1L)).thenReturn(cat);

        MvcTestResult result = mockMvc.get().uri("/api/categories/1").exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.data.name").isEqualTo("Clothing");
    }

    @Test
    void create_adminUser_returns201() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Footwear");

        Category created = Category.builder().id(2L).name("Footwear").build();
        when(categoryService.create(any(CategoryDto.class))).thenReturn(created);

        MvcTestResult result = mockMvc.post().uri("/api/categories")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange();

        assertThat(result).hasStatus(201);
        assertThat(result).bodyJson().extractingPath("$.data.name").isEqualTo("Footwear");
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Footwear");

        assertThat(mockMvc.post().uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange())
                .hasStatus(403);
    }

    @Test
    void create_regularUser_returns403() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Footwear");

        assertThat(mockMvc.post().uri("/api/categories")
                .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange())
                .hasStatus(403);
    }

    @Test
    void create_missingName_returns400() throws Exception {
        CategoryDto dto = new CategoryDto();
        // name is blank

        assertThat(mockMvc.post().uri("/api/categories")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange())
                .hasStatus(400);
    }

    @Test
    void update_adminUser_returns200() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated Category");

        Category updated = Category.builder().id(1L).name("Updated Category").build();
        when(categoryService.update(eq(1L), any(CategoryDto.class))).thenReturn(updated);

        MvcTestResult result = mockMvc.put().uri("/api/categories/1")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.data.name").isEqualTo("Updated Category");
    }

    @Test
    void delete_adminUser_returns200() {
        doNothing().when(categoryService).delete(1L);

        MvcTestResult result = mockMvc.delete().uri("/api/categories/1")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void delete_unauthenticated_returns403() {
        assertThat(mockMvc.delete().uri("/api/categories/1").exchange())
                .hasStatus(403);
    }
}
