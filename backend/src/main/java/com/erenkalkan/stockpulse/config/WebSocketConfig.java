package com.erenkalkan.stockpulse.config;

import com.erenkalkan.stockpulse.service.websocket.AlertTriggerWebSocketHandler;
import com.erenkalkan.stockpulse.service.websocket.StockPriceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@RequiredArgsConstructor
@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

  @Value("${app.url}")
  private String APP_URL;

  private final StockPriceWebSocketHandler stockPriceWebSocketHandler;
  private final AlertTriggerWebSocketHandler alertTriggerWebSocketHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(stockPriceWebSocketHandler, "/live-prices").setAllowedOrigins(APP_URL);
    registry.addHandler(alertTriggerWebSocketHandler, "/alert-triggers").setAllowedOrigins(APP_URL);
  }
}
