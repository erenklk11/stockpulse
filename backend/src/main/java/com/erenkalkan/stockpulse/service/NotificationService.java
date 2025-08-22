package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.service.websocket.AlertTriggerWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final AlertTriggerWebSocketHandler webSocketHandler;

  public void sendAlertNotification(Alert alert) {
    String message = String.format("Alert for %s: Price is now %s your target of %.2f",
            alert.getStock().getSymbol(), alert.getCondition().toString().toLowerCase(), alert.getTargetValue());
    try {
      webSocketHandler.broadcast(alert.getWatchlist().getUser(), message);
      log.info("Sent WebSocket notification to user {}", alert.getWatchlist().getUser().getEmail());
    } catch (Exception e) {
      log.error("Failed to send WebSocket notification to user {}", alert.getWatchlist().getUser().getEmail(), e);
    }
  }
}

