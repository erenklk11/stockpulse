package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.ResetPasswordRequestDTO;
import com.erenkalkan.stockpulse.service.VerificationTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PasswordController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    com.erenkalkan.stockpulse.security.JwtAuthFilter.class,
    com.erenkalkan.stockpulse.config.SecurityConfig.class
}))
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PasswordControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private VerificationTokenService tokenService;

  @Autowired
  private ObjectMapper objectMapper;

  // Tests for /forgot endpoint
  @Test
  void testForgotPassword_WithValidEmail_ShouldReturnTrue() throws Exception {
    // Arrange
    String email = "john@example.com";
    when(tokenService.sendForgotPasswordEmail(anyString())).thenReturn(true);

    // Act & Assert
    mvc.perform(post("/api/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(email)))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void testForgotPassword_WithInvalidEmail_ShouldReturnFalse() throws Exception {
    // Arrange
    String email = "nonexistent@example.com";
    when(tokenService.sendForgotPasswordEmail(anyString())).thenReturn(false);

    // Act & Assert
    mvc.perform(post("/api/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(email)))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void testForgotPassword_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
    // Arrange
    String email = "";
    when(tokenService.sendForgotPasswordEmail(any(String.class))).thenThrow(new InvalidInputException("Email cannot be null"));

    // Act & Assert
    mvc.perform(post("/api/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(email)))
        .andExpect(status().isBadRequest());
  }

  // Tests for /reset endpoint
  @Test
  void testResetPassword_WithValidRequest_ShouldReturnTrue() throws Exception {
    // Arrange
    ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
        .token("valid-reset-token")
        .newPassword("newPassword123")
        .build();

    when(tokenService.resetPassword(any(ResetPasswordRequestDTO.class))).thenReturn(true);

    // Act & Assert
    mvc.perform(post("/api/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void testResetPassword_WithInvalidToken_ShouldReturnFalse() throws Exception {
    // Arrange
    ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
        .token("invalid-reset-token")
        .newPassword("newPassword123")
        .build();

    when(tokenService.resetPassword(any(ResetPasswordRequestDTO.class))).thenReturn(false);

    // Act & Assert
    mvc.perform(post("/api/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void testResetPassword_WithNullToken_ShouldReturnBadRequest() throws Exception {
    // Arrange
    ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
        .token(null) // Invalid - null token
        .newPassword("newPassword123")
        .build();

    // Act & Assert
    mvc.perform(post("/api/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testResetPassword_WithNullPassword_ShouldReturnBadRequest() throws Exception {
    // Arrange
    ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
        .token("valid-reset-token")
        .newPassword(null) // Invalid - null password
        .build();

    // Act & Assert
    mvc.perform(post("/api/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testResetPassword_WithEmptyToken_ShouldReturnBadRequest() throws Exception {
    // Arrange
    ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
        .token("") // Invalid - empty token
        .newPassword("newPassword123")
        .build();

    // Act & Assert
    mvc.perform(post("/api/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testResetPassword_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
    // Arrange
    ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
        .token("valid-reset-token")
        .newPassword("") // Invalid - empty password
        .build();

    // Act & Assert
    mvc.perform(post("/api/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // Tests for /verify endpoint
  @Test
  void testVerifyToken_WithValidToken_ShouldReturnTrue() throws Exception {
    // Arrange
    String token = "valid-verification-token";
    when(tokenService.verifyToken(anyString())).thenReturn(true);

    // Act & Assert
    mvc.perform(post("/api/password/verify")
            .param("token", token))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void testVerifyToken_WithInvalidToken_ShouldReturnFalse() throws Exception {
    // Arrange
    String token = "invalid-verification-token";
    when(tokenService.verifyToken(anyString())).thenReturn(false);

    // Act & Assert
    mvc.perform(post("/api/password/verify")
            .param("token", token))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void testVerifyToken_WithExpiredToken_ShouldReturnFalse() throws Exception {
    // Arrange
    String token = "expired-verification-token";
    when(tokenService.verifyToken(anyString())).thenReturn(false);

    // Act & Assert
    mvc.perform(post("/api/password/verify")
            .param("token", token))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void testVerifyToken_WithEmptyToken_ShouldReturnBadRequest() throws Exception {
    // Arrange
    when(tokenService.verifyToken("")).thenThrow(new com.erenkalkan.stockpulse.exception.InvalidJwtTokenException("Authentication token is required"));

    // Act & Assert
    mvc.perform(post("/api/password/verify")
            .param("token", ""))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testVerifyToken_WithoutToken_ShouldReturnBadRequest() throws Exception {
    // Act & Assert
    mvc.perform(post("/api/password/verify"))
        .andExpect(status().isBadRequest());
  }
}