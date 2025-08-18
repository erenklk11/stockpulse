package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.StockDTO;
import com.erenkalkan.stockpulse.model.dto.StockDataDTO;
import com.erenkalkan.stockpulse.model.dto.StockFinancialsDTO;
import com.erenkalkan.stockpulse.model.dto.StockRecommendationsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocksService {

  private final RestClient restClient;

  @Value("${app.api.finnhub.url}")
  private String finnhubUrl;
  @Value("${app.api.finnhub.key}")
  private String finnhubKey;

  @Value("${app.api.polygon.url}")
  private String polygonUrl;
  @Value("${app.api.polygon.key}")
  private String polygonKey;

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

  public StockDTO getStock(String symbol) {

    StockDataDTO stockDataDTO = fetchStockDataFromPolygonAPI(symbol);
    StockFinancialsDTO stockFinancialsDTO = fetchStockFinancials(stockDataDTO.getName());
    StockRecommendationsDTO stockRecommendationsDTO = fetchStockRecommendations(symbol);

    return StockDTO.builder()
            .data(stockDataDTO)
            .financials(stockFinancialsDTO)
            .recommendations(stockRecommendationsDTO)
            .build();
  }

  public StockDataDTO getStockDataForHomePage(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new InvalidInputException("Stock symbol cannot be null or empty");
    }

    CacheEntry cached = cache.get(symbol);
    long now = Instant.now().toEpochMilli();
    if (cached != null && (now - cached.timestamp) < CACHE_TTL_MILLIS) {
      return cached.data;
    }

    StockDataDTO data = fetchStockDataFromFinnhubAPI(symbol);

    cache.put(symbol, new CacheEntry(data, now));
    return data;
  }

  private StockDataDTO fetchStockDataFromFinnhubAPI(String symbol) {
    String apiUrl = finnhubUrl + "stock/profile2?symbol=" + symbol + "&token=" + finnhubKey;

    try {
      Map<String, Object> result = restClient.get()
              .uri(apiUrl)
              .retrieve()
              .body(Map.class);

      if (result != null && !result.isEmpty()) {

        String exchange = (String) result.get("exchange");
        String industry = (String) result.get("finnhubIndustry");
        String marketCap = result.get("marketCapitalization").toString();
        marketCap = convertMarketCapFromFinnhubAPIToStringValue(marketCap);

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

  // When pulling data from Polygon, we also get 'description'. Needed for StocksComponent
  private StockDataDTO fetchStockDataFromPolygonAPI(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new InvalidInputException("Stock symbol cannot be null or empty");
    }

    String apiUrl = polygonUrl + "v3/reference/tickers/" + symbol + "?apiKey=" + polygonKey;

    try {
      Map<String, Object> result = restClient.get()
              .uri(apiUrl)
              .retrieve()
              .body(Map.class);

      if (result != null && !result.isEmpty()) {

        Map<String, Object> results = (Map<String, Object>) result.get("results");
        Map<String, Object> branding = (Map<String, Object>) results.get("branding");

        String name = (String) results.get("name");
        String description = (String) results.get("description");
        String logoUrl = (String) branding.get("logo_url");
        logoUrl = logoUrl + "?apiKey=" + polygonKey;
        String exchange = (String) results.get("primary_exchange");
        String marketCap = results.get("market_cap").toString();
        marketCap = convertMarketCapFromPolygonAPIToStringValue(marketCap);

        return StockDataDTO.builder()
                .name(name)
                .description(description)
                .logoUrl(logoUrl)
                .exchange(exchange)
                .marketCap(marketCap)
                .build();
      }
      log.warn("Empty or null response from Polygon API for symbol: {}", symbol);
      return StockDataDTO.builder().build();

    } catch (Exception e) {
      throw new RestClientException("Failed to fetch stock data from Polygon API", e);
    }
  }

  // Ignoring exceptions because Polygon's API endpoint is currently experimental.
  private StockFinancialsDTO fetchStockFinancials(String companyName) {
    if (companyName == null || companyName.trim().isEmpty()) {
      throw new InvalidInputException("Company name cannot be null or empty");
    }

    String apiUrl = polygonUrl + "vX/reference/financials?company_name=" + companyName +
            "&order=desc&limit=1&sort=filing_date&apiKey=" + polygonKey;

    try {
      Map<String, Object> result = restClient.get()
              .uri(apiUrl)
              .retrieve()
              .body(Map.class);

      if (result != null && !result.isEmpty()) {

        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        if (results != null || results.size() != 0) {

          Map<String, Object> firstResult = results.get(0);
          Map<String, Object> financials = (Map<String, Object>) firstResult.get("financials");

          if (financials == null) {
            throw new RuntimeException("No financials data found");
          }

          // Extract nested financial statement data
          Map<String, Object> balanceSheet = (Map<String, Object>) financials.get("balance_sheet");
          Map<String, Object> incomeStatement = (Map<String, Object>) financials.get("income_statement");
          Map<String, Object> cashFlowStatement = (Map<String, Object>) financials.get("cash_flow_statement");

          StockFinancialsDTO dto = new StockFinancialsDTO();

          // Balance Sheet fields
          if (balanceSheet != null) {
            dto.setAssets(extractLongValue(balanceSheet, "assets"));
            dto.setLiabilities(extractLongValue(balanceSheet, "liabilities"));
            dto.setEquity(extractLongValue(balanceSheet, "equity"));
            dto.setCurrentAssets(extractLongValue(balanceSheet, "current_assets"));
            dto.setCurrentLiabilities(extractLongValue(balanceSheet, "current_liabilities"));
          }

          // Income Statement fields
          if (incomeStatement != null) {
            dto.setRevenues(extractLongValue(incomeStatement, "revenues"));
            dto.setGrossProfit(extractLongValue(incomeStatement, "gross_profit"));
            dto.setOperatingIncome(extractLongValue(incomeStatement, "operating_income_loss"));
            dto.setNetIncome(extractLongValue(incomeStatement, "net_income_loss"));
            dto.setBasicEarningsPerShare(extractDoubleValue(incomeStatement, "basic_earnings_per_share"));
            dto.setDilutedEarningsPerShare(extractDoubleValue(incomeStatement, "diluted_earnings_per_share"));
          }

          // Cash Flow Statement fields
          if (cashFlowStatement != null) {
            dto.setNetCashFlowFromOperatingActivities(extractLongValue(cashFlowStatement, "net_cash_flow_from_operating_activities"));
            dto.setNetCashFlowFromInvestingActivities(extractLongValue(cashFlowStatement, "net_cash_flow_from_investing_activities"));
            dto.setNetCashFlowFromFinancingActivities(extractLongValue(cashFlowStatement, "net_cash_flow_from_financing_activities"));
            dto.setNetCashFlow(extractLongValue(cashFlowStatement, "net_cash_flow"));
          }
          return dto;
        }
      }
      log.warn("Empty or null response from Polygon API for company: {}", companyName);
      return StockFinancialsDTO.builder().build();

    } catch (Exception e) {
        return null;
    }
  }

  private StockRecommendationsDTO fetchStockRecommendations(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new InvalidInputException("Stock symbol cannot be null or empty");
    }
    String apiUrl = finnhubUrl + "stock/recommendation?symbol=" + symbol + "&token=" + finnhubKey;

    try {
      List<Map<String, Object>> result = restClient.get()
              .uri(apiUrl)
              .retrieve()
              .body(List.class);

      if (result != null && !result.isEmpty()) {

        // Get the most recent recommendation only
        Map<String, Object> results = result.get(0);

        int strongBuy = (Integer) results.get("strongBuy");
        int buy = (Integer) results.get("buy");
        int hold = (Integer) results.get("hold");
        int sell = (Integer) results.get("sell");
        int strongSell = (Integer) results.get("strongSell");

        return StockRecommendationsDTO.builder()
                .strongBuy(strongBuy)
                .buy(buy)
                .hold(hold)
                .sell(sell)
                .strongBuy(strongSell)
                .build();
      }
      log.warn("Empty or null response from Polygon API for symbol: {}", symbol);
      return StockRecommendationsDTO.builder().build();
    }
    catch (Exception e) {
      throw new RestClientException("Failed to fetch stock recommendations from Finnhub API", e);
    }
  }

  // Helper method to extract long values from nested value objects
  private long extractLongValue(Map<String, Object> statement, String fieldName) {
    Map<String, Object> fieldData = (Map<String, Object>) statement.get(fieldName);
    if (fieldData != null && fieldData.containsKey("value")) {
      Object value = fieldData.get("value");
      if (value instanceof Number) {
        return ((Number) value).longValue();
      }
    }
    return 0L; // Default value if field not found
  }

  // Helper method to extract double values from nested value objects
  private double extractDoubleValue(Map<String, Object> statement, String fieldName) {
    Map<String, Object> fieldData = (Map<String, Object>) statement.get(fieldName);
    if (fieldData != null && fieldData.containsKey("value")) {
      Object value = fieldData.get("value");
      if (value instanceof Number) {
        return ((Number) value).doubleValue();
      }
    }
    return 0.0; // Default value if field not found
  }

  private String convertMarketCapFromFinnhubAPIToStringValue(String marketCap) {
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

  private String convertMarketCapFromPolygonAPIToStringValue(String marketCap) {
    Double doubleMarketCap = Double.parseDouble(marketCap); // Can handle scientific notation
    Long longMarketCap = doubleMarketCap.longValue(); // Convert to long for comparison

    if (longMarketCap >= 1_000_000_000_000L) {
      Double trillions = longMarketCap / 1_000_000_000_000.0;
      Double truncated = Math.floor(trillions * 10) / 10.0;
      return truncated.toString() + "T";
    }

    if (longMarketCap >= 1_000_000_000L) {
      Double billions = longMarketCap / 1_000_000_000.0;
      Double truncated = Math.floor(billions * 10) / 10.0;
      return truncated.toString() + "B";
    }

    Double millions = longMarketCap / 1_000_000.0;
    Double truncated = Math.floor(millions * 10) / 10.0;
    return truncated.toString() + "M";
  }
}
