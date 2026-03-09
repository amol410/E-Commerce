package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.CartItemRequest;
import com.ecom.ecommerce.entity.Cart;
import com.ecom.ecommerce.entity.User;
import com.ecom.ecommerce.exception.ResourceNotFoundException;
import com.ecom.ecommerce.repository.UserRepository;
import com.ecom.ecommerce.response.ApiResponse;
import com.ecom.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<Cart>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Cart fetched", cartService.getCartByUserId(getUserId(userDetails))));
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<Cart>> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                                      @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartService.addItem(getUserId(userDetails), request)));
    }

    @PutMapping("/update/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<Cart>> updateItem(@AuthenticationPrincipal UserDetails userDetails,
                                                         @PathVariable Long itemId,
                                                         @RequestParam Integer quantity) {
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cartService.updateItemQuantity(getUserId(userDetails), itemId, quantity)));
    }

    @DeleteMapping("/remove/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<Cart>> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                                         @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed", cartService.removeItem(getUserId(userDetails), itemId)));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(getUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
