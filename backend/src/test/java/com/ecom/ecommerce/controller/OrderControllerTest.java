package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.entity.Order;
import com.ecom.ecommerce.entity.User;
import com.ecom.ecommerce.entity.enums.OrderStatus;
import com.ecom.ecommerce.repository.UserRepository;
import com.ecom.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private UserRepository userRepository;

    private User mockUser;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).name("Bob").email("bob@example.com").build();
        mockOrder = Order.builder()
                .id(1L)
                .user(mockUser)
                .status(OrderStatus.PLACED)
                .totalAmount(new BigDecimal("1299.00"))
                .items(new ArrayList<>())
                .build();
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void placeOrder_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/orders/place"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob@example.com")
    void placeOrder_authenticated_returns201() throws Exception {
        when(orderService.placeOrder(1L)).thenReturn(mockOrder);

        mockMvc.perform(post("/api/orders/place"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PLACED"))
                .andExpect(jsonPath("$.data.totalAmount").value(1299.00));
    }

    @Test
    @WithMockUser(username = "bob@example.com")
    void getMyOrders_authenticated_returns200() throws Exception {
        when(orderService.getOrderHistory(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockOrder)));

        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PLACED"));
    }

    @Test
    void getMyOrders_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob@example.com")
    void getOrderById_ownOrder_returns200() throws Exception {
        when(orderService.getOrderById(1L, 1L)).thenReturn(mockOrder);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getOrderById_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isForbidden());
    }
}
