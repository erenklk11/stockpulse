package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock
  private JavaMailSender javaMailSender;

  @InjectMocks
  private EmailService emailService;

  private VerificationToken testVerificationToken;
  private VerificationToken testPasswordResetToken;
  private User testUser;
  private final String TEST_EMAIL = "test@email.com";
  private final String TEST_TOKEN = "test-token-123";
  private final String TEST_FROM_EMAIL = "noreply@stockpulse.com";
  private final String TEST_APP_URL = "http://localhost:3000";

  @BeforeEach
  void setUp() {
    testUser = User.builder()
            .firstName("Bruce")
            .email(TEST_EMAIL)
            .role(Role.REGULAR_USER)
            .build();

    testVerificationToken = VerificationToken.builder()
            .token(TEST_TOKEN)
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now())
            .expiresOn(LocalDateTime.now().plusHours(1))
            .build();

    testPasswordResetToken = VerificationToken.builder()
            .token(TEST_TOKEN)
            .tokenType(TokenType.PASSWORD_RESET)
            .user(testUser)
            .createdAt(LocalDateTime.now())
            .expiresOn(LocalDateTime.now().plusHours(1))
            .build();

    ReflectionTestUtils.setField(emailService, "from", TEST_FROM_EMAIL);
    ReflectionTestUtils.setField(emailService, "appUrl", TEST_APP_URL);
  }

  @Test
  void sendVerificationEmail_whenPasswordResetToken_shouldSendEmailWithCorrectSubject() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendVerificationEmail(TEST_EMAIL, testPasswordResetToken);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertNotNull(sentMessage);
    assertEquals(TEST_FROM_EMAIL, sentMessage.getFrom());
    assertEquals(TEST_EMAIL, sentMessage.getTo()[0]);
    assertEquals("Reset Your Password", sentMessage.getSubject());
    assertTrue(sentMessage.getText().contains("Please click the link below to verify your email address:"));
    assertTrue(sentMessage.getText().contains(TEST_APP_URL + "/verify-email?token=" + TEST_TOKEN));
    assertTrue(sentMessage.getText().contains("This link will expire in 1 hours."));
  }

  @Test
  void sendVerificationEmail_shouldIncludeCorrectVerificationLink() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendVerificationEmail(TEST_EMAIL, testVerificationToken);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    String expectedLink = TEST_APP_URL + "/verify-email?token=" + TEST_TOKEN;
    assertTrue(sentMessage.getText().contains(expectedLink));
  }

  @Test
  void sendVerificationEmail_whenJavaMailSenderThrowsException_shouldThrowMailSendException() {
    // Arrange
    RuntimeException mailException = new RuntimeException("SMTP server unavailable");
    doThrow(mailException).when(javaMailSender).send(any(SimpleMailMessage.class));

    // Act & Assert
    MailSendException exception = assertThrows(
            MailSendException.class,
            () -> emailService.sendVerificationEmail(TEST_EMAIL, testVerificationToken)
    );

    assertTrue(exception.getMessage().contains("Failed to send verification email"));
    assertEquals(mailException, exception.getCause());
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendVerificationEmail_whenEmailConfigurationIsCorrect_shouldSetAllEmailFields() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendVerificationEmail(TEST_EMAIL, testPasswordResetToken);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertEquals(TEST_FROM_EMAIL, sentMessage.getFrom());
    assertEquals(1, sentMessage.getTo().length);
    assertEquals(TEST_EMAIL, sentMessage.getTo()[0]);
    assertNotNull(sentMessage.getText());
    assertFalse(sentMessage.getText().isEmpty());
  }

  @Test
  void sendVerificationEmail_withNullTokenType_shouldNotSetSubject() {
    // Arrange
    VerificationToken tokenWithNullType = VerificationToken.builder()
            .token(TEST_TOKEN)
            .tokenType(null)
            .user(testUser)
            .createdAt(LocalDateTime.now())
            .expiresOn(LocalDateTime.now().plusHours(1))
            .build();

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendVerificationEmail(TEST_EMAIL, tokenWithNullType);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertNull(sentMessage.getSubject());
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendVerificationEmail_shouldCallJavaMailSenderExactlyOnce() {
    // Act
    emailService.sendVerificationEmail(TEST_EMAIL, testVerificationToken);

    // Assert
    verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}