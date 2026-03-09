package com.ecom.ecommerce.service;

import com.ecom.ecommerce.dto.CartItemRequest;
import com.ecom.ecommerce.entity.Cart;
import com.ecom.ecommerce.entity.CartItem;
import com.ecom.ecommerce.entity.Product;
import com.ecom.ecommerce.exception.BadRequestException;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.CartItemRepository;
import com.ecom.ecommerce.repository.CartRepository;
import com.ecom.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
    }

    public Cart addItem(Long userId, CartItemRequest request) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (!product.getActive()) {
            throw new BadRequestException("Product is not available");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        // If item already in cart, increment quantity
        if (cartItemRepository.existsByCartAndProduct(cart, product)) {
            CartItem existing = cartItemRepository.findByCartAndProduct(cart, product).get();
            int newQty = existing.getQuantity() + request.getQuantity();
            if (product.getStock() < newQty) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
            }
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
        }

        return getCartByUserId(userId);
    }

    public Cart updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = getCartByUserId(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("This item does not belong to your cart");
        }
        if (item.getProduct().getStock() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + item.getProduct().getStock());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCartByUserId(userId);
    }

    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getCartByUserId(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("This item does not belong to your cart");
        }

        cartItemRepository.delete(item);
        return getCartByUserId(userId);
    }

    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }
}
