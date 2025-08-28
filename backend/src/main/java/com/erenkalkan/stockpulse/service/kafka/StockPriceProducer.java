package com.erenkalkan.stockpulse.service.kafka;

import com.erenkalkan.stockpulse.model.dto.StockPriceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceProducer {

    private final KafkaTemplate<String, StockPriceDTO> stockPriceKafkaTemplate;

    @Value("stock-prices")
    private String stockPricesTopic;


    public void publishStockPrice(StockPriceDTO stockPriceDTO) {
        if (stockPriceDTO == null) {
            log.warn("Attempted to publish null stock price");
            return;
        }

        if (stockPriceDTO.getSymbol() == null || stockPriceDTO.getSymbol().trim().isEmpty()) {
            log.warn("Attempted to publish stock price with null or empty symbol");
            return;
        }

        try {
            CompletableFuture<SendResult<String, StockPriceDTO>> future =
                stockPriceKafkaTemplate.send(stockPricesTopic, stockPriceDTO.getSymbol(), stockPriceDTO);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish stock price to Kafka for symbol: {} at price: {}",
                            stockPriceDTO.getSymbol(), stockPriceDTO.getPrice(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Unexpected error publishing stock price for symbol: {}", stockPriceDTO.getSymbol(), e);
        }
    }

    public String getTopicName() {
        return stockPricesTopic;
    }
}
