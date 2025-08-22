package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.*;
import com.erenkalkan.stockpulse.model.dto.CreateAlertRequestDTO;
import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
import com.erenkalkan.stockpulse.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

  private final AlertRepository alertRepository;
  private final StocksService stocksService;
  private final WatchlistService watchlistService;
  private final UserService userService;


  public Alert save(Alert alert) {
    try {
      log.debug("Attempting to save alert with symbol: {}", alert.getStock().getSymbol());
      return alertRepository.save(alert);
    } catch (Exception e) {
      log.error("Unexpected error while saving alert: {}", alert.getStock().getSymbol(), e);
      throw new DatabaseOperationException("Failed to save alert to database: " + e.getMessage(), e);
    }
  }

  public boolean delete(Alert alert) {
    try {
      log.debug("Deleting alert: (ID: {})", alert.getId());
      alertRepository.delete(alert);
      return true;
    } catch (Exception e) {
      log.error("Unexpected error while deleting alert with ID: {}", alert.getId(), e);
      throw new DatabaseOperationException("Failed to delete alert from database: " + e.getMessage(), e);
    }
  }

  public List<Alert> findAllBySymbol(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new InvalidInputException("Stock symbol cannot be null");
    }

    Optional<Stock> stock = stocksService.findBySymbol(symbol);
    if (stock.isEmpty()) {
      log.warn("Stock {} could not have been found. Returning empty Alerts list", symbol);
      return List.of();
    }
    return alertRepository.findAllByStock(stock.get());
  }

  public boolean createAlert(CreateAlertRequestDTO request, Authentication authentication) {

    log.info(request.toString());
    Optional<User> optionalUser = userService.findByEmail(authentication.getName());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("User with email: " + authentication.getName() + " was not found");
    }
    User user = optionalUser.get();

    Optional<Watchlist> optionalWatchlist = watchlistService.getWatchlist(request.getWatchlistId());
    if (optionalWatchlist.isEmpty()) {
      throw new ResourceNotFoundException(String.format("Watchlist with id %s does not exist", request.getWatchlistId()));
    }
    Watchlist watchlist = optionalWatchlist.get();

    stocksService.save(request.getStock());

    Alert alert;
    // If TriggerType.PERCENTAGE_CHANGE_PRICE, the alert value is the percentage value.
    // If TriggerType.TO_PRICE, the alert value is the target value the user wants the stock to rise/drop to.
    if (request.getTriggerType() == TriggerType.PERCENTAGE_CHANGE_PRICE) {
      alert = Alert.builder()
              .stock(request.getStock())
              .triggerType(request.getTriggerType())
              .percentageValue(request.getPercentageValue())
              .targetValue(request.getTargetValue())
              .watchlist(watchlist)
              .build();
    }
    else {
      alert = Alert.builder()
              .stock(request.getStock())
              .triggerType(request.getTriggerType())
              .targetValue(request.getTargetValue())
              .watchlist(watchlist)
              .build();
    }

    save(alert);

    return true;
  }

  public boolean deleteAlert(Long id, Authentication authentication) {
    if (id == null) {
      throw new InvalidInputException("Alert ID cannot be null");
    }

    Optional<User> optionalUser = userService.findByEmail(authentication.getName());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("User with email: " + authentication.getName() + " was not found");
    }
    User user = optionalUser.get();

    Optional<Alert> optionalAlert = alertRepository.findById(id);
    if (optionalAlert.isEmpty()) {
      throw new ResourceNotFoundException(String.format("Alert with id %s does not exist", id));
    }
    Alert alert = optionalAlert.get();

    if (!alert.getWatchlist().getUser().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException("User is not authorized to delete this alert");
    }

    return delete(alert);
  }
}
