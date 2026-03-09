package com.ecom.ecommerce.service;

import com.ecom.ecommerce.entity.*;
import com.ecom.ecommerce.exception.BadRequestException;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Order placeOrder(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items before placing an order.");
        }

        // Validate stock for all items first
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.getActive()) {
                throw new BadRequestException("Product '" + product.getName() + "' is no longer available");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + product.getName() +
                        "'. Available: " + product.getStock() + ", Requested: " + item.getQuantity());
            }
        }

        // Build order
        Order order = Order.builder()
                .user(cart.getUser())
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // Decrement stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order
        cartItemRepository.deleteByCartId(cart.getId());

        return savedOrder;
    }

    public Page<Order> getOrderHistory(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied to this order");
        }
        return order;
    }
}
