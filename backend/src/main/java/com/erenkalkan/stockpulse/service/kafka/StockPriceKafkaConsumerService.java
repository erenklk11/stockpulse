package com.erenkalkan.stockpulse.service.kafka;

import com.erenkalkan.stockpulse.service.websocket.StockPriceWebSocketHandler;
import com.erenkalkan.stockpulse.model.dto.StockPriceDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockPriceKafkaConsumerService {

  private final StockPriceWebSocketHandler webSocketHandler;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @KafkaListener(topics = "stock-prices", groupId = "websocket-server-group")
  public void listen(StockPriceDTO stockPrice) {
    try {
      String jsonMessage = objectMapper.writeValueAsString(stockPrice);
      webSocketHandler.broadcast(jsonMessage);
    } catch (JsonProcessingException e) {
      log.error("Error serializing StockPrice object to JSON: {}", e.getMessage());
    }
  }
}
