package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.*;
import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
import com.erenkalkan.stockpulse.repository.WatchlistRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

  @Mock
  private WatchlistRepository watchlistRepository;

  @Mock
  private UserService userService;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private WatchlistService watchlistService;

  private User testUser;
  private Watchlist testWatchlist;
  private static final String TEST_EMAIL = "test@example.com";
  private static final Long TEST_USER_ID = 1L;
  private static final Long TEST_WATCHLIST_ID = 1L;
  private static final String TEST_WATCHLIST_NAME = "Test Watchlist";


  @BeforeEach
  void setUp() {
    testUser = User.builder()
            .id(TEST_USER_ID)
            .firstName("Bruce")
            .email(TEST_EMAIL)
            .build();

    testWatchlist = Watchlist.builder()
            .id(TEST_WATCHLIST_ID)
            .watchlistName("Test Watchlist")
            .user(testUser)
            .build();
  }

  @Test
  @DisplayName("Save watchlist successfully")
  void save_ShouldReturnSavedWatchlist_WhenValidWatchlist() {
    // Arrange
    when(watchlistRepository.save(testWatchlist)).thenReturn(testWatchlist);

    // Act
    Watchlist result = watchlistService.save(testWatchlist);

    // Assert
    assertNotNull(result);
    assertEquals(testWatchlist.getId(), result.getId());
    assertEquals(testWatchlist.getWatchlistName(), result.getWatchlistName());
    verify(watchlistRepository, times(1)).save(testWatchlist);
  }

  @Test
  @DisplayName("Save watchlist should throw DatabaseOperationException when repository throws exception")
  void save_ShouldThrowDatabaseOperationException_WhenRepositoryThrowsException() {
    // Arrange
    RuntimeException repositoryException = new RuntimeException("Database error");
    when(watchlistRepository.save(testWatchlist)).thenThrow(repositoryException);

    // Act & Assert
    DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
            () -> watchlistService.save(testWatchlist));

    assertTrue(exception.getMessage().contains("Failed to save watchlist to database"));
    assertEquals(repositoryException, exception.getCause());
    verify(watchlistRepository, times(1)).save(testWatchlist);
  }

  @Test
  @DisplayName("Delete watchlist successfully")
  void delete_ShouldReturnTrue_WhenWatchlistDeletedSuccessfully() {
    // Arrange
    doNothing().when(watchlistRepository).delete(testWatchlist);

    // Act
    boolean result = watchlistService.delete(testWatchlist);

    // Assert
    assertTrue(result);
    verify(watchlistRepository, times(1)).delete(testWatchlist);
  }

  @Test
  @DisplayName("Delete watchlist should throw DatabaseOperationException when repository throws exception")
  void delete_ShouldThrowDatabaseOperationException_WhenRepositoryThrowsException() {
    // Arrange
    RuntimeException repositoryException = new RuntimeException("Database error");
    doThrow(repositoryException).when(watchlistRepository).delete(testWatchlist);

    // Act & Assert
    DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
            () -> watchlistService.delete(testWatchlist));

    assertTrue(exception.getMessage().contains("Failed to delete watchlist from database"));
    assertEquals(repositoryException, exception.getCause());
    verify(watchlistRepository, times(1)).delete(testWatchlist);
  }

  @Test
  @DisplayName("Create watchlist successfully")
  void createWatchlist_ShouldReturnCreatedWatchlist_WhenValidInputs() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlist);

    // Act
    Watchlist result = watchlistService.createWatchlist(TEST_WATCHLIST_NAME, authentication);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_WATCHLIST_NAME, result.getWatchlistName());
    assertEquals(testUser, result.getUser());
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).save(any(Watchlist.class));
  }

  @Test
  @DisplayName("Create watchlist should throw InvalidInputException when watchlist name is null")
  void createWatchlist_ShouldThrowInvalidInputException_WhenWatchlistNameIsNull() {
    // Arrange
    // No arrangement needed for null input

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> watchlistService.createWatchlist(null, authentication));

    assertEquals("Watchlist name cannot be null or empty", exception.getMessage());
    verify(userService, never()).findByEmail(anyString());
    verify(watchlistRepository, never()).save(any(Watchlist.class));
  }

  @Test
  @DisplayName("Create watchlist should throw InvalidInputException when watchlist name is empty")
  void createWatchlist_ShouldThrowInvalidInputException_WhenWatchlistNameIsEmpty() {
    // Arrange
    String emptyName = "   ";

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> watchlistService.createWatchlist(emptyName, authentication));

    assertEquals("Watchlist name cannot be null or empty", exception.getMessage());
    verify(userService, never()).findByEmail(anyString());
    verify(watchlistRepository, never()).save(any(Watchlist.class));
  }

  @Test
  @DisplayName("Create watchlist should throw UserNotFoundException when user not found")
  void createWatchlist_ShouldThrowUserNotFoundException_WhenUserNotFound() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> watchlistService.createWatchlist(TEST_WATCHLIST_NAME, authentication));

    assertTrue(exception.getMessage().contains("User with email: " + TEST_EMAIL + " was not found"));
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, never()).save(any(Watchlist.class));
  }

  @Test
  @DisplayName("Delete watchlist successfully")
  void deleteWatchlist_ShouldReturnTrue_WhenValidInputsAndAuthorizedUser() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.of(testWatchlist));
    doNothing().when(watchlistRepository).delete(testWatchlist);

    // Act
    boolean result = watchlistService.deleteWatchlist(TEST_WATCHLIST_ID, authentication);

    // Assert
    assertTrue(result);
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
    verify(watchlistRepository, times(1)).delete(testWatchlist);
  }

  @Test
  @DisplayName("Delete watchlist should throw InvalidInputException when ID is null")
  void deleteWatchlist_ShouldThrowInvalidInputException_WhenIdIsNull() {
    // Arrange
    // No arrangement needed for null input

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> watchlistService.deleteWatchlist(null, authentication));

    assertEquals("Watchlist ID cannot be null", exception.getMessage());
    verify(userService, never()).findByEmail(anyString());
    verify(watchlistRepository, never()).findById(any());
  }

  @Test
  @DisplayName("Delete watchlist should throw UserNotFoundException when user not found")
  void deleteWatchlist_ShouldThrowUserNotFoundException_WhenUserNotFound() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> watchlistService.deleteWatchlist(TEST_WATCHLIST_ID, authentication));

    assertTrue(exception.getMessage().contains("User with email: " + TEST_EMAIL + " was not found"));
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, never()).findById(any());
  }

  @Test
  @DisplayName("Delete watchlist should throw ResourceNotFoundException when watchlist not found")
  void deleteWatchlist_ShouldThrowResourceNotFoundException_WhenWatchlistNotFound() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> watchlistService.deleteWatchlist(TEST_WATCHLIST_ID, authentication));

    assertTrue(exception.getMessage().contains("Watchlist with ID: " + TEST_WATCHLIST_ID + " was not found"));
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
    verify(watchlistRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Delete watchlist should throw UnauthorizedAccessException when user is not owner")
  void deleteWatchlist_ShouldThrowUnauthorizedAccessException_WhenUserIsNotOwner() {
    // Arrange
    User differentUser = User.builder().id(2L).email("different@example.com").build();
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(differentUser));
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.of(testWatchlist));

    // Act & Assert
    UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
            () -> watchlistService.deleteWatchlist(TEST_WATCHLIST_ID, authentication));

    assertEquals("User is not authorized to delete this watchlist", exception.getMessage());
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
    verify(watchlistRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Get all watchlists successfully")
  void getAllWatchlists_ShouldReturnWatchlistsWithAlertCounts_WhenUserExists() {
    // Arrange
    Stock testStock = Stock.builder()
            .companyName("Apple Inc.")
            .symbol("AAPL")
            .build();

    Alert alert1 = Alert.builder()
            .stock(testStock)
            .triggerType(TriggerType.TO_PRICE)
            .condition(ConditionType.ABOVE)
            .targetValue(150.00)
            .build();

    Alert alert2 = Alert.builder()
            .stock(testStock)
            .triggerType(TriggerType.PERCENTAGE_CHANGE_PRICE)
            .condition(ConditionType.BELOW)
            .targetValue(150.00)
            .build();

    Watchlist watchlist1 = Watchlist.builder()
            .id(1L)
            .watchlistName("Watchlist 1")
            .alerts(List.of(alert1, alert2))
            .build();
    Watchlist watchlist2 = Watchlist.builder()
            .id(2L)
            .watchlistName("Watchlist 2")
            .alerts(List.of(alert1))
            .build();

    testUser.setWatchlists(List.of(watchlist1, watchlist2));

    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    // Act
    List<Watchlist> result = watchlistService.getAllWatchlists(authentication);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(2, result.get(0).getAlertCount());
    assertEquals(1, result.get(1).getAlertCount());
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
  }

  @Test
  @DisplayName("Get all watchlists should return empty list when user has no watchlists")
  void getAllWatchlists_ShouldReturnEmptyList_WhenUserHasNoWatchlists() {
    // Arrange
    testUser.setWatchlists(List.of());
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    // Act
    List<Watchlist> result = watchlistService.getAllWatchlists(authentication);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
  }

  @Test
  @DisplayName("Get all watchlists should throw UserNotFoundException when user not found")
  void getAllWatchlists_ShouldThrowUserNotFoundException_WhenUserNotFound() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> watchlistService.getAllWatchlists(authentication));

    assertTrue(exception.getMessage().contains("User with email: " + TEST_EMAIL + " was not found"));
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
  }

  @Test
  @DisplayName("Get watchlist by ID successfully")
  void getWatchlist_ShouldReturnOptionalWatchlist_WhenIdProvided() {
    // Arrange
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.of(testWatchlist));

    // Act
    Optional<Watchlist> result = watchlistService.getWatchlist(TEST_WATCHLIST_ID);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(testWatchlist, result.get());
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
  }

  @Test
  @DisplayName("Get watchlist by ID should return empty optional when not found")
  void getWatchlist_ShouldReturnEmptyOptional_WhenWatchlistNotFound() {
    // Arrange
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.empty());

    // Act
    Optional<Watchlist> result = watchlistService.getWatchlist(TEST_WATCHLIST_ID);

    // Assert
    assertTrue(result.isEmpty());
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
  }

  @Test
  @DisplayName("Get watchlist with authentication successfully")
  void getWatchlistWithAuth_ShouldReturnWatchlist_WhenValidInputsAndAuthorizedUser() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.of(testWatchlist));

    // Act
    Watchlist result = watchlistService.getWatchlist(TEST_WATCHLIST_ID, authentication);

    // Assert
    assertNotNull(result);
    assertEquals(testWatchlist, result);
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
  }

  @Test
  @DisplayName("Get watchlist with authentication should throw InvalidInputException when ID is null")
  void getWatchlistWithAuth_ShouldThrowInvalidInputException_WhenIdIsNull() {
    // Arrange
    // No arrangement needed for null input

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> watchlistService.getWatchlist(null, authentication));

    assertEquals("Watchlist ID cannot be null", exception.getMessage());
    verify(userService, never()).findByEmail(anyString());
    verify(watchlistRepository, never()).findById(any());
  }

  @Test
  @DisplayName("Get watchlist with authentication should throw UserNotFoundException when user not found")
  void getWatchlistWithAuth_ShouldThrowUserNotFoundException_WhenUserNotFound() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> watchlistService.getWatchlist(TEST_WATCHLIST_ID, authentication));

    assertTrue(exception.getMessage().contains("User with email: " + TEST_EMAIL + " was not found"));
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, never()).findById(any());
  }

  @Test
  @DisplayName("Get watchlist with authentication should throw ResourceNotFoundException when watchlist not found")
  void getWatchlistWithAuth_ShouldThrowResourceNotFoundException_WhenWatchlistNotFound() {
    // Arrange
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> watchlistService.getWatchlist(TEST_WATCHLIST_ID, authentication));

    assertTrue(exception.getMessage().contains("Watchlist with ID: " + TEST_WATCHLIST_ID + " was not found"));
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
  }

  @Test
  @DisplayName("Get watchlist with authentication should throw UnauthorizedAccessException when user is not owner")
  void getWatchlistWithAuth_ShouldThrowUnauthorizedAccessException_WhenUserIsNotOwner() {
    // Arrange
    User differentUser = User.builder().id(2L).email("different@example.com").build();
    when(authentication.getName()).thenReturn(TEST_EMAIL);
    when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(differentUser));
    when(watchlistRepository.findById(TEST_WATCHLIST_ID)).thenReturn(Optional.of(testWatchlist));

    // Act & Assert
    UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
            () -> watchlistService.getWatchlist(TEST_WATCHLIST_ID, authentication));

    assertEquals("User is not authorized to delete this watchlist", exception.getMessage());
    verify(userService, times(1)).findByEmail(TEST_EMAIL);
    verify(watchlistRepository, times(1)).findById(TEST_WATCHLIST_ID);
  }
}