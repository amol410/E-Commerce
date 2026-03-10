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

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.ecom.ecommerce.config.TestMvcConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestMvcConfig.class)

class OrderControllerTest {

    @Autowired private MockMvcTester mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrderService orderService;
    @MockitoBean private UserRepository userRepository;

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
    void placeOrder_unauthenticated_returns403() {
        assertThat(mockMvc.post().uri("/api/orders/place").exchange())
                .hasStatus(403);
    }

    @Test
    void placeOrder_authenticated_returns201() {
        when(orderService.placeOrder(1L)).thenReturn(mockOrder);

        MvcTestResult result = mockMvc.post().uri("/api/orders/place")
                .with(SecurityMockMvcRequestPostProcessors.user("bob@example.com"))
                .exchange();

        assertThat(result).hasStatus(201);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data.status").isEqualTo("PLACED");
        assertThat(result).bodyJson().extractingPath("$.data.totalAmount").isEqualTo(1299.00);
    }

    @Test
    void getMyOrders_authenticated_returns200() {
        when(orderService.getOrderHistory(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockOrder)));

        MvcTestResult result = mockMvc.get().uri("/api/orders/my")
                .with(SecurityMockMvcRequestPostProcessors.user("bob@example.com"))
                .exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data.content[0].status").isEqualTo("PLACED");
    }

    @Test
    void getMyOrders_unauthenticated_returns403() {
        assertThat(mockMvc.get().uri("/api/orders/my").exchange())
                .hasStatus(403);
    }

    @Test
    void getOrderById_ownOrder_returns200() {
        when(orderService.getOrderById(1L, 1L)).thenReturn(mockOrder);

        MvcTestResult result = mockMvc.get().uri("/api/orders/1")
                .with(SecurityMockMvcRequestPostProcessors.user("bob@example.com"))
                .exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data.id").isEqualTo(1);
    }

    @Test
    void getOrderById_unauthenticated_returns403() {
        assertThat(mockMvc.get().uri("/api/orders/1").exchange())
                .hasStatus(403);
    }
}
