package com.erenkalkan.stockpulse.config;

import com.erenkalkan.stockpulse.service.websocket.AlertTriggerWebSocketHandler;
import com.erenkalkan.stockpulse.service.websocket.StockPriceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@RequiredArgsConstructor
@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

  private final StockPriceWebSocketHandler stockPriceWebSocketHandler;
  private final AlertTriggerWebSocketHandler alertTriggerWebSocketHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    // TODO: Change to frontend url in for production
    registry.addHandler(stockPriceWebSocketHandler, "/live-prices").setAllowedOrigins("*");
    registry.addHandler(alertTriggerWebSocketHandler, "/alert-triggers").setAllowedOrigins("*");
  }
}
