package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.LoginRequestDTO;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterRequestDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterResponseDTO;
import com.erenkalkan.stockpulse.service.AuthService;
import com.erenkalkan.stockpulse.service.JwtService;
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
  private final JwtService jwtService;

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

  @GetMapping("/verify")
  public ResponseEntity<Map<String, Boolean>> verifyAuthentication(HttpServletRequest request) {
    String token = jwtService.extractTokenFromCookies(request);
    boolean isAuthenticated = token != null && jwtService.isTokenValid(token);

    Map<String, Boolean> response = new HashMap<>();
    response.put("authenticated", isAuthenticated);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    // Clear the JWT cookie
    Cookie cookie = new Cookie("auth-token", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0); // Delete the cookie
    response.addCookie(cookie);

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
}
