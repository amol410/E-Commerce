package com.ecom.ecommerce.service;

import com.ecom.ecommerce.dto.RegisterRequest;
import com.ecom.ecommerce.entity.User;
import com.ecom.ecommerce.exception.BadRequestException;
import com.ecom.ecommerce.repository.CartRepository;
import com.ecom.ecommerce.repository.UserRepository;
import com.ecom.ecommerce.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("test@test.com");
        request.setPassword("password123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void register_newEmail_savesUserAndCreatesCart() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Amol");
        request.setEmail("amol@test.com");
        request.setPassword("password123");

        User savedUser = User.builder().id(1L).name("Amol").email("amol@test.com").password("encoded").build();

        when(userRepository.existsByEmail("amol@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("amol@test.com"))
                .thenReturn(org.springframework.security.core.userdetails.User.builder()
                        .username("amol@test.com").password("encoded").roles("USER").build());
        when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");

        var result = authService.register(request);

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo("mock-jwt-token");
        verify(cartRepository).save(any());
    }
}
