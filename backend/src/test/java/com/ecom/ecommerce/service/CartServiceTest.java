package com.ecom.ecommerce.service;

import com.ecom.ecommerce.dto.CartItemRequest;
import com.ecom.ecommerce.entity.*;
import com.ecom.ecommerce.exception.BadRequestException;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.CartItemRepository;
import com.ecom.ecommerce.repository.CartRepository;
import com.ecom.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").email("test@test.com").build();
        cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        product = Product.builder().id(1L).name("Product A").price(new BigDecimal("200.00")).stock(5).active(true).build();
    }

    @Test
    void addItem_validRequest_addsItemToCart() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.existsByCartAndProduct(cart, product)).thenReturn(false);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());

        cartService.addItem(1L, request);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_insufficientStock_throwsBadRequest() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(10); // more than stock (5)

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void getCartByUserId_notFound_throwsException() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCartByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
