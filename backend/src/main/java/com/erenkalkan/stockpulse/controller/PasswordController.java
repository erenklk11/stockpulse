package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.ResetPasswordRequestDTO;
import com.erenkalkan.stockpulse.service.VerificationTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/password")
public class PasswordController {

  private final VerificationTokenService tokenService;

  @PostMapping("/forgot")
  public ResponseEntity<Boolean> forgotPassword(@RequestBody String email) {
    return ResponseEntity.ok(tokenService.sendForgotPasswordEmail(email));
  }

  @PostMapping("/reset")
  public ResponseEntity<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
    return ResponseEntity.ok(tokenService.resetPassword(request));
  }

  @PostMapping("/verify")
  public ResponseEntity<Boolean> verifyToken(@RequestParam String token) {
    return ResponseEntity.ok(tokenService.verifyToken(token));
  }
}
