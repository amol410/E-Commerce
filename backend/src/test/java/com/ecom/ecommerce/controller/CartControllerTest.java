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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;
    @MockBean private UserRepository userRepository;

    private User mockUser;
    private Cart mockCart;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).name("Jane").email("jane@example.com").build();
        mockCart = Cart.builder().id(1L).user(mockUser).items(new ArrayList<>()).build();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void getCart_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void getCart_authenticated_returns200() throws Exception {
        when(cartService.getCartByUserId(1L)).thenReturn(mockCart);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void addItem_validRequest_returns200() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(cartService.addItem(eq(1L), any(CartItemRequest.class))).thenReturn(mockCart);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void addItem_missingProductId_returns400() throws Exception {
        CartItemRequest request = new CartItemRequest();
        // productId is null
        request.setQuantity(2);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void addItem_quantityZero_returns400() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(0);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_unauthenticated_returns403() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(1);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void updateItem_validRequest_returns200() throws Exception {
        when(cartService.updateItemQuantity(eq(1L), eq(5L), eq(3))).thenReturn(mockCart);

        mockMvc.perform(put("/api/cart/update/5")
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void removeItem_validItemId_returns200() throws Exception {
        when(cartService.removeItem(eq(1L), eq(5L))).thenReturn(mockCart);

        mockMvc.perform(delete("/api/cart/remove/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void removeItem_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/cart/remove/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jane@example.com")
    void clearCart_authenticated_returns200() throws Exception {
        doNothing().when(cartService).clearCart(1L);

        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void clearCart_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isForbidden());
    }
}
