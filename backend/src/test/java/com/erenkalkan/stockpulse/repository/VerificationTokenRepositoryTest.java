package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VerificationTokenRepositoryTest {

  @Autowired
  private VerificationTokenRepository verificationTokenRepository;

  @Autowired
  private UserRepository userRepository;

  private final String TEST_TOKEN = "encryptedTokenXXX";
  private VerificationToken testVerificationToken;
  private User testUser;

  @BeforeEach
  void setUp() {
    verificationTokenRepository.deleteAll();
    userRepository.deleteAll();

    testUser = User.builder()
            .firstName("Bruce")
            .email("test@email.com")
            .password("encodedPassword123")
            .role(Role.REGULAR_USER)
            .build();

    testUser = userRepository.save(testUser);

    testVerificationToken = VerificationToken.builder()
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .token(TEST_TOKEN)
            .expirationMs(1000L)
            .build();
  }

  @Test
  void findByToken_whenTokenExists_shouldReturnVerificationToken() {
    // Arrange
    verificationTokenRepository.save(testVerificationToken);

    // Act
    Optional<VerificationToken> result = verificationTokenRepository.findByToken(TEST_TOKEN);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(result.get().getId(), testVerificationToken.getId());
    assertEquals(result.get().getToken(), testVerificationToken.getToken());
  }

  @Test
  void findByToken_whenTokenDoesNotExist_shouldReturnEmpty() {
    // Arrange
    verificationTokenRepository.save(testVerificationToken);
    String notExistingToken = "nonExistentTokenXXX";

    // Act
    Optional<VerificationToken> result = verificationTokenRepository.findByToken(notExistingToken);

    // Assert
    assertFalse(result.isPresent());
  }

}