package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.GoogleOAuthRequestDTO;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.service.GoogleOAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;

    @Value("${app.jwt.cookie.name:auth-token}")
    private String cookieName;

    @Value("${app.jwt.cookie.max-age:86400}")
    private int cookieMaxAge;

    @Value("${app.environment:dev}")
    private String environment;

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> authenticateWithGoogle(
            @Valid @RequestBody GoogleOAuthRequestDTO request,
            HttpServletResponse response) {

        try {
            log.info("Processing Google OAuth authentication for authorization code");

            LoginResponseDTO loginResponse = googleOAuthService.authenticateWithGoogle(request.getCode(), request.getCodeVerifier());
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
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(!"dev".equals(environment)); // HTTPS only in production
        cookie.setPath("/");
        cookie.setMaxAge(cookieMaxAge);
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }
}
