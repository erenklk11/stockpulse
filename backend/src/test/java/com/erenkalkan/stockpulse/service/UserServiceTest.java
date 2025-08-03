package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.DatabaseOperationException;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private final String TEST_EMAIL = "test@email.com";
  private final String TEST_PASSWORD = "encodedPassword123";

  @BeforeEach
  void setUp() {
    testUser = User.builder()
            .firstName("Bruce")
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .role(Role.REGULAR_USER)
            .build();
  }

  @Test
  void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
    // Arrange
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    // Act
    UserDetails result = userService.loadUserByUsername(TEST_EMAIL);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_EMAIL, result.getUsername());
    assertEquals(TEST_PASSWORD, result.getPassword());
    assertTrue(result.getAuthorities().containsAll(testUser.getAuthorities()));
    verify(userRepository).findByEmail(TEST_EMAIL);
  }

  @Test
  void loadUserByUsername_whenUserDoesNotExist_shouldThrowUsernameNotFoundException() {
    // Arrange
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername(TEST_EMAIL)
    );

    assertTrue(exception.getMessage().contains(TEST_EMAIL));
    verify(userRepository).findByEmail(TEST_EMAIL);
  }

  @Test
  void saveUser_whenSuccessful_shouldReturnSavedUser() {
    // Arrange
    when(userRepository.save(testUser)).thenReturn(testUser);

    // Act
    User result = userService.saveUser(testUser);

    // Assert
    assertNotNull(result);
    assertEquals(testUser, result);
    verify(userRepository).save(testUser);
  }

  @Test
  void saveUser_whenRepositoryThrowsException_shouldThrowDatabaseOperationException() {
    // Arrange
    RuntimeException repositoryException = new RuntimeException("Database connection failed");
    when(userRepository.save(any(User.class))).thenThrow(repositoryException);

    // Act & Assert
    DatabaseOperationException exception = assertThrows(
            DatabaseOperationException.class,
            () -> userService.saveUser(testUser)
    );

    assertTrue(exception.getMessage().contains("Failed to save user to database"));
    assertEquals(repositoryException, exception.getCause());
    verify(userRepository).save(testUser);
  }
}