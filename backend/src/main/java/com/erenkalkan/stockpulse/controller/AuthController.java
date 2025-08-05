package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.*;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
    RegisterResponseDTO response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {
    LoginResponseDTO loginResponse = authService.login(request);
    setJwtCookie(response, loginResponse.getToken());
    loginResponse.setToken(null);

    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/verify")
  public ResponseEntity<Boolean> verifyAuthentication(HttpServletRequest request) {
    return ResponseEntity.ok(authService.verifyAuthentication(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    response.addCookie(clearCookie());

    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("message", "Logged out successfully");

    return ResponseEntity.ok(responseBody);
  }


  private void setJwtCookie(HttpServletResponse response, String token) {
    Cookie cookie = new Cookie("auth-token", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(86400);
    response.addCookie(cookie);
  }

  private Cookie clearCookie() {
    Cookie cookie = new Cookie("auth-token", "");
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }
}
