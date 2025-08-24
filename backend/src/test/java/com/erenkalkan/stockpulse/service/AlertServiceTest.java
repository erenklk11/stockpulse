package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.*;
import com.erenkalkan.stockpulse.model.dto.CreateAlertRequestDTO;
import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import com.erenkalkan.stockpulse.model.enums.Role;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
import com.erenkalkan.stockpulse.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

  @Mock
  private AlertRepository alertRepository;

  @Mock
  private StocksService stocksService;

  @Mock
  private WatchlistService watchlistService;

  @Mock
  private UserService userService;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private AlertService alertService;

  private Alert testAlert;
  private Stock testStock;
  private User testUser;
  private Watchlist testWatchlist;
  private CreateAlertRequestDTO createAlertRequest;

  @BeforeEach
  void setUp() {
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

    testAlert = Alert.builder()
            .stock(testStock)
            .watchlist(testWatchlist)
            .triggerType(TriggerType.TO_PRICE)
            .condition(ConditionType.ABOVE)
            .targetValue(160.00)
            .build();

    createAlertRequest = CreateAlertRequestDTO.builder()
            .stock(testStock)
            .triggerType(TriggerType.TO_PRICE)
            .targetValue(160.00)
            .watchlistId(1L)
            .build();
  }

  @Test
  @DisplayName("save - Should save alert successfully")
  void save_ShouldSaveAlertSuccessfully() {
    // Arrange
    when(alertRepository.save(testAlert)).thenReturn(testAlert);

    // Act
    Alert result = alertService.save(testAlert);

    // Assert
    assertNotNull(result);
    assertEquals(testAlert.getId(), result.getId());
    assertEquals(testAlert.getStock().getSymbol(), result.getStock().getSymbol());
    verify(alertRepository, times(1)).save(testAlert);
  }

  @Test
  @DisplayName("save - Should throw DatabaseOperationException when repository throws exception")
  void save_ShouldThrowDatabaseOperationException_WhenRepositoryThrowsException() {
    // Arrange
    when(alertRepository.save(testAlert)).thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    DatabaseOperationException exception = assertThrows(
            DatabaseOperationException.class,
            () -> alertService.save(testAlert)
    );

    assertTrue(exception.getMessage().contains("Failed to save alert to database"));
    verify(alertRepository, times(1)).save(testAlert);
  }

  @Test
  @DisplayName("delete - Should delete alert successfully")
  void delete_ShouldDeleteAlertSuccessfully() {
    // Arrange
    doNothing().when(alertRepository).delete(testAlert);

    // Act
    boolean result = alertService.delete(testAlert);

    // Assert
    assertTrue(result);
    verify(alertRepository, times(1)).delete(testAlert);
  }

  @Test
  @DisplayName("delete - Should throw DatabaseOperationException when repository throws exception")
  void delete_ShouldThrowDatabaseOperationException_WhenRepositoryThrowsException() {
    // Arrange
    doThrow(new RuntimeException("Database error")).when(alertRepository).delete(testAlert);

    // Act & Assert
    DatabaseOperationException exception = assertThrows(
            DatabaseOperationException.class,
            () -> alertService.delete(testAlert)
    );

    assertTrue(exception.getMessage().contains("Failed to delete alert from database"));
    verify(alertRepository, times(1)).delete(testAlert);
  }

  @Test
  @DisplayName("findAllBySymbol - Should return alerts when stock exists")
  void findAllBySymbol_ShouldReturnAlerts_WhenStockExists() {
    // Arrange
    String symbol = "AAPL";
    List<Alert> expectedAlerts = List.of(testAlert);
    when(stocksService.findBySymbol(symbol)).thenReturn(Optional.of(testStock));
    when(alertRepository.findAllByStock(testStock)).thenReturn(expectedAlerts);

    // Act
    List<Alert> result = alertService.findAllBySymbol(symbol);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testAlert.getId(), result.get(0).getId());
    verify(stocksService, times(1)).findBySymbol(symbol);
    verify(alertRepository, times(1)).findAllByStock(testStock);
  }

  @Test
  @DisplayName("findAllBySymbol - Should return empty list when stock does not exist")
  void findAllBySymbol_ShouldReturnEmptyList_WhenStockDoesNotExist() {
    // Arrange
    String symbol = "NONEXISTENT";
    when(stocksService.findBySymbol(symbol)).thenReturn(Optional.empty());

    // Act
    List<Alert> result = alertService.findAllBySymbol(symbol);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(stocksService, times(1)).findBySymbol(symbol);
    verify(alertRepository, never()).findAllByStock(any());
  }

  @Test
  @DisplayName("findAllBySymbol - Should throw InvalidInputException when symbol is null")
  void findAllBySymbol_ShouldThrowInvalidInputException_WhenSymbolIsNull() {
    // Arrange
    String symbol = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> alertService.findAllBySymbol(symbol)
    );

    assertEquals("Stock symbol cannot be null", exception.getMessage());
    verify(stocksService, never()).findBySymbol(any());
  }

  @Test
  @DisplayName("findAllBySymbol - Should throw InvalidInputException when symbol is empty")
  void findAllBySymbol_ShouldThrowInvalidInputException_WhenSymbolIsEmpty() {
    // Arrange
    String symbol = "   ";

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> alertService.findAllBySymbol(symbol)
    );

    assertEquals("Stock symbol cannot be null", exception.getMessage());
    verify(stocksService, never()).findBySymbol(any());
  }

  @Test
  @DisplayName("createAlert - Should create alert successfully with TO_PRICE trigger type")
  void createAlert_ShouldCreateAlertSuccessfully_WithToPriceTriggerType() {
    // Arrange
    when(authentication.getName()).thenReturn("test@example.com");
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(watchlistService.getWatchlist(1L)).thenReturn(Optional.of(testWatchlist));
    when(stocksService.save(testStock)).thenReturn(testStock);
    when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

    // Act
    boolean result = alertService.createAlert(createAlertRequest, authentication);

    // Assert
    assertTrue(result);
    verify(userService, times(1)).findByEmail("test@example.com");
    verify(watchlistService, times(1)).getWatchlist(1L);
    verify(stocksService, times(1)).save(testStock);
    verify(alertRepository, times(1)).save(any(Alert.class));
  }

  @Test
  @DisplayName("createAlert - Should create alert successfully with PERCENTAGE_CHANGE_PRICE trigger type")
  void createAlert_ShouldCreateAlertSuccessfully_WithPercentageChangePriceTriggerType() {
    // Arrange
    CreateAlertRequestDTO percentageRequest = CreateAlertRequestDTO.builder()
            .stock(testStock)
            .triggerType(TriggerType.PERCENTAGE_CHANGE_PRICE)
            .targetValue(160.00)
            .percentageValue(10.0)
            .watchlistId(1L)
            .build();

    when(authentication.getName()).thenReturn("test@example.com");
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(watchlistService.getWatchlist(1L)).thenReturn(Optional.of(testWatchlist));
    when(stocksService.save(testStock)).thenReturn(testStock);
    when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

    // Act
    boolean result = alertService.createAlert(percentageRequest, authentication);

    // Assert
    assertTrue(result);
    verify(userService, times(1)).findByEmail("test@example.com");
    verify(watchlistService, times(1)).getWatchlist(1L);
    verify(stocksService, times(1)).save(testStock);
    verify(alertRepository, times(1)).save(any(Alert.class));
  }

  @Test
  @DisplayName("createAlert - Should throw UserNotFoundException when user does not exist")
  void createAlert_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
    // Arrange
    when(authentication.getName()).thenReturn("nonexistent@example.com");
    when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> alertService.createAlert(createAlertRequest, authentication)
    );

    assertTrue(exception.getMessage().contains("User with email: nonexistent@example.com was not found"));
    verify(userService, times(1)).findByEmail("nonexistent@example.com");
    verify(watchlistService, never()).getWatchlist(any());
  }

  @Test
  @DisplayName("createAlert - Should throw ResourceNotFoundException when watchlist does not exist")
  void createAlert_ShouldThrowResourceNotFoundException_WhenWatchlistDoesNotExist() {
    // Arrange
    when(authentication.getName()).thenReturn("test@example.com");
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(watchlistService.getWatchlist(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> alertService.createAlert(createAlertRequest, authentication)
    );

    assertTrue(exception.getMessage().contains("Watchlist with id 1 does not exist"));
    verify(userService, times(1)).findByEmail("test@example.com");
    verify(watchlistService, times(1)).getWatchlist(1L);
    verify(stocksService, never()).save(any());
  }

  @Test
  @DisplayName("deleteAlert - Should delete alert successfully")
  void deleteAlert_ShouldDeleteAlertSuccessfully() {
    // Arrange
    Long alertId = 1L;
    when(authentication.getName()).thenReturn("test@example.com");
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(alertRepository.findById(alertId)).thenReturn(Optional.of(testAlert));
    doNothing().when(alertRepository).delete(testAlert);

    // Act
    boolean result = alertService.deleteAlert(alertId, authentication);

    // Assert
    assertTrue(result);
    verify(userService, times(1)).findByEmail("test@example.com");
    verify(alertRepository, times(1)).findById(alertId);
    verify(alertRepository, times(1)).delete(testAlert);
  }

  @Test
  @DisplayName("deleteAlert - Should throw InvalidInputException when alert ID is null")
  void deleteAlert_ShouldThrowInvalidInputException_WhenAlertIdIsNull() {
    // Arrange
    Long alertId = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> alertService.deleteAlert(alertId, authentication)
    );

    assertEquals("Alert ID cannot be null", exception.getMessage());
    verify(userService, never()).findByEmail(any());
  }

  @Test
  @DisplayName("deleteAlert - Should throw UserNotFoundException when user does not exist")
  void deleteAlert_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
    // Arrange
    Long alertId = 1L;
    when(authentication.getName()).thenReturn("nonexistent@example.com");
    when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> alertService.deleteAlert(alertId, authentication)
    );

    assertTrue(exception.getMessage().contains("User with email: nonexistent@example.com was not found"));
    verify(userService, times(1)).findByEmail("nonexistent@example.com");
    verify(alertRepository, never()).findById(any());
  }

  @Test
  @DisplayName("deleteAlert - Should throw ResourceNotFoundException when alert does not exist")
  void deleteAlert_ShouldThrowResourceNotFoundException_WhenAlertDoesNotExist() {
    // Arrange
    Long alertId = 999L;
    when(authentication.getName()).thenReturn("test@example.com");
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> alertService.deleteAlert(alertId, authentication)
    );

    assertTrue(exception.getMessage().contains("Alert with id 999 does not exist"));
    verify(userService, times(1)).findByEmail("test@example.com");
    verify(alertRepository, times(1)).findById(alertId);
    verify(alertRepository, never()).delete(any());
  }

  @Test
  @DisplayName("deleteAlert - Should throw UnauthorizedAccessException when user is not authorized")
  void deleteAlert_ShouldThrowUnauthorizedAccessException_WhenUserIsNotAuthorized() {
    // Arrange
    Long alertId = 1L;
    User differentUser = User.builder()
            .id(2L)
            .email("different@example.com")
            .firstName("Different")
            .build();

    when(authentication.getName()).thenReturn("different@example.com");
    when(userService.findByEmail("different@example.com")).thenReturn(Optional.of(differentUser));
    when(alertRepository.findById(alertId)).thenReturn(Optional.of(testAlert));

    // Act & Assert
    UnauthorizedAccessException exception = assertThrows(
            UnauthorizedAccessException.class,
            () -> alertService.deleteAlert(alertId, authentication)
    );

    assertEquals("User is not authorized to delete this alert", exception.getMessage());
    verify(userService, times(1)).findByEmail("different@example.com");
    verify(alertRepository, times(1)).findById(alertId);
    verify(alertRepository, never()).delete(any());
  }
}