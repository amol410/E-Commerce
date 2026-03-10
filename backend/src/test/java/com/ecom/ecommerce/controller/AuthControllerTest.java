package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.LoginRequest;
import com.ecom.ecommerce.dto.RegisterRequest;
import com.ecom.ecommerce.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.ecom.ecommerce.config.TestMvcConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestMvcConfig.class)

class AuthControllerTest {

    @Autowired private MockMvcTester mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;

    @Test
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        Map<String, Object> response = Map.of(
                "token", "jwt-token",
                "userId", 1L,
                "name", "John Doe",
                "email", "john@example.com",
                "role", "ROLE_USER"
        );
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(201);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data.token").isEqualTo("jwt-token");
        assertThat(result).bodyJson().extractingPath("$.data.email").isEqualTo("john@example.com");
    }

    @Test
    void register_missingName_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");
        // name is missing

        assertThat(mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(400);
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        assertThat(mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(400);
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        Map<String, Object> response = Map.of(
                "token", "jwt-token",
                "userId", 1L,
                "name", "John Doe",
                "email", "john@example.com",
                "role", "ROLE_USER"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(200);
        assertThat(result).bodyJson().extractingPath("$.success").isEqualTo(true);
        assertThat(result).bodyJson().extractingPath("$.data.token").isEqualTo("jwt-token");
    }

    @Test
    void login_missingPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        // password missing

        assertThat(mockMvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange())
                .hasStatus(400);
    }
}
