package com.erenkalkan.stockpulse.service.websocket;

import com.erenkalkan.stockpulse.service.StockWebSocketService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockPriceWebSocketHandler extends TextWebSocketHandler {

  private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
  private final StockWebSocketService stockWebSocketService;
  private final ObjectMapper objectMapper = new ObjectMapper();

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
    Set<WebSocketSession> currentSessions = new HashSet<>(sessions);
    for (WebSocketSession session : currentSessions) {
      if (session.isOpen()) {
        try {
          String response = String.format("{\"type\":\"price_update\",\"data\":%s}", message);
          session.sendMessage(new TextMessage(response));
        } catch (IOException e) {
          log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
          sessions.remove(session);
        }
      }
      else {
        sessions.remove(session);
      }
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    try {
      String payload = message.getPayload();

      JsonNode messageNode = objectMapper.readTree(payload);
      String messageType = messageNode.get("type").asText();

      switch (messageType) {
        case "subscribe":
          handleSubscriptionRequest(session, messageNode);
          break;
        case "unsubscribe":
          handleUnsubscriptionRequest(session, messageNode);
          break;
        case "ping":
          handlePingRequest(session);
          break;
        default:
          sendErrorToSession(session, "Unknown message type: " + messageType);
      }

    } catch (Exception e) {
      log.error("Error processing message from session {}: {}", session.getId(), e.getMessage());
      sendErrorToSession(session, "Error processing message: " + e.getMessage());
    }
  }

  private void handleSubscriptionRequest(WebSocketSession session, JsonNode messageNode) {
    try {
      List<String> symbols = new ArrayList<>();

      if (messageNode.has("symbol")) {
        symbols.add(messageNode.get("symbol").asText());
      }
      else if (messageNode.has("symbols")) {
        JsonNode symbolsNode = messageNode.get("symbols");
        if (symbolsNode.isArray()) {
          for (JsonNode symbolNode : symbolsNode) {
            symbols.add(symbolNode.asText());
          }
        }
      }

      if (symbols.isEmpty()) {
        sendErrorToSession(session, "No symbols provided for subscription");
        return;
      }

      List<String> validSymbols = symbols.stream()
              .filter(symbol -> symbol != null && !symbol.trim().isEmpty())
              .map(String::trim)
              .map(String::toUpperCase)
              .distinct()
              .collect(Collectors.toList());

      if (validSymbols.isEmpty()) {
        sendErrorToSession(session, "No valid symbols provided");
        return;
      }

      stockWebSocketService.addSymbols(validSymbols);

      String response = String.format(
              "{\"type\":\"subscribed\",\"symbols\":%s,\"message\":\"Successfully subscribed to symbols\"}",
              objectMapper.writeValueAsString(validSymbols)
      );
      sendToSession(session, response);

      log.info("Session {} subscribed to symbols: {}", session.getId(), validSymbols);

    } catch (Exception e) {
      log.error("Error handling subscription request", e);
      sendErrorToSession(session, "Error processing subscription request");
    }
  }

  private void handleUnsubscriptionRequest(WebSocketSession session, JsonNode messageNode) {
    // TODO: Implement unsubscription logic if needed
    sendToSession(session, "{\"type\":\"info\",\"message\":\"Unsubscription not yet implemented\"}");
  }

  private void handlePingRequest(WebSocketSession session) {
    sendToSession(session, "{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}");
  }

  private void sendToSession(WebSocketSession session, String message) {
    if (session.isOpen()) {
      try {
        session.sendMessage(new TextMessage(message));
      } catch (IOException e) {
        log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
      }
    }
  }

  private void sendErrorToSession(WebSocketSession session, String errorMessage) {
    try {
      String errorResponse = String.format(
              "{\"type\":\"error\",\"message\":\"%s\"}",
              errorMessage.replace("\"", "\\\"")
      );
      sendToSession(session, errorResponse);
    } catch (Exception e) {
      log.error("Error sending error message to session {}: {}", session.getId(), e.getMessage());
    }
  }
}