package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.service.websocket.AlertTriggerWebSocketHandler;
import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private AlertTriggerWebSocketHandler webSocketHandler;

  @InjectMocks
  private NotificationService notificationService;

  private User user;
  private Stock stock;
  private Watchlist watchlist;
  private Alert alert;

  @BeforeEach
  void setUp() {
    // Arrange - Create test data objects
    user = new User();
    user.setEmail("test@example.com");

    stock = new Stock();
    stock.setSymbol("AAPL");

    watchlist = new Watchlist();
    watchlist.setUser(user);

    alert = new Alert();
    alert.setStock(stock);
    alert.setWatchlist(watchlist);
    alert.setTargetValue(150.0);
    alert.setCondition(ConditionType.ABOVE);
  }

  private String formatExpectedMessage(String symbol, String condition, Double targetValue) {
    return String.format("Alert for %s: Price is now %s your target of %.2f",
            symbol, condition, targetValue);
  }

  @Test
  void sendAlertNotification_WithValidAlert_ShouldBroadcastMessage() throws Exception {
    // Arrange
    String expectedMessage = formatExpectedMessage("AAPL", "above", 150.0);
    doNothing().when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WithBelowCondition_ShouldFormatMessageCorrectly() throws Exception {
    // Arrange
    alert.setCondition(ConditionType.BELOW);
    alert.setTargetValue(100.0);
    String expectedMessage = formatExpectedMessage("AAPL", "below", 100.0);
    doNothing().when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WithDifferentStock_ShouldIncludeCorrectSymbol() throws Exception {
    // Arrange
    stock.setSymbol("GOOGL");
    String expectedMessage = formatExpectedMessage("GOOGL", "above", 150.0);
    doNothing().when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WhenWebSocketHandlerThrowsException_ShouldHandleGracefully() throws Exception {
    // Arrange
    String expectedMessage = formatExpectedMessage("AAPL", "above", 150.0);
    RuntimeException exception = new RuntimeException("WebSocket connection failed");
    doThrow(exception).when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WithLongTargetValue_ShouldFormatToTwoDecimalPlaces() throws Exception {
    // Arrange
    alert.setTargetValue(150.75);
    String expectedMessage = formatExpectedMessage("AAPL", "above", 150.75);
    doNothing().when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WithWholeNumberTarget_ShouldStillShowTwoDecimalPlaces() throws Exception {
    // Arrange
    alert.setTargetValue(200.0);
    String expectedMessage = formatExpectedMessage("AAPL", "above", 200.0);
    doNothing().when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WhenGenericExceptionThrown_ShouldCatchAndHandleGracefully() throws Exception {
    // Arrange
    String expectedMessage = formatExpectedMessage("AAPL", "above", 150.0);
    IllegalStateException exception = new IllegalStateException("Unexpected error");
    doThrow(exception).when(webSocketHandler).broadcast(eq(user), eq(expectedMessage));

    // Act
    notificationService.sendAlertNotification(alert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, expectedMessage);
  }

  @Test
  void sendAlertNotification_WithMultipleCalls_ShouldHandleEachIndependently() throws Exception {
    // Arrange
    Alert secondAlert = new Alert();
    Stock secondStock = new Stock();
    secondStock.setSymbol("TSLA");
    secondAlert.setStock(secondStock);
    secondAlert.setWatchlist(watchlist);
    secondAlert.setTargetValue(800.00);
    secondAlert.setCondition(ConditionType.BELOW);

    String firstMessage = formatExpectedMessage("AAPL", "above", 150.00);
    String secondMessage = formatExpectedMessage("TSLA", "below", 800.00);

    doNothing().when(webSocketHandler).broadcast(eq(user), any(String.class));

    // Act
    notificationService.sendAlertNotification(alert);
    notificationService.sendAlertNotification(secondAlert);

    // Assert
    verify(webSocketHandler, times(1)).broadcast(user, firstMessage);
    verify(webSocketHandler, times(1)).broadcast(user, secondMessage);
  }
}