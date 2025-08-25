package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.ChangePasswordRequestDTO;
import com.erenkalkan.stockpulse.model.dto.ResetPasswordRequestDTO;
import com.erenkalkan.stockpulse.service.VerificationTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password")
public class PasswordController {

  private final VerificationTokenService tokenService;

  @PostMapping("/forgot")
  public ResponseEntity<Map<String, Boolean>> forgotPassword(@RequestBody String email) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("mailSent", tokenService.sendForgotPasswordEmail(email));
    return ResponseEntity.ok(response);
  }

  @PutMapping("/reset")
  public ResponseEntity<Map<String, Boolean>> changePassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("passwordReset", tokenService.resetPassword(request));
    return ResponseEntity.ok(response);
  }

  @PutMapping("/change")
  public ResponseEntity<Map<String, Boolean>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request, Authentication authentication) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("passwordChanged", tokenService.resetPassword(request, authentication));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/verify")
  public ResponseEntity<Map<String, Boolean>> verifyToken(@RequestParam String token) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("tokenVerified", tokenService.verifyToken(token));
    return ResponseEntity.ok(response);
  }
}
