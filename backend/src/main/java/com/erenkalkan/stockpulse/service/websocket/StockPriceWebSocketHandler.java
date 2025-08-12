package com.erenkalkan.stockpulse.service.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class StockPriceWebSocketHandler extends TextWebSocketHandler {

  private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.add(session);
    log.info("New WebSocket connection established: {}", session.getId());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session);
    log.info("WebSocket connection closed: {}", session.getId());
  }

  public void broadcast(String message) {
    // Iterate over a copy of the sessions to avoid ConcurrentModificationException
    Set<WebSocketSession> currentSessions = new HashSet<>(sessions);
    for (WebSocketSession session : currentSessions) {
      if (session.isOpen()) {
        try {
          session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
          log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
        }
      }
    }
  }
}