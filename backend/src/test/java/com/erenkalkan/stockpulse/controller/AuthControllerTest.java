package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.*;
import com.erenkalkan.stockpulse.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    com.erenkalkan.stockpulse.security.JwtAuthFilter.class,
    com.erenkalkan.stockpulse.config.SecurityConfig.class
}))
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private AuthService authService;

  @Autowired
  private ObjectMapper objectMapper;


  @Test
  void testRegister_ShouldReturnCreatedWithUserData() throws Exception {
    // Arrange
    RegisterRequestDTO request = RegisterRequestDTO.builder()
        .firstName("John")
        .email("john@example.com")
        .password("password123")
        .build();

    RegisterResponseDTO response = RegisterResponseDTO.builder()
        .firstName("John")
        .email("john@example.com")
        .build();

    when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

    // Act & Assert
    mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  void testRegister_WithInvalidData_ShouldReturnBadRequest() throws Exception {
    // Arrange
    RegisterRequestDTO request = RegisterRequestDTO.builder()
        .firstName(null) // Invalid - null firstName
        .email("john@example.com")
        .password("password123")
        .build();

    // Act & Assert
    mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testLogin_ShouldReturnOkWithUserDataAndSetCookie() throws Exception {
    // Arrange
    LoginRequestDTO request = LoginRequestDTO.builder()
        .email("john@example.com")
        .password("password123")
        .build();

    LoginResponseDTO response = LoginResponseDTO.builder()
        .firstName("John")
        .email("john@example.com")
        .token("jwt-token-123")
        .build();

    when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

    // Act & Assert
    mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.email").value("john@example.com"))
        .andExpect(jsonPath("$.token").doesNotExist()) // Token should be null in response
        .andExpect(cookie().value("auth-token", "jwt-token-123"))
        .andExpect(cookie().httpOnly("auth-token", true))
        .andExpect(cookie().secure("auth-token", true))
        .andExpect(cookie().path("auth-token", "/"))
        .andExpect(cookie().maxAge("auth-token", 86400));
  }

  @Test
  void testLogin_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {
    // Arrange
    LoginRequestDTO request = LoginRequestDTO.builder()
        .email(null) // Invalid - null email
        .password("password123")
        .build();

    // Act & Assert
    mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testLogin_ShouldNotReturnTokenInResponseBody() throws Exception {
    // Arrange
    LoginRequestDTO request = LoginRequestDTO.builder()
            .email("john@example.com")
            .password("password123")
            .build();

    LoginResponseDTO response = LoginResponseDTO.builder()
            .firstName("John")
            .email("john@example.com")
            .token("jwt-token-123")
            .build();

    when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

    // Act & Assert
    mvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").doesNotExist()); // Ensure token is not in response body
  }

  @Test
  void testVerifyAuthentication_WithValidToken_ShouldReturnTrue() throws Exception {
    // Arrange
    when(authService.verifyAuthentication(any())).thenReturn(true);

    Cookie authCookie = new Cookie("auth-token", "valid-jwt-token");

    // Act & Assert
    mvc.perform(post("/api/auth/verify")
            .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void testVerifyAuthentication_WithInvalidToken_ShouldReturnFalse() throws Exception {
    // Arrange
    when(authService.verifyAuthentication(any())).thenReturn(false);

    Cookie authCookie = new Cookie("auth-token", "invalid-jwt-token");

    // Act & Assert
    mvc.perform(post("/api/auth/verify")
            .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void testVerifyAuthentication_WithoutToken_ShouldReturnFalse() throws Exception {
    // Arrange
    when(authService.verifyAuthentication(any())).thenReturn(false);

    // Act & Assert
    mvc.perform(post("/api/auth/verify"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void testLogout_ShouldReturnOkWithMessageAndClearCookie() throws Exception {
    // Arrange
    // No specific arrangement needed for logout

    // Act & Assert
    mvc.perform(post("/api/auth/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Logged out successfully"))
        .andExpect(cookie().value("auth-token", ""))
        .andExpect(cookie().httpOnly("auth-token", true))
        .andExpect(cookie().secure("auth-token", true))
        .andExpect(cookie().path("auth-token", "/"))
        .andExpect(cookie().maxAge("auth-token", 0));
  }
}