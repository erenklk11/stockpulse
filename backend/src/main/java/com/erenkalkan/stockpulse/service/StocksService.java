package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.StockDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocksService {

  private final RestClient restClient;

  @Value("${app.api.finnhub.url}")
  private String url;

  @Value("${app.api.finnhub.key}")
  private String key;

  public StockDataDTO getStockData(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new InvalidInputException("Stock symbol cannot be null or empty");
    }

    String apiUrl = url + "stock/profile2?symbol=" + symbol + "&token=" + key;

    try {
      Map<String, Object> result = restClient.get()
              .uri(apiUrl)
              .retrieve()
              .body(Map.class);

      if (result != null && !result.isEmpty()) {

        String exchange = (String) result.get("exchange");
        String industry = (String) result.get("finnhubIndustry");
        String marketCap = result.get("marketCapitalization").toString();
        marketCap = convertMarketCapToStringValue(marketCap);

        return StockDataDTO.builder()
                .exchange(exchange)
                .marketCap(marketCap)
                .industry(industry)
                .build();
      }

      log.warn("Empty or null response from Finnhub API for symbol: {}", symbol);
      return StockDataDTO.builder().build();

    } catch (NumberFormatException e) {
      throw new RestClientException("Invalid market capitalization format from API", e);
    } catch (Exception e) {
      throw new RestClientException("Failed to fetch stock data from Finnhub API", e);
    }
  }

  private String convertMarketCapToStringValue(String marketCap) {
    Double millionsMarketCap = Double.parseDouble(marketCap); // Receiving already in millions from the Finnhub API

    if (millionsMarketCap >= 1_000_000) {
      Double trillions = millionsMarketCap / 1_000_000.0;
      Double truncated = Math.floor(trillions * 10) / 10.0;
      return truncated.toString() + "T";
    }

    if (millionsMarketCap >= 1_000) {
      Double billions = millionsMarketCap / 1_000.0;
      Double truncated = Math.floor(billions * 10) / 10.0;
      return truncated.toString() + "B";
    }

    Double truncated = Math.floor(millionsMarketCap * 10) / 10.0;
    return truncated.toString() + "M";
  }
}
