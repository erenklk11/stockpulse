package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private final String TEST_EMAIL = "test@gmail.com";

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    testUser = User.builder()
            .firstName("Bruce")
            .email(TEST_EMAIL)
            .password("encodedPassword123")
            .role(Role.REGULAR_USER)
            .build();
  }

  @Test
  void findByEmail_WhenUserExists_ShouldReturnUser() {
    // Arrange
    User result = userRepository.save(testUser);

    // Act
    Optional<User> foundUser = userRepository.findByEmail(TEST_EMAIL);

    // Assert
    assertTrue(foundUser.isPresent(), "User should be found");
    assertEquals(result.getId(), foundUser.get().getId());
    assertEquals(result.getEmail(), foundUser.get().getEmail());
    assertEquals(result.getFirstName(), foundUser.get().getFirstName());
    assertEquals(result.getRole(), foundUser.get().getRole());
  }

  @Test
  void findByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
    // Act
    Optional<User> result = userRepository.findByEmail("nonexistent@email.com");

    // Assert
    assertTrue(result.isEmpty(), "User should not be found");
  }

  @Test
  void findByEmail_WithNullEmail_ShouldReturnEmpty() {
    // Act
    Optional<User> result = userRepository.findByEmail(null);

    // Assert
    assertFalse(result.isPresent(), "User should not be found for null email");
  }

  @Test
  void existsByEmail_WhenUserExists_ShouldReturnTrue() {
    // Arrange
    userRepository.save(testUser);

    // Act
    boolean userExists = userRepository.existsByEmail(TEST_EMAIL);

    // Assert
    assertTrue(userExists, "User should exist");
  }

  @Test
  void existsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
    // Act
    boolean userExists = userRepository.existsByEmail("nonexistent@email.com");

    // Assert
    assertFalse(userExists, "User should not exist");
  }

  @Test
  void existsByEmail_WithNullEmail_ShouldReturnFalse() {
    // Act
    boolean userExists = userRepository.existsByEmail(null);

    // Assert
    assertFalse(userExists, "User should not exist for null email");
  }
}