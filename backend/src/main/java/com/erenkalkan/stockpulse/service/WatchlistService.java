package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.*;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

  private final WatchlistRepository watchlistRepository;
  private final AlertService alertService;
  private final UserService userService;


  public Watchlist save(Watchlist watchlist) {
    try {
      log.debug("Attempting to save watchlist with name: {}", watchlist.getWatchlistName());
      return watchlistRepository.save(watchlist);
    } catch (Exception e) {
      log.error("Unexpected error while saving watchlist: {}", watchlist.getWatchlistName(), e);
      throw new DatabaseOperationException("Failed to save watchlist to database: " + e.getMessage(), e);
    }
  }

  public boolean delete(Watchlist watchlist) {
    try {
      log.debug("Deleting watchlist: (ID: {})", watchlist.getId());
      watchlistRepository.delete(watchlist);
      return true;
    } catch (Exception e) {
      log.error("Unexpected error while deleting watchlist with ID: {}", watchlist.getId(), e);
      throw new DatabaseOperationException("Failed to delete watchlist from database: " + e.getMessage(), e);
    }
  }

  public Watchlist createWatchlist(String watchlistName, Authentication authentication) {

    if (watchlistName == null || watchlistName.trim().isEmpty()) {
      throw new InvalidInputException("Watchlist name cannot be null or empty");
    }

    Optional<User> optionalUser = userService.findByEmail(authentication.getName());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("User with email: " + authentication.getName() + " was not found");
    }
    User user = optionalUser.get();

    Watchlist watchlist = Watchlist.builder()
            .watchlistName(watchlistName)
            .user(user)
            .build();

    return save(watchlist);
  }

  public boolean deleteWatchlist(Long id, Authentication authentication) {

    if (id == null) {
      throw new InvalidInputException("Watchlist ID cannot be null");
    }

    Optional<User> optionalUser = userService.findByEmail(authentication.getName());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("User with email: " + authentication.getName() + " was not found");
    }
    User user = optionalUser.get();

    Optional<Watchlist> optionalWatchlist = watchlistRepository.findById(id);
    if (optionalWatchlist.isEmpty()) {
      throw new ResourceNotFoundException("Watchlist with ID: " + id + " was not found");
    }
    Watchlist watchlist = optionalWatchlist.get();

    if (!watchlist.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException("User is not authorized to delete this watchlist");
    }

    return delete(watchlist);
  }

  public List<Watchlist> getAllWatchlists(Authentication authentication) {

    Optional<User> optionalUser = userService.findByEmail(authentication.getName());
    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("User with email: " + authentication.getName() + " was not found");
    }
    User user = optionalUser.get();

    List<Watchlist> watchlists = user.getWatchlists();
    if (watchlists.isEmpty() || watchlists == null) {
      return List.of();
    }

    // Set the alertCounts to be display at the watchlist section on the home page
    watchlists = watchlists.stream()
            .peek(watchlist -> watchlist.setAlertCount(watchlist.getAlerts().size()))
            .toList();

    return watchlists;
  }
}
