package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.entity.*;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
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
  private Alert testAlert;
  private Stock testStock;
  private Watchlist testWatchlist;

  private final String TEST_EMAIL = "test@email.com";
  private final String TEST_TOKEN = "test-token-123";
  private final String TEST_FROM_EMAIL = "noreply@stockpulse.com";
  private final String TEST_APP_URL = "http://localhost:3000";
  private final String TEST_STOCK_SYMBOL = "AAPL";
  private final Double TEST_TARGET_VALUE = 150.00;

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

    testStock = Stock.builder()
            .companyName("Apple Inc.")
            .symbol("AAPL")
            .build();

    testWatchlist = Watchlist.builder()
            .watchlistName("Test Watchlist")
            .user(testUser)
            .build();

    testAlert = Alert.builder()
            .stock(testStock)
            .watchlist(testWatchlist)
            .triggerType(TriggerType.TO_PRICE)
            .condition(ConditionType.ABOVE)
            .targetValue(TEST_TARGET_VALUE)
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

  @Test
  void sendAlertEmail_whenAlertConditionIsAbove_shouldSendEmailWithCorrectContent() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();
    System.out.println(sentMessage.getText());

    assertNotNull(sentMessage);
    assertEquals(TEST_FROM_EMAIL, sentMessage.getFrom());
    assertEquals(TEST_EMAIL, sentMessage.getTo()[0]);
    assertTrue(sentMessage.getSubject().contains("Your Alert for AAPL has been triggered!"));
    assertTrue(sentMessage.getText().contains("Hey Bruce,"));
    assertTrue(sentMessage.getText().contains("your Alert for the stock: " + TEST_STOCK_SYMBOL));
    assertTrue(sentMessage.getText().contains("Price is now above your target of 150.00"));
  }

  @Test
  void sendAlertEmail_whenAlertConditionIsBelow_shouldSendEmailWithCorrectContent() {
    // Arrange
    testAlert = Alert.builder()
            .stock(testStock)
            .watchlist(testWatchlist)
            .condition(ConditionType.BELOW)
            .triggerType(TriggerType.TO_PRICE)
            .targetValue(TEST_TARGET_VALUE)
            .build();

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertTrue(sentMessage.getText().contains("Price is now below your target of 150.00"));
  }

  @Test
  void sendAlertEmail_shouldSetCorrectEmailFields() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertEquals(TEST_FROM_EMAIL, sentMessage.getFrom());
    assertEquals(1, sentMessage.getTo().length);
    assertEquals(TEST_EMAIL, sentMessage.getTo()[0]);
    assertNotNull(sentMessage.getText());
    assertFalse(sentMessage.getText().isEmpty());
    assertFalse(sentMessage.getSubject().isEmpty());
  }

  @Test
  void sendAlertEmail_shouldIncludeUserFirstNameInMessage() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertTrue(sentMessage.getText().contains("Hey " + testUser.getFirstName() + ","));
  }

  @Test
  void sendAlertEmail_shouldIncludeStockSymbolInMessage() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertTrue(sentMessage.getText().contains("stock: " + TEST_STOCK_SYMBOL));
  }

  @Test
  void sendAlertEmail_shouldFormatTargetValueWithTwoDecimals() {
    // Arrange
    Double targetValue = 12345.00;
    testAlert = Alert.builder()
            .stock(testStock)
            .watchlist(testWatchlist)
            .triggerType(TriggerType.TO_PRICE)
            .condition(ConditionType.ABOVE)
            .targetValue(targetValue)
            .build();

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender).send(messageCaptor.capture());
    SimpleMailMessage sentMessage = messageCaptor.getValue();

    assertTrue(sentMessage.getText().contains("12345.00"));
  }

  @Test
  void sendAlertEmail_whenJavaMailSenderThrowsException_shouldThrowMailSendException() {
    // Arrange
    RuntimeException mailException = new RuntimeException("SMTP server unavailable");
    doThrow(mailException).when(javaMailSender).send(any(SimpleMailMessage.class));

    // Act & Assert
    MailSendException exception = assertThrows(
            MailSendException.class,
            () -> emailService.sendAlertEmail(testAlert)
    );

    assertTrue(exception.getMessage().contains("Failed to send alert trigger email"));
    assertEquals(mailException, exception.getCause());
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendAlertEmail_shouldCallJavaMailSenderExactlyOnce() {
    // Act
    emailService.sendAlertEmail(testAlert);

    // Assert
    verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendAlertEmail_withDifferentAlertConditions_shouldUseCorrectConditionText() {

    for (ConditionType condition : ConditionType.values()) {
      // Arrange
      Alert alert = Alert.builder()
              .stock(testStock)
              .watchlist(testWatchlist)
              .triggerType(TriggerType.TO_PRICE)
              .condition(condition)
              .targetValue(TEST_TARGET_VALUE)
              .build();

      ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

      // Act
      emailService.sendAlertEmail(alert);

      // Assert
      verify(javaMailSender).send(messageCaptor.capture());
      SimpleMailMessage sentMessage = messageCaptor.getValue();

      String expectedConditionText = condition.toString().toLowerCase();
      assertTrue(sentMessage.getText().contains("Price is now " + expectedConditionText + " your target"));

      // Reset mock for next iteration
      reset(javaMailSender);
    }
  }


}