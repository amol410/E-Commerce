package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.entity.Order;
import com.ecom.ecommerce.entity.User;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.UserRepository;
import com.ecom.ecommerce.response.ApiResponse;
import com.ecom.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @PostMapping("/place")
    @Operation(summary = "Place an order from cart")
    public ResponseEntity<ApiResponse<Order>> placeOrder(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", orderService.placeOrder(getUserId(userDetails))));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's order history")
    public ResponseEntity<ApiResponse<Page<Order>>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched",
                orderService.getOrderHistory(getUserId(userDetails), pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched",
                orderService.getOrderById(id, getUserId(userDetails))));
    }
}
