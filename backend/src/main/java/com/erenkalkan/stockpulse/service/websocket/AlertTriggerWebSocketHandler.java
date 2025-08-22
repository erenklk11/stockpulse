package com.erenkalkan.stockpulse.service.websocket;

import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AlertTriggerWebSocketHandler extends TextWebSocketHandler {

  private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
  private final JwtService jwtService;


  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.add(session);

    String email = getEmailFromJwtCookie(session);
    if (email != null) {
      session.getAttributes().put("email", email);
      log.info("New WebSocket connection established for user {}: {}", email, session.getId());
    } else {
      log.warn("WebSocket connection established without valid JWT: {}", session.getId());
      session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
      return;
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session);
    log.info("WebSocket connection closed: {}", session.getId());
  }

  public void broadcast(User user, String message) {
    if (user == null) {
      log.warn("Cannot broadcast message: user is null");
      return;
    }

    String email = user.getUsername();
    if (email == null) {
      log.warn("Cannot broadcast message: username is null");
      return;
    }

    List<WebSocketSession> sessionsToRemove = new ArrayList<>();

    synchronized (sessions) {
      for (WebSocketSession session : sessions) {
        try {
          if (session.isOpen()) {
            String sessionUsername = (String) session.getAttributes().get("email");

            if (email.equals(sessionUsername)) {
              session.sendMessage(new TextMessage(message));
              log.debug("Alert message sent to user {} via session {}", email, session.getId());
            }
          } else {
            sessionsToRemove.add(session);
          }
        } catch (IOException e) {
          log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
          sessionsToRemove.add(session);
        } catch (Exception e) {
          log.error("Unexpected error while broadcasting to session {}: {}", session.getId(), e.getMessage());
        }
      }
      sessions.removeAll(sessionsToRemove);
    }
    if (sessionsToRemove.size() > 0) {
      log.info("Removed {} closed/failed sessions during broadcast", sessionsToRemove.size());
    }
  }

  private String getEmailFromJwtCookie(WebSocketSession session) {
    try {
      HttpHeaders headers = session.getHandshakeHeaders();
      List<String> cookieHeaders = headers.get("Cookie");

      if (cookieHeaders == null || cookieHeaders.isEmpty()) {
        return null;
      }

      for (String cookieHeader : cookieHeaders) {
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
          String[] parts = cookie.trim().split("=", 2);
          if (parts.length == 2 && "auth-token".equals(parts[0].trim())) {
            String token = parts[1].trim();

            if (jwtService.isTokenValid(token)) {
              return jwtService.extractUsername(token);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error extracting username from JWT cookie: {}", e.getMessage());
    }
    return null;
  }
}
