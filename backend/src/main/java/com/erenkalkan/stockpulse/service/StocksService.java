package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.StockDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocksService {

  private final RestClient restClient;

  @Value("${app.api.finnhub.url}")
  private String url;

  @Value("${app.api.finnhub.key}")
  private String key;

  private static final long CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000; // 1 day
  private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

  private static class CacheEntry {
    final StockDataDTO data;
    final long timestamp;

    CacheEntry(StockDataDTO data, long timestamp) {
      this.data = data;
      this.timestamp = timestamp;
    }
  }

  public StockDataDTO getStockData(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new InvalidInputException("Stock symbol cannot be null or empty");
    }

    CacheEntry cached = cache.get(symbol);
    long now = Instant.now().toEpochMilli();
    if (cached != null && (now - cached.timestamp) < CACHE_TTL_MILLIS) {
      return cached.data;
    }

    StockDataDTO data = fetchStockDataFromApi(symbol);

    cache.put(symbol, new CacheEntry(data, now));
    return data;
  }

  private StockDataDTO fetchStockDataFromApi(String symbol) {
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
