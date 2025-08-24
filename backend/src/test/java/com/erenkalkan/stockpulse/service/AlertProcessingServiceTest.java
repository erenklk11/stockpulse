package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.dto.StockPriceDTO;
import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertProcessingServiceTest {

  @Mock
  private AlertService alertService;

  @Mock
  private KafkaTemplate<String, Alert> alertKafkaTemplate;

  @InjectMocks
  private AlertProcessingService alertProcessingService;

  private StockPriceDTO stockPriceDTO;
  private Alert aboveAlert;
  private Alert belowAlert;
  private Alert triggeredAlert;
  private Stock testStock;
  private User testUser;
  private Watchlist testWatchlist;

  @BeforeEach
  void setUp() {
    // Arrange
    ReflectionTestUtils.setField(alertProcessingService, "alertTriggersTopic", "alert-triggers");

    testStock = Stock.builder()
            .companyName("Apple Inc.")
            .symbol("AAPL")
            .build();

    testUser = User.builder()
            .id(1L)
            .firstName("Bruce")
            .email("test@email.com")
            .role(Role.REGULAR_USER)
            .build();

    testWatchlist = Watchlist.builder()
            .watchlistName("Test Watchlist")
            .user(testUser)
            .build();

    stockPriceDTO = StockPriceDTO.builder()
            .symbol("AAPL")
            .price(150.00)
            .build();

    aboveAlert = createAlert(1L, ConditionType.ABOVE, 140.00, false);
    belowAlert = createAlert(2L, ConditionType.BELOW, 160.00, false);
    triggeredAlert = createAlert(3L, ConditionType.ABOVE, 130.00, true);
  }

  private Alert createAlert(Long id, ConditionType condition, Double targetValue, boolean isTriggered) {
    return Alert.builder()
            .id(id)
            .stock(testStock)
            .watchlist(testWatchlist)
            .triggerType(TriggerType.TO_PRICE)
            .isTriggered(isTriggered)
            .condition(condition)
            .targetValue(targetValue)
            .build();
  }

  @Test
  void processStockPrice_WhenAboveConditionMet_ShouldTriggerAlert() {
    // Arrange
    List<Alert> alerts = Collections.singletonList(aboveAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertTrue(aboveAlert.isTriggered(), "Alert should be marked as triggered");
    verify(alertKafkaTemplate, times(1)).send("alert-triggers", aboveAlert);
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenBelowConditionMet_ShouldTriggerAlert() {
    // Arrange
    List<Alert> alerts = Collections.singletonList(belowAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertTrue(belowAlert.isTriggered(), "Alert should be marked as triggered");
    verify(alertKafkaTemplate, times(1)).send("alert-triggers", belowAlert);
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenAboveConditionNotMet_ShouldNotTriggerAlert() {
    // Arrange
    Alert highTargetAlert = createAlert(4L, ConditionType.ABOVE, 200.00, false);
    List<Alert> alerts = Collections.singletonList(highTargetAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertFalse(highTargetAlert.isTriggered(), "Alert should not be marked as triggered");
    verify(alertKafkaTemplate, never()).send(anyString(), any(Alert.class));
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenBelowConditionNotMet_ShouldNotTriggerAlert() {
    // Arrange
    Alert lowTargetAlert = createAlert(5L, ConditionType.BELOW, 100.00, false);
    List<Alert> alerts = Collections.singletonList(lowTargetAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertFalse(lowTargetAlert.isTriggered(), "Alert should not be marked as triggered");
    verify(alertKafkaTemplate, never()).send(anyString(), any(Alert.class));
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenAlertAlreadyTriggered_ShouldNotTriggerAgain() {
    // Arrange
    List<Alert> alerts = Collections.singletonList(triggeredAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertTrue(triggeredAlert.isTriggered(), "Alert should remain triggered");
    verify(alertKafkaTemplate, never()).send(anyString(), any(Alert.class));
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenMultipleAlertsExist_ShouldProcessAllCorrectly() {
    // Arrange
    Alert anotherAboveAlert = createAlert(6L, ConditionType.ABOVE, 145.00, false);
    Alert anotherBelowAlert = createAlert(7L, ConditionType.BELOW, 155.00, false);
    Alert nonTriggeringAlert = createAlert(8L, ConditionType.ABOVE, 200.00, false);

    List<Alert> alerts = Arrays.asList(aboveAlert, anotherAboveAlert, anotherBelowAlert, nonTriggeringAlert, triggeredAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertTrue(aboveAlert.isTriggered(), "First above alert should be triggered");
    assertTrue(anotherAboveAlert.isTriggered(), "Second above alert should be triggered");
    assertTrue(anotherBelowAlert.isTriggered(), "Below alert should be triggered");
    assertFalse(nonTriggeringAlert.isTriggered(), "Non-triggering alert should not be triggered");
    assertTrue(triggeredAlert.isTriggered(), "Already triggered alert should remain triggered");

    // Verify Kafka messages sent for newly triggered alerts only
    verify(alertKafkaTemplate, times(3)).send(eq("alert-triggers"), any(Alert.class));
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenNoAlertsExist_ShouldHandleGracefully() {
    // Arrange
    when(alertService.findAllBySymbol("AAPL")).thenReturn(Collections.emptyList());

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    verify(alertKafkaTemplate, never()).send(anyString(), any(Alert.class));
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_WhenPriceEqualsTargetValue_ShouldNotTriggerAlert() {
    // Arrange
    Alert equalTargetAlert = createAlert(9L, ConditionType.ABOVE, 150.00, false);
    List<Alert> alerts = Collections.singletonList(equalTargetAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    assertFalse(equalTargetAlert.isTriggered(), "Alert should not be triggered for equal values with ABOVE condition");
    verify(alertKafkaTemplate, never()).send(anyString(), any(Alert.class));
    verify(alertService, times(1)).findAllBySymbol("AAPL");
  }

  @Test
  void processStockPrice_ShouldSendCorrectAlertToKafka() {
    // Arrange
    List<Alert> alerts = Collections.singletonList(aboveAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);
    ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);

    // Act
    alertProcessingService.processStockPrice(stockPriceDTO);

    // Assert
    verify(alertKafkaTemplate).send(eq("alert-triggers"), alertCaptor.capture());
    Alert capturedAlert = alertCaptor.getValue();
    assertEquals(aboveAlert.getId(), capturedAlert.getId(), "Correct alert should be sent to Kafka");
    assertTrue(capturedAlert.isTriggered(), "Alert sent to Kafka should be marked as triggered");
  }

  @Test
  void processStockPrice_WhenKafkaTemplateFails_ShouldNotAffectAlertState() {
    // Arrange
    List<Alert> alerts = Collections.singletonList(aboveAlert);
    when(alertService.findAllBySymbol("AAPL")).thenReturn(alerts);
    doThrow(new RuntimeException("Kafka error")).when(alertKafkaTemplate).send(anyString(), any(Alert.class));

    // Act
    RuntimeException exception = assertThrows(RuntimeException.class,
            () -> alertProcessingService.processStockPrice(stockPriceDTO));

    // Assert
    assertEquals("Kafka error", exception.getMessage());
    assertFalse(aboveAlert.isTriggered(), "Alert should be marked as triggered before Kafka failure");
    verify(alertKafkaTemplate, times(1)).send("alert-triggers", aboveAlert);
  }
}