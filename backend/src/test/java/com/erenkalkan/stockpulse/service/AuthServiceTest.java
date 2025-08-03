package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidCredentialsException;
import com.erenkalkan.stockpulse.exception.UserAlreadyExistsException;
import com.erenkalkan.stockpulse.exception.UserNotFoundException;
import com.erenkalkan.stockpulse.model.dto.LoginRequestDTO;
import com.erenkalkan.stockpulse.model.dto.LoginResponseDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterRequestDTO;
import com.erenkalkan.stockpulse.model.dto.RegisterResponseDTO;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserService userService;

  @Mock
  private JwtService jwtService;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private UserDetails userDetails;

  @InjectMocks
  private AuthService authService;

  private RegisterRequestDTO testRegisterRequest;
  private LoginRequestDTO testLoginRequest;
  private User testUser;
  private User testOAuthUser;
  private final String TEST_EMAIL = "test@email.com";
  private final String TEST_FIRST_NAME = "Bruce";
  private final String TEST_PASSWORD = "password123";
  private final String TEST_ENCODED_PASSWORD = "encodedPassword123";
  private final String TEST_JWT_TOKEN = "jwt-token-123";

  @BeforeEach
  void setUp() {
    testRegisterRequest = RegisterRequestDTO.builder()
            .firstName(TEST_FIRST_NAME)
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

    testLoginRequest = LoginRequestDTO.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .build();

    testUser = User.builder()
            .id(1L)
            .firstName(TEST_FIRST_NAME)
            .email(TEST_EMAIL)
            .password(TEST_ENCODED_PASSWORD)
            .role(Role.REGULAR_USER)
            .isOAuthUser(false)
            .build();

    testOAuthUser = User.builder()
            .id(2L)
            .firstName(TEST_FIRST_NAME)
            .email(TEST_EMAIL)
            .role(Role.REGULAR_USER)
            .isOAuthUser(true)
            .build();
  }

  @Test
  void register_whenValidRequest_shouldCreateUserAndReturnResponse() {
    // Arrange
    when(userService.existsByEmail(TEST_EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);

    // Act
    RegisterResponseDTO result = authService.register(testRegisterRequest);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_FIRST_NAME, result.getFirstName());
    assertEquals(TEST_EMAIL, result.getEmail());

    verify(userService).existsByEmail(TEST_EMAIL);
    verify(passwordEncoder).encode(TEST_PASSWORD);
    verify(userService).saveUser(any(User.class));
  }

  @Test
  void register_whenUserAlreadyExists_shouldThrowUserAlreadyExistsException() {
    // Arrange
    when(userService.existsByEmail(TEST_EMAIL)).thenReturn(true);

    // Act & Assert
    UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> authService.register(testRegisterRequest)
    );

    assertTrue(exception.getMessage().contains("User with email '" + TEST_EMAIL + "' already exists"));
    verify(userService).existsByEmail(TEST_EMAIL);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userService, never()).saveUser(any(User.class));
  }

  @Test
  void register_shouldEncodePasswordBeforeSaving() {
    // Arrange
    when(userService.existsByEmail(TEST_EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);

    // Act
    authService.register(testRegisterRequest);

    // Assert
    verify(passwordEncoder).encode(TEST_PASSWORD);
    verify(userService).saveUser(argThat(user ->
            user.getPassword().equals(TEST_ENCODED_PASSWORD) &&
            user.getRole() == Role.REGULAR_USER &&
            user.getFirstName().equals(TEST_FIRST_NAME) &&
            user.getEmail().equals(TEST_EMAIL)
    ));
  }

  @Test
  void login_whenValidCredentials_shouldReturnLoginResponseWithToken() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(true);
    when(userService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
    when(jwtService.generateToken(userDetails)).thenReturn(TEST_JWT_TOKEN);

    // Act
    LoginResponseDTO result = authService.login(testLoginRequest);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_FIRST_NAME, result.getFirstName());
    assertEquals(TEST_EMAIL, result.getEmail());
    assertEquals(TEST_JWT_TOKEN, result.getToken());

    verify(userService).findByEmail(TEST_EMAIL);
    verify(passwordEncoder).matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD);
    verify(userService).loadUserByUsername(TEST_EMAIL);
    verify(jwtService).generateToken(userDetails);
  }

  @Test
  void login_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> authService.login(testLoginRequest)
    );

    assertTrue(exception.getMessage().contains("User with email '" + TEST_EMAIL + "' does not exist"));
    verify(userService).findByEmail(TEST_EMAIL);
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_whenUserIsOAuthUser_shouldThrowInvalidCredentialsException() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testOAuthUser));

    // Act & Assert
    InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(testLoginRequest)
    );

    assertEquals("Please use Google login for this account", exception.getMessage());
    verify(userService).findByEmail(TEST_EMAIL);
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_whenPasswordIsIncorrect_shouldThrowInvalidCredentialsException() {
    // Arrange
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(false);

    // Act & Assert
    InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(testLoginRequest)
    );

    assertEquals("Password is incorrect", exception.getMessage());
    verify(userService).findByEmail(TEST_EMAIL);
    verify(passwordEncoder).matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD);
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_whenUserIsNotOAuthUser_shouldProceedWithPasswordCheck() {
    // Arrange
    User regularUser = User.builder()
            .firstName(TEST_FIRST_NAME)
            .email(TEST_EMAIL)
            .password(TEST_ENCODED_PASSWORD)
            .role(Role.REGULAR_USER)
            .isOAuthUser(null)
            .build();

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(regularUser));
    when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(true);
    when(userService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
    when(jwtService.generateToken(userDetails)).thenReturn(TEST_JWT_TOKEN);

    // Act
    LoginResponseDTO result = authService.login(testLoginRequest);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_JWT_TOKEN, result.getToken());
    verify(passwordEncoder).matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD);
  }

  @Test
  void verifyAuthentication_whenValidToken_shouldReturnTrue() {
    // Arrange
    when(jwtService.extractTokenFromCookies(httpServletRequest)).thenReturn(TEST_JWT_TOKEN);
    when(jwtService.isTokenValid(TEST_JWT_TOKEN)).thenReturn(true);

    // Act
    boolean result = authService.verifyAuthentication(httpServletRequest);

    // Assert
    assertTrue(result);
    verify(jwtService).extractTokenFromCookies(httpServletRequest);
    verify(jwtService).isTokenValid(TEST_JWT_TOKEN);
  }

  @Test
  void verifyAuthentication_whenNoToken_shouldReturnFalse() {
    // Arrange
    when(jwtService.extractTokenFromCookies(httpServletRequest)).thenReturn(null);

    // Act
    boolean result = authService.verifyAuthentication(httpServletRequest);

    // Assert
    assertFalse(result);
    verify(jwtService).extractTokenFromCookies(httpServletRequest);
    verify(jwtService, never()).isTokenValid(anyString());
  }

  @Test
  void verifyAuthentication_whenInvalidToken_shouldReturnFalse() {
    // Arrange
    when(jwtService.extractTokenFromCookies(httpServletRequest)).thenReturn(TEST_JWT_TOKEN);
    when(jwtService.isTokenValid(TEST_JWT_TOKEN)).thenReturn(false);

    // Act
    boolean result = authService.verifyAuthentication(httpServletRequest);

    // Assert
    assertFalse(result);
    verify(jwtService).extractTokenFromCookies(httpServletRequest);
    verify(jwtService).isTokenValid(TEST_JWT_TOKEN);
  }

  @Test
  void verifyAuthentication_shouldCallJwtServiceMethodsInCorrectOrder() {
    // Arrange
    when(jwtService.extractTokenFromCookies(httpServletRequest)).thenReturn(TEST_JWT_TOKEN);
    when(jwtService.isTokenValid(TEST_JWT_TOKEN)).thenReturn(true);

    // Act
    authService.verifyAuthentication(httpServletRequest);

    // Assert
    verify(jwtService).extractTokenFromCookies(httpServletRequest);
    verify(jwtService).isTokenValid(TEST_JWT_TOKEN);
  }
}