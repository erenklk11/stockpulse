package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.entity.Watchlist;
import com.erenkalkan.stockpulse.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/watchlist")
public class WatchlistController {

  private final WatchlistService watchlistService;

  @PostMapping("/create")
  public ResponseEntity<Watchlist> createWatchlist(@RequestParam String watchlistName,
                                                   Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED).body(watchlistService.createWatchlist(watchlistName, authentication));
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Map<String, Boolean>> deleteWatchlist(@PathVariable Long id, Authentication authentication) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("deleted", watchlistService.deleteWatchlist(id, authentication));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Watchlist> getWatchlist(@PathVariable Long id, Authentication authentication) {
    return ResponseEntity.ok(watchlistService.getWatchlist(id, authentication));
  }

  @GetMapping("/getAll")
  public ResponseEntity<List<Watchlist>> getAllWatchlists(Authentication authentication) {
    return ResponseEntity.ok(watchlistService.getAllWatchlists(authentication));
  }
}
