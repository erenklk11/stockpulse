package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.exception.InvalidJwtTokenException;
import com.erenkalkan.stockpulse.exception.JwtConfigurationException;
import com.erenkalkan.stockpulse.exception.JwtTokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  @Mock
  private UserDetails userDetails;

  @Mock
  private HttpServletRequest httpServletRequest;

  @InjectMocks
  private JwtService jwtService;

  private final String TEST_SECRET_KEY = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdGluZy10aGF0LWlzLWxvbmctZW5vdWdo";
  private final String TEST_EXPIRATION_MS = "3600000"; // 1 hour
  private final String TEST_ISSUER = "stockpulse-test";
  private final String TEST_USERNAME = "test@email.com";
  private String validToken;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
    ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS);
    ReflectionTestUtils.setField(jwtService, "issuer", TEST_ISSUER);

    when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

    // Generate a valid token for tests
    validToken = jwtService.generateToken(userDetails);
  }

  @Test
  void extractUsername_whenValidToken_shouldReturnUsername() {
    // Act
    String username = jwtService.extractUsername(validToken);

    // Assert
    assertEquals(TEST_USERNAME, username);
  }

  @Test
  void extractUsername_whenExpiredToken_shouldThrowJwtTokenExpiredException() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", "-1000"); // expired
    String expiredToken = jwtService.generateToken(userDetails);
    ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS); // reset

    // Act & Assert
    assertThrows(JwtTokenExpiredException.class, () -> jwtService.extractUsername(expiredToken));
  }

  @Test
  void extractUsername_whenInvalidToken_shouldThrowInvalidJwtTokenException() {
    // Arrange
    String invalidToken = "invalid.jwt.token";

    // Act & Assert
    assertThrows(InvalidJwtTokenException.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  void extractClaim_whenValidToken_shouldReturnClaim() {
    // Act
    String subject = jwtService.extractClaim(validToken, Claims::getSubject);
    Date expiration = jwtService.extractClaim(validToken, Claims::getExpiration);

    // Assert
    assertEquals(TEST_USERNAME, subject);
    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void extractAllClaims_whenValidToken_shouldReturnAllClaims() {
    // Act
    Claims claims = jwtService.extractAllClaims(validToken);

    // Assert
    assertNotNull(claims);
    assertEquals(TEST_USERNAME, claims.getSubject());
    assertEquals(TEST_ISSUER, claims.getIssuer());
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
  }

  @Test
  void extractAllClaims_whenExpiredToken_shouldThrowJwtTokenExpiredException() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", "-1000"); // expired
    String expiredToken = jwtService.generateToken(userDetails);
    ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS); // reset

    // Act & Assert
    assertThrows(JwtTokenExpiredException.class, () -> jwtService.extractAllClaims(expiredToken));
  }

  @Test
  void extractAllClaims_whenInvalidToken_shouldThrowInvalidJwtTokenException() {
    // Arrange
    String invalidToken = "invalid.jwt.token";

    // Act & Assert
    assertThrows(InvalidJwtTokenException.class, () -> jwtService.extractAllClaims(invalidToken));
  }

  @Test
  void generateToken_whenValidUserDetails_shouldReturnValidToken() {
    // Act
    String token = jwtService.generateToken(userDetails);

    // Assert
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(jwtService.isTokenValid(token, userDetails));
    assertEquals(TEST_USERNAME, jwtService.extractUsername(token));
  }

  @Test
  void generateToken_withExtraClaims_shouldIncludeExtraClaimsInToken() {
    // Arrange
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "ADMIN");
    extraClaims.put("userId", 123);

    // Act
    String token = jwtService.generateToken(extraClaims, userDetails);

    // Assert
    assertNotNull(token);
    Claims claims = jwtService.extractAllClaims(token);
    assertEquals("ADMIN", claims.get("role"));
    assertEquals(123, claims.get("userId"));
    assertEquals(TEST_USERNAME, claims.getSubject());
  }

  @Test
  void generateToken_whenSecretKeyIsNull_shouldThrowJwtConfigurationException() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "secretKey", null);

    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> jwtService.generateToken(userDetails)
    );
    assertTrue(exception.getMessage().contains("Failed to generate JWT token"));
    assertTrue(exception.getCause() instanceof JwtConfigurationException);
  }

  @Test
  void generateToken_whenExpirationMsIsNull_shouldThrowJwtConfigurationException() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", null);

    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> jwtService.generateToken(userDetails)
    );
    assertTrue(exception.getMessage().contains("Failed to generate JWT token"));
    assertTrue(exception.getCause() instanceof JwtConfigurationException);
  }

  @Test
  void generateToken_whenExpirationMsIsInvalid_shouldThrowInvalidInputException() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", "invalid-number");

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> jwtService.generateToken(userDetails)
    );
    assertTrue(exception.getMessage().contains("Invalid JWT expiration value"));
  }

  @Test
  void isTokenValid_whenValidTokenAndMatchingUser_shouldReturnTrue() {
    // Act
    boolean isValid = jwtService.isTokenValid(validToken, userDetails);

    // Assert
    assertTrue(isValid);
  }

  @Test
  void isTokenValid_whenValidTokenButDifferentUser_shouldReturnFalse() {
    // Arrange
    UserDetails differentUser = mock(UserDetails.class);
    when(differentUser.getUsername()).thenReturn("different@email.com");

    // Act
    boolean isValid = jwtService.isTokenValid(validToken, differentUser);

    // Assert
    assertFalse(isValid);
  }

  @Test
  void isTokenValid_whenExpiredToken_shouldReturnFalse() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", "-1000"); // expired
    String expiredToken = jwtService.generateToken(userDetails);
    ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS); // reset

    // Act & Assert
    assertThrows(JwtTokenExpiredException.class, () -> jwtService.isTokenValid(expiredToken, userDetails));
  }

  @Test
  void isTokenValid_withTokenOnly_whenValidToken_shouldReturnTrue() {
    // Act
    boolean isValid = jwtService.isTokenValid(validToken);

    // Assert
    assertTrue(isValid);
  }

  @Test
  void isTokenValid_withTokenOnly_whenExpiredToken_shouldReturnFalse() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", "-1000"); // expired
    String expiredToken = jwtService.generateToken(userDetails);
    ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS); // reset

    // Act & Assert
    assertThrows(JwtTokenExpiredException.class, () -> jwtService.isTokenValid(expiredToken));
  }

  @Test
  void isTokenExpired_whenValidToken_shouldReturnFalse() {
    // Act
    boolean isExpired = jwtService.isTokenExpired(validToken);

    // Assert
    assertFalse(isExpired);
  }

  @Test
  void isTokenExpired_whenExpiredToken_shouldReturnTrue() {
    // Arrange
    ReflectionTestUtils.setField(jwtService, "expirationMs", "-1000"); // expired
    String expiredToken = jwtService.generateToken(userDetails);
    ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS); // reset

    // Act & Assert
    assertThrows(JwtTokenExpiredException.class, () -> jwtService.isTokenExpired(expiredToken));
  }

  @Test
  void extractTokenFromCookies_whenAuthTokenCookieExists_shouldReturnToken() {
    // Arrange
    String expectedToken = "test-auth-token";
    Cookie authCookie = new Cookie("auth-token", expectedToken);
    Cookie[] cookies = {authCookie};
    when(httpServletRequest.getCookies()).thenReturn(cookies);

    // Act
    String extractedToken = jwtService.extractTokenFromCookies(httpServletRequest);

    // Assert
    assertEquals(expectedToken, extractedToken);
  }

  @Test
  void extractTokenFromCookies_whenAuthTokenCookieDoesNotExist_shouldReturnNull() {
    // Arrange
    Cookie otherCookie = new Cookie("other-cookie", "other-value");
    Cookie[] cookies = {otherCookie};
    when(httpServletRequest.getCookies()).thenReturn(cookies);

    // Act
    String extractedToken = jwtService.extractTokenFromCookies(httpServletRequest);

    // Assert
    assertNull(extractedToken);
  }

  @Test
  void extractTokenFromCookies_whenNoCookiesExist_shouldReturnNull() {
    // Arrange
    when(httpServletRequest.getCookies()).thenReturn(null);

    // Act
    String extractedToken = jwtService.extractTokenFromCookies(httpServletRequest);

    // Assert
    assertNull(extractedToken);
  }

  @Test
  void extractTokenFromCookies_whenMultipleCookiesIncludingAuthToken_shouldReturnAuthToken() {
    // Arrange
    String expectedToken = "test-auth-token";
    Cookie sessionCookie = new Cookie("session-id", "session-123");
    Cookie authCookie = new Cookie("auth-token", expectedToken);
    Cookie preferenceCookie = new Cookie("user-pref", "dark-mode");
    Cookie[] cookies = {sessionCookie, authCookie, preferenceCookie};
    when(httpServletRequest.getCookies()).thenReturn(cookies);

    // Act
    String extractedToken = jwtService.extractTokenFromCookies(httpServletRequest);

    // Assert
    assertEquals(expectedToken, extractedToken);
  }

  @Test
  void generateToken_shouldSetCorrectIssuer() {
    // Act
    String token = jwtService.generateToken(userDetails);

    // Assert
    Claims claims = jwtService.extractAllClaims(token);
    assertEquals(TEST_ISSUER, claims.getIssuer());
  }

}