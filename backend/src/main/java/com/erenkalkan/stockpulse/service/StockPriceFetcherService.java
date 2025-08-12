package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.dto.StockPriceDTO;
import com.erenkalkan.stockpulse.service.kafka.StockPriceKafkaPublisherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceFetcherService {

  @Value("${app.api.finnhub.websocketUrl}")
  private String websocketUrl;

  @Value("${app.api.finnhub.key}")
  private String finnhubApiKey;

  private final StockPriceKafkaPublisherService kafkaPublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private WebSocketSession currentSession;
  private volatile boolean shouldReconnect = true;
  private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();

  @PostConstruct
  public void initialize() {
    log.info("StockPriceFetcherService initialized - establishing WebSocket connection");
    connectToFinnhub();
  }

  public void addSymbols(List<String> symbols) {
    if (symbols == null || symbols.isEmpty()) {
      return;
    }

    subscribedSymbols.addAll(symbols);

    if (currentSession != null && currentSession.isOpen()) {
      try {
        for (String symbol : symbols) {
          String subscribeMessage = String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", symbol);
          currentSession.sendMessage(new TextMessage(subscribeMessage));
          log.info("Subscribed to symbol: {}", symbol);
        }
      } catch (Exception e) {
        log.error("Error adding symbols", e);
      }
    }
  }

  public void removeSymbols(List<String> symbols) {
    if (symbols == null || symbols.isEmpty()) {
      return;
    }

    subscribedSymbols.removeAll(symbols);

    if (currentSession != null && currentSession.isOpen()) {
      try {
        for (String symbol : symbols) {
          String unsubscribeMessage = String.format("{\"type\":\"unsubscribe\",\"symbol\":\"%s\"}", symbol);
          currentSession.sendMessage(new TextMessage(unsubscribeMessage));
          log.info("Unsubscribed from symbol: {}", symbol);
        }
      } catch (Exception e) {
        log.error("Error removing symbols", e);
      }
    }
  }

  public Set<String> getSubscribedSymbols() {
    return new HashSet<>(subscribedSymbols);
  }

  private void connectToFinnhub() {
    WebSocketClient client = new StandardWebSocketClient();
    String uri = websocketUrl + "?token=" + finnhubApiKey;

    try {
      log.info("Connecting to Finnhub WebSocket: {}", websocketUrl);
      client.execute(new FinnhubWebSocketHandler(), uri);
    } catch (Exception e) {
      log.error("Failed to connect to Finnhub WebSocket", e);
      scheduleReconnect();
    }
  }

  private void scheduleReconnect() {
    if (shouldReconnect) {
      log.info("Scheduling reconnection in 10 seconds");
      scheduler.schedule(this::connectToFinnhub, 10, TimeUnit.SECONDS);
    }
  }

  public void stopService() {
    shouldReconnect = false;
    if (currentSession != null && currentSession.isOpen()) {
      try {
        currentSession.close();
      } catch (Exception e) {
        log.error("Error closing WebSocket session", e);
      }
    }
    scheduler.shutdown();
  }

  private class FinnhubWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      log.info("WebSocket connection established");
      currentSession = session;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
      try {
        String payload = message.getPayload();

        if (payload.equals("{\"type\":\"ping\"}")) {
          // Respond to ping with pong
          session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
          return;
        }

        // Parse the incoming JSON message from Finnhub
        JsonNode rootNode = objectMapper.readTree(payload);
        String type = rootNode.path("type").asText();

        if ("trade".equals(type)) {
          JsonNode dataArray = rootNode.path("data");
          if (dataArray.isArray()) {
            for (JsonNode tradeData : dataArray) {
              processTradeData(tradeData);
            }
          }
        }

      } catch (Exception e) {
        log.error("Error processing WebSocket message: {}", message.getPayload(), e);
      }
    }

    private void processTradeData(JsonNode tradeData) {
      try {
        String symbol = tradeData.path("s").asText();
        double price = tradeData.path("p").asDouble();
        long timestamp = tradeData.path("t").asLong();

        if (symbol.isEmpty() || price <= 0 || timestamp <= 0) {
          log.warn("Invalid trade data received: {}", tradeData);
          return;
        }

        StockPriceDTO stockPriceDTO = new StockPriceDTO(symbol, price, timestamp);

        kafkaPublisher.publishStockPrice(stockPriceDTO);

      } catch (Exception e) {
        log.error("Error processing trade data: {}", tradeData, e);
      }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
      log.error("WebSocket transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
      log.warn("WebSocket connection closed with status: {} - {}", status.getCode(), status.getReason());
      currentSession = null;

      if (shouldReconnect) {
        scheduleReconnect();
      }
    }
  }
}
