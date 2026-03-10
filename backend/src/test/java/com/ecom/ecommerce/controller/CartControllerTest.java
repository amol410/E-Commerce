package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.CartItemRequest;
import com.ecom.ecommerce.entity.Cart;
import com.ecom.ecommerce.entity.User;
import com.ecom.ecommerce.repository.UserRepository;
import com.ecom.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.ecom.ecommerce.config.TestMvcConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestMvcConfig.class)

class CartControllerTest {

    @Autowired private MockMvcTester mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CartService cartService;
    @MockitoBean private UserRepository userRepository;

    private User mockUser;
    private Cart mockCart;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).name("Jane").email("jane@example.com").build();
        mockCart = Cart.builder().id(1L).user(mockUser).items(new ArrayList<>()).build();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void getCart_unauthenticated_returns403() {
        assertThat(mockMvc.get().uri("/api/cart").exchange())
                .hasStatus(403);
    }

    @Test
    void getCart_authenticated_returns200() {
        when(cartService.getCartByUserId(1L)).thenReturn(mockCart);

        assertThat(mockMvc.get().uri("/api/cart")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .exchange())
                .hasStatus(200)
                .bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void addItem_validRequest_returns200() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(cartService.addItem(eq(1L), any(CartItemRequest.class))).thenReturn(mockCart);

        assertThat(mockMvc.post().uri("/api/cart/add")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(200)
                .bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void addItem_missingProductId_returns400() throws Exception {
        CartItemRequest request = new CartItemRequest();
        // productId is null
        request.setQuantity(2);

        assertThat(mockMvc.post().uri("/api/cart/add")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(400);
    }

    @Test
    void addItem_quantityZero_returns400() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(0);

        assertThat(mockMvc.post().uri("/api/cart/add")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(400);
    }

    @Test
    void addItem_unauthenticated_returns403() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(1);

        assertThat(mockMvc.post().uri("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(403);
    }

    @Test
    void updateItem_validRequest_returns200() {
        when(cartService.updateItemQuantity(eq(1L), eq(5L), eq(3))).thenReturn(mockCart);

        assertThat(mockMvc.put().uri("/api/cart/update/5")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .param("quantity", "3")
                .exchange())
                .hasStatus(200)
                .bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void removeItem_validItemId_returns200() {
        when(cartService.removeItem(eq(1L), eq(5L))).thenReturn(mockCart);

        assertThat(mockMvc.delete().uri("/api/cart/remove/5")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .exchange())
                .hasStatus(200)
                .bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void removeItem_unauthenticated_returns403() {
        assertThat(mockMvc.delete().uri("/api/cart/remove/5").exchange())
                .hasStatus(403);
    }

    @Test
    void clearCart_authenticated_returns200() {
        doNothing().when(cartService).clearCart(1L);

        assertThat(mockMvc.delete().uri("/api/cart/clear")
                .with(SecurityMockMvcRequestPostProcessors.user("jane@example.com"))
                .exchange())
                .hasStatus(200)
                .bodyJson().extractingPath("$.success").isEqualTo(true);
    }

    @Test
    void clearCart_unauthenticated_returns403() {
        assertThat(mockMvc.delete().uri("/api/cart/clear").exchange())
                .hasStatus(403);
    }
}
