package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.*;
import com.erenkalkan.stockpulse.model.dto.ResetPasswordRequestDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import com.erenkalkan.stockpulse.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserService userService;

  @Mock
  private JwtService jwtService;

  @Mock
  private EmailService emailService;

  @Mock
  private VerificationTokenRepository verificationTokenRepository;

  @InjectMocks
  private VerificationTokenService verificationTokenService;

  private User testUser;
  private VerificationToken testVerificationToken;
  private VerificationToken expiredVerificationToken;
  private VerificationToken usedVerificationToken;
  private ResetPasswordRequestDTO testResetPasswordRequest;
  private UserDetails testUserDetails;

  private final String TEST_EMAIL = "test@email.com";
  private final String TEST_TOKEN = "test-token-123";
  private final String TEST_JWT_TOKEN = "jwt-token-456";
  private final String TEST_NEW_PASSWORD = "newPassword123";
  private final Long TEST_EXPIRATION_MS = 3600000L; // 1 hour

  @BeforeEach
  void setUp() {
    testUser = User.builder()
            .firstName("John")
            .email(TEST_EMAIL)
            .password("oldPassword")
            .role(Role.REGULAR_USER)
            .build();

    testVerificationToken = VerificationToken.builder()
            .id(1L)
            .token(TEST_TOKEN)
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now())
            .expiresOn(LocalDateTime.now().plusHours(1))
            .used(false)
            .expirationMs(TEST_EXPIRATION_MS)
            .build();

    testResetPasswordRequest = ResetPasswordRequestDTO.builder()
            .token(TEST_TOKEN)
            .newPassword(TEST_NEW_PASSWORD)
            .build();

    testUserDetails = mock(UserDetails.class);

    ReflectionTestUtils.setField(verificationTokenService, "tokenExpirationMs", TEST_EXPIRATION_MS);
  }

  // ========== SAVE METHOD TESTS ==========

  @Test
  void save_whenValidToken_shouldReturnSavedToken() {
    // Arrange
    when(verificationTokenRepository.save(testVerificationToken)).thenReturn(testVerificationToken);

    // Act
    VerificationToken result = verificationTokenService.save(testVerificationToken);

    // Assert
    assertNotNull(result);
    assertEquals(testVerificationToken, result);
    verify(verificationTokenRepository).save(testVerificationToken);
  }

  @Test
  void save_whenRepositoryThrowsException_shouldThrowDatabaseOperationException() {
    // Arrange
    RuntimeException repositoryException = new RuntimeException("Database connection failed");
    when(verificationTokenRepository.save(any(VerificationToken.class))).thenThrow(repositoryException);

    // Act & Assert
    DatabaseOperationException exception = assertThrows(
            DatabaseOperationException.class,
            () -> verificationTokenService.save(testVerificationToken)
    );

    assertTrue(exception.getMessage().contains("Failed to save verification token to database"));
    assertEquals(repositoryException, exception.getCause());
    verify(verificationTokenRepository).save(testVerificationToken);
  }

  // ========== SEND FORGOT PASSWORD EMAIL TESTS ==========

  @Test
  void sendForgotPasswordEmail_whenValidEmail_shouldSendEmailAndSaveToken() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(userService.loadUserByUsername(TEST_EMAIL)).thenReturn(testUserDetails);
    when(jwtService.generateToken(testUserDetails)).thenReturn(TEST_JWT_TOKEN);
    when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(testVerificationToken);

    ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);

    // Act
    boolean result = verificationTokenService.sendForgotPasswordEmail(TEST_EMAIL);

    // Assert
    assertTrue(result);
    verify(userService).findByEmail(TEST_EMAIL);
    verify(userService).loadUserByUsername(TEST_EMAIL);
    verify(jwtService).generateToken(testUserDetails);
    verify(emailService).sendVerificationEmail(eq(TEST_EMAIL), tokenCaptor.capture());
    verify(verificationTokenRepository).save(tokenCaptor.capture());

    VerificationToken capturedToken = tokenCaptor.getValue();
    assertEquals(TokenType.PASSWORD_RESET, capturedToken.getTokenType());
    assertEquals(testUser, capturedToken.getUser());
    assertEquals(TEST_JWT_TOKEN, capturedToken.getToken());
    assertEquals(TEST_EXPIRATION_MS, capturedToken.getExpirationMs());
  }

  @Test
  void sendForgotPasswordEmail_whenEmailIsNull_shouldThrowInvalidInputException() {
    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> verificationTokenService.sendForgotPasswordEmail(null)
    );

    assertEquals("Email cannot be null", exception.getMessage());
    verifyNoInteractions(userService, jwtService, emailService, verificationTokenRepository);
  }

  @Test
  void sendForgotPasswordEmail_whenUserNotFound_shouldThrowUserNotFoundException() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> verificationTokenService.sendForgotPasswordEmail(TEST_EMAIL)
    );

    assertEquals("User with email " + TEST_EMAIL + " does not exist", exception.getMessage());
    verify(userService).findByEmail(TEST_EMAIL);
    verifyNoInteractions(jwtService, emailService, verificationTokenRepository);
  }

  @Test
  void sendForgotPasswordEmail_shouldCreateTokenWithCorrectProperties() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(userService.loadUserByUsername(TEST_EMAIL)).thenReturn(testUserDetails);
    when(jwtService.generateToken(testUserDetails)).thenReturn(TEST_JWT_TOKEN);
    when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(testVerificationToken);

    ArgumentCaptor<VerificationToken> emailTokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
    ArgumentCaptor<VerificationToken> saveTokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);

    // Act
    verificationTokenService.sendForgotPasswordEmail(TEST_EMAIL);

    // Assert
    verify(emailService).sendVerificationEmail(eq(TEST_EMAIL), emailTokenCaptor.capture());
    verify(verificationTokenRepository).save(saveTokenCaptor.capture());

    VerificationToken emailToken = emailTokenCaptor.getValue();
    VerificationToken savedToken = saveTokenCaptor.getValue();

    // Both captures should be the same token object
    assertEquals(emailToken, savedToken);
    assertEquals(TokenType.PASSWORD_RESET, emailToken.getTokenType());
    assertEquals(testUser, emailToken.getUser());
    assertEquals(TEST_JWT_TOKEN, emailToken.getToken());
    assertEquals(TEST_EXPIRATION_MS, emailToken.getExpirationMs());
  }

  // ========== RESET PASSWORD TESTS ==========

  @Test
  void resetPassword_whenValidRequest_shouldResetPasswordAndMarkTokenAsUsed() {
    // Arrange
    String testEncodedPassword = "encodedPassword123";
    when(verificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(testVerificationToken));
    when(passwordEncoder.encode(TEST_NEW_PASSWORD)).thenReturn(testEncodedPassword);
    when(verificationTokenRepository.save(testVerificationToken)).thenReturn(testVerificationToken);

    // Act
    boolean result = verificationTokenService.resetPassword(testResetPasswordRequest);

    // Assert
    assertTrue(result);
    assertEquals(testEncodedPassword, testUser.getPassword());
    assertTrue(testVerificationToken.getUsed());
    verify(verificationTokenRepository, times(2)).findByToken(TEST_TOKEN);
    verify(passwordEncoder).encode(TEST_NEW_PASSWORD);
    verify(userService).saveUser(testUser);
    verify(verificationTokenRepository).save(testVerificationToken);
  }

  @Test
  void resetPassword_whenTokenNotFound_shouldThrowInvalidJwtTokenException() {
    // Arrange
    when(verificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.empty());

    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> verificationTokenService.resetPassword(testResetPasswordRequest)
    );

    assertEquals("Authentication token does not exist", exception.getMessage());
    verify(verificationTokenRepository).findByToken(TEST_TOKEN);
    verifyNoInteractions(passwordEncoder, userService);
  }

  @Test
  void resetPassword_whenTokenIsExpired_shouldThrowJwtTokenExpiredException() {
    // Arrange
    expiredVerificationToken = VerificationToken.builder()
            .id(2L)
            .token("expired-token")
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now().minusHours(2))
            .expiresOn(LocalDateTime.now().minusHours(1))
            .used(false)
            .expirationMs(TEST_EXPIRATION_MS)
            .build();

    when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredVerificationToken));

    ResetPasswordRequestDTO expiredTokenRequest = ResetPasswordRequestDTO.builder()
            .token("expired-token")
            .newPassword(TEST_NEW_PASSWORD)
            .build();

    // Act & Assert
    JwtTokenExpiredException exception = assertThrows(
            JwtTokenExpiredException.class,
            () -> verificationTokenService.resetPassword(expiredTokenRequest)
    );

    assertEquals("Authentication token is expired", exception.getMessage());
    verify(verificationTokenRepository).findByToken("expired-token");
    verifyNoInteractions(passwordEncoder, userService);
  }

  @Test
  void resetPassword_whenTokenIsUsed_shouldThrowInvalidJwtTokenException() {
    // Arrange
    usedVerificationToken = VerificationToken.builder()
            .id(3L)
            .token("used-token")
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now())
            .expiresOn(LocalDateTime.now().plusHours(1))
            .used(true)
            .expirationMs(TEST_EXPIRATION_MS)
            .build();

    when(verificationTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedVerificationToken));

    ResetPasswordRequestDTO usedTokenRequest = ResetPasswordRequestDTO.builder()
            .token("used-token")
            .newPassword(TEST_NEW_PASSWORD)
            .build();

    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> verificationTokenService.resetPassword(usedTokenRequest)
    );

    assertEquals("Authentication token is not valid", exception.getMessage());
    verify(verificationTokenRepository).findByToken("used-token");
    verifyNoInteractions(passwordEncoder, userService);
  }

  // ========== VERIFY TOKEN TESTS ==========

  @Test
  void verifyToken_whenValidToken_shouldReturnTrue() {
    // Arrange
    when(verificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(testVerificationToken));

    // Act
    boolean result = verificationTokenService.verifyToken(TEST_TOKEN);

    // Assert
    assertTrue(result);
    verify(verificationTokenRepository).findByToken(TEST_TOKEN);
  }

  @Test
  void verifyToken_whenTokenIsNull_shouldThrowInvalidJwtTokenException() {
    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> verificationTokenService.verifyToken(null)
    );

    assertEquals("Authentication token is required", exception.getMessage());
    verifyNoInteractions(verificationTokenRepository);
  }

  @Test
  void verifyToken_whenTokenNotFound_shouldThrowInvalidJwtTokenException() {
    // Arrange
    when(verificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.empty());

    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> verificationTokenService.verifyToken(TEST_TOKEN)
    );

    assertEquals("Authentication token does not exist", exception.getMessage());
    verify(verificationTokenRepository).findByToken(TEST_TOKEN);
  }

  @Test
  void verifyToken_whenTokenIsExpired_shouldThrowJwtTokenExpiredException() {
    // Arrange
    expiredVerificationToken = VerificationToken.builder()
            .id(2L)
            .token("expired-token")
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now().minusHours(2))
            .expiresOn(LocalDateTime.now().minusHours(1))
            .used(false)
            .expirationMs(TEST_EXPIRATION_MS)
            .build();

    when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredVerificationToken));

    // Act & Assert
    JwtTokenExpiredException exception = assertThrows(
            JwtTokenExpiredException.class,
            () -> verificationTokenService.verifyToken("expired-token")
    );

    assertEquals("Authentication token is expired", exception.getMessage());
    verify(verificationTokenRepository).findByToken("expired-token");
  }

  @Test
  void verifyToken_whenTokenIsUsed_shouldThrowInvalidJwtTokenException() {
    // Arrange
    usedVerificationToken = VerificationToken.builder()
            .id(3L)
            .token("used-token")
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now())
            .expiresOn(LocalDateTime.now().plusHours(1))
            .used(true)
            .expirationMs(TEST_EXPIRATION_MS)
            .build();

    when(verificationTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedVerificationToken));

    // Act & Assert
    InvalidJwtTokenException exception = assertThrows(
            InvalidJwtTokenException.class,
            () -> verificationTokenService.verifyToken("used-token")
    );

    assertEquals("Authentication token is not valid", exception.getMessage());
    verify(verificationTokenRepository).findByToken("used-token");
  }

  @Test
  void verifyToken_shouldCallRepositoryExactlyOnce() {
    // Arrange
    when(verificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(testVerificationToken));

    // Act
    verificationTokenService.verifyToken(TEST_TOKEN);

    // Assert
    verify(verificationTokenRepository, times(1)).findByToken(TEST_TOKEN);
  }
}