package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AlertRepositoryTest {

  @Autowired
  private AlertRepository alertRepository;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private WatchlistRepository watchlistRepository;
  @Autowired
  private UserRepository userRepository;

  private Stock stock;
  private Watchlist watchlist;
  private User user;

  @BeforeEach
  void setUp() {
    alertRepository.deleteAll();
    stockRepository.deleteAll();
    watchlistRepository.deleteAll();

    stock = Stock.builder()
            .companyName("Apple Inc.")
            .symbol("AAPL")
            .build();
    stock = stockRepository.save(stock);

    user = User.builder()
            .firstName("Bruce")
            .email("test@email.com")
            .password("password")
            .build();
    user = userRepository.save(user);

    watchlist = Watchlist.builder()
            .watchlistName("Test Watchlist")
            .user(user)
            .build();
    watchlist = watchlistRepository.save(watchlist);
  }

  @Test
  void findAllByStock_WhenAlertsExist_ShouldReturnAlerts() {
    // Arrange
    Alert alert1 = Alert.builder()
            .stock(stock)
            .triggerType(TriggerType.PERCENTAGE_CHANGE_PRICE)
            .percentageValue(5.00)
            .targetValue(200.00)
            .condition(ConditionType.ABOVE)
            .watchlist(watchlist)
            .isTriggered(false)
            .build();
    Alert alert2 = Alert.builder()
            .stock(stock)
            .triggerType(TriggerType.TO_PRICE)
            .targetValue(150.00)
            .condition(ConditionType.BELOW)
            .watchlist(watchlist)
            .isTriggered(false)
            .build();
    alertRepository.save(alert1);
    alertRepository.save(alert2);

    // Act
    List<Alert> result = alertRepository.findAllByStock(stock);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(a -> a.getStock().getId().equals(stock.getId())));
  }

  @Test
  void findAllByStock_WhenNoAlertsExist_ShouldReturnEmpty() {
    // Act
    List<Alert> result = alertRepository.findAllByStock(stock);

    // Assert
    assertTrue(result.isEmpty());
  }
}