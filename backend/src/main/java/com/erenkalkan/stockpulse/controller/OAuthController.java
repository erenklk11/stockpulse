package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.GoogleOAuthRequestDTO;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;

    @Value("${app.jwt.expiration}")
    private int cookieMaxAge;


    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> authenticateWithGoogle(
            @Valid @RequestBody GoogleOAuthRequestDTO request,
            HttpServletResponse response) {

        try {
            log.info("Processing Google OAuth authentication for authorization code");

            LoginResponseDTO loginResponse = googleOAuthService.authenticateWithGoogle(request.getCode(), request.getCodeVerifier());
            log.info("THE USER: {}", loginResponse);
            setJwtCookie(response, loginResponse.getToken());
            loginResponse.setToken(null);

            log.info("Google OAuth authentication successful for user: {}", loginResponse.getEmail());

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.error("Google OAuth authentication failed", e);
            throw new OAuth2AuthenticationException("Google OAuth authentication failed");
        }
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("auth-token", token)
                .httpOnly(true)
                .secure(false) // true in prod
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
    }
}
