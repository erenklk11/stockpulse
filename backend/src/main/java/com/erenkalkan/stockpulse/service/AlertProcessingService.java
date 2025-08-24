package com.erenkalkan.stockpulse.service;


import com.erenkalkan.stockpulse.model.dto.StockPriceDTO;
import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.enums.ConditionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertProcessingService {

  @Value("${app.kafka.topics-alertTriggers:alert-triggers")
  private String alertTriggersTopic;

  private final AlertService alertService;
  private final KafkaTemplate<String, Alert> alertKafkaTemplate;


  @KafkaListener(topics = "${app.kafka.topics.stockPrices:stock-prices}", groupId = "alert-processor-group")
  public void processStockPrice(StockPriceDTO priceUpdate) {
    log.info("Processing price update for {}: {}", priceUpdate.getSymbol(), priceUpdate.getPrice());

    List<Alert> alerts = alertService.findAllBySymbol(priceUpdate.getSymbol());

    for (Alert alert : alerts) {
      boolean conditionMet = false;
      if (((ConditionType.ABOVE.equals(alert.getCondition()) && priceUpdate.getPrice() > alert.getTargetValue()) ||
              (ConditionType.BELOW.equals(alert.getCondition()) && priceUpdate.getPrice() < alert.getTargetValue())) && !alert.isTriggered()) {
        {
          conditionMet = true;
        }

        if (conditionMet) {
          log.info("Alert triggered for user {}: {} is {} {}",
                  alert.getWatchlist().getUser(), alert.getStock().getSymbol(), alert.getCondition(), alert.getTargetValue());
          alertKafkaTemplate.send(alertTriggersTopic, alert);
          alert.setTriggered(true);
        }
      }
    }
  }
}
