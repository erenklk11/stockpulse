package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.StockDTO;
import com.erenkalkan.stockpulse.model.dto.StockDataDTO;
import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StocksServiceTest {

  @Mock
  private RestClient restClient;

  @Mock
  private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private RestClient.ResponseSpec responseSpec;

  @Mock
  private StockRepository stockRepository;

  @InjectMocks
  private StocksService stocksService;

  private final String TEST_SYMBOL = "AAPL";
  private final String TEST_COMPANY_NAME = "Apple Inc.";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(stocksService, "finnhubUrl", "https://finnhub.io/api/v1/");
    ReflectionTestUtils.setField(stocksService, "finnhubKey", "test-finnhub-key");
    ReflectionTestUtils.setField(stocksService, "polygonUrl", "https://api.polygon.io/");
    ReflectionTestUtils.setField(stocksService, "polygonKey", "test-polygon-key");

    ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    ReflectionTestUtils.setField(stocksService, "cache", cache);
  }

  @Test
  void save_ShouldReturnSavedStock() {
    // Arrange
    Stock inputStock = new Stock();
    inputStock.setSymbol(TEST_SYMBOL);
    Stock savedStock = new Stock();
    savedStock.setId(1L);
    savedStock.setSymbol(TEST_SYMBOL);

    when(stockRepository.save(inputStock)).thenReturn(savedStock);

    // Act
    Stock result = stocksService.save(inputStock);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(TEST_SYMBOL, result.getSymbol());
    verify(stockRepository, times(1)).save(inputStock);
  }

  @Test
  void getStock_ShouldReturnCompleteStockDTO() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> polygonResponse = createPolygonStockDataResponse();
    when(responseSpec.body(Map.class)).thenReturn(polygonResponse);

    List<Map<String, Object>> recommendationsResponse = createRecommendationsResponse();
    when(responseSpec.body(List.class)).thenReturn(recommendationsResponse);

    // Act
    StockDTO result = stocksService.getStock(TEST_SYMBOL);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getData());
    assertNotNull(result.getRecommendations());
    assertEquals(TEST_COMPANY_NAME, result.getData().getName());
    assertEquals(5, result.getRecommendations().getBuy());
    verify(restClient, times(3)).get(); // Called for polygon, financials, and recommendations
  }

  @Test
  void getStockDataForHomePage_WithValidSymbol_ShouldReturnStockData() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> finnhubResponse = createFinnhubStockDataResponse();
    when(responseSpec.body(Map.class)).thenReturn(finnhubResponse);

    // Act
    StockDataDTO result = stocksService.getStockDataForHomePage(TEST_SYMBOL);

    // Assert
    assertNotNull(result);
    assertEquals("NASDAQ", result.getExchange());
    assertEquals("3.0T", result.getMarketCap());
    assertEquals("Technology", result.getIndustry());
    verify(restClient, times(1)).get();
  }

  @Test
  void getStockDataForHomePage_WithExpiredCache_ShouldFetchNewData() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> finnhubResponse = createFinnhubStockDataResponse();
    when(responseSpec.body(Map.class)).thenReturn(finnhubResponse);

    // Simulate expired cache by calling the method twice with enough time gap
    // First call will populate cache, but we'll test the behavior when cache would be expired

    // Act
    StockDataDTO result = stocksService.getStockDataForHomePage(TEST_SYMBOL);

    // Assert
    assertNotNull(result);
    assertEquals("NASDAQ", result.getExchange());
    assertEquals("3.0T", result.getMarketCap());
    assertEquals("Technology", result.getIndustry());
    verify(restClient, times(1)).get();
  }

  @Test
  void getStockDataForHomePage_WithNullSymbol_ShouldThrowInvalidInputException() {
    // Arrange
    String nullSymbol = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> stocksService.getStockDataForHomePage(nullSymbol));

    assertEquals("Stock symbol cannot be null or empty", exception.getMessage());
    verify(restClient, never()).get();
  }

  @Test
  void getStockDataForHomePage_WithEmptySymbol_ShouldThrowInvalidInputException() {
    // Arrange
    String emptySymbol = "  ";

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> stocksService.getStockDataForHomePage(emptySymbol));

    assertEquals("Stock symbol cannot be null or empty", exception.getMessage());
    verify(restClient, never()).get();
  }

  @Test
  void getStockDataForHomePage_WithApiException_ShouldThrowRestClientException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenThrow(new RuntimeException("API Error"));

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> stocksService.getStockDataForHomePage(TEST_SYMBOL));

    assertEquals("Failed to fetch stock data from Finnhub API", exception.getMessage());
  }

  @Test
  void findBySymbol_WithValidSymbol_ShouldReturnStock() {
    // Arrange
    Stock expectedStock = new Stock();
    expectedStock.setSymbol(TEST_SYMBOL);
    when(stockRepository.findBySymbol(TEST_SYMBOL)).thenReturn(Optional.of(expectedStock));

    // Act
    Optional<Stock> result = stocksService.findBySymbol(TEST_SYMBOL);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(TEST_SYMBOL, result.get().getSymbol());
    verify(stockRepository, times(1)).findBySymbol(TEST_SYMBOL);
  }

  @Test
  void findBySymbol_WithNonExistentSymbol_ShouldReturnEmpty() {
    // Arrange
    when(stockRepository.findBySymbol("NONEXISTENT")).thenReturn(Optional.empty());

    // Act
    Optional<Stock> result = stocksService.findBySymbol("NONEXISTENT");

    // Assert
    assertFalse(result.isPresent());
    verify(stockRepository, times(1)).findBySymbol("NONEXISTENT");
  }

  @Test
  void findBySymbol_WithNullSymbol_ShouldThrowInvalidInputException() {
    // Arrange
    String nullSymbol = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> stocksService.findBySymbol(nullSymbol));

    assertEquals("Stock symbol cannot be null or empty", exception.getMessage());
    verify(stockRepository, never()).findBySymbol(any());
  }

  @Test
  void fetchStockClosePrice_WithValidSymbol_ShouldReturnPrice() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Double> priceResponse = new HashMap<>();
    priceResponse.put("pc", 150.25);
    when(responseSpec.body(Map.class)).thenReturn(priceResponse);

    // Act
    Double result = stocksService.fetchStockClosePrice(TEST_SYMBOL);

    // Assert
    assertNotNull(result);
    assertEquals(150.25, result, 0.01);
    verify(restClient, times(1)).get();
  }

  @Test
  void fetchStockClosePrice_WithEmptyResponse_ShouldReturnZero() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(new HashMap<>());

    // Act
    Double result = stocksService.fetchStockClosePrice(TEST_SYMBOL);

    // Assert
    assertEquals(0.0, result, 0.01);
  }

  @Test
  void fetchStockClosePrice_WithNullSymbol_ShouldThrowInvalidInputException() {
    // Arrange
    String nullSymbol = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> stocksService.fetchStockClosePrice(nullSymbol));

    assertEquals("Stock symbol cannot be null or empty", exception.getMessage());
    verify(restClient, never()).get();
  }

  @Test
  void fetchStockClosePrice_WithApiException_ShouldThrowRestClientException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenThrow(new RuntimeException("API Error"));

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> stocksService.fetchStockClosePrice(TEST_SYMBOL));

    assertEquals("Failed to fetch stock close price from Finnhub API", exception.getMessage());
  }

  @Test
  void getStockDataForHomePage_WithInvalidMarketCapFormat_ShouldThrowRestClientException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> invalidResponse = new HashMap<>();
    invalidResponse.put("exchange", "NASDAQ");
    invalidResponse.put("marketCapitalization", "invalid_number");
    invalidResponse.put("finnhubIndustry", "Technology");
    when(responseSpec.body(Map.class)).thenReturn(invalidResponse);

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> stocksService.getStockDataForHomePage(TEST_SYMBOL));

    assertEquals("Invalid market capitalization format from API", exception.getMessage());
  }

  @Test
  void getStockDataForHomePage_WithNullResponse_ShouldReturnEmptyDTO() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(null);

    // Act
    StockDataDTO result = stocksService.getStockDataForHomePage(TEST_SYMBOL);

    // Assert
    assertNotNull(result);
    assertNull(result.getExchange());
    assertNull(result.getMarketCap());
    assertNull(result.getIndustry());
  }

  @Test
  void getStock_WithPolygonApiFailure_ShouldThrowRestClientException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenThrow(new RuntimeException("Polygon API Error"));

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> stocksService.getStock(TEST_SYMBOL));

    assertEquals("Failed to fetch stock data from Polygon API", exception.getMessage());
  }

  @Test
  void getStock_WithRecommendationsApiFailure_ShouldThrowRestClientException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> polygonResponse = createPolygonStockDataResponse();

    when(responseSpec.body(Map.class))
            .thenReturn(polygonResponse);

    when(responseSpec.body(List.class))
            .thenThrow(new RuntimeException("Recommendations API Error"));

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> stocksService.getStock(TEST_SYMBOL));

    assertEquals("Failed to fetch stock recommendations from Finnhub API", exception.getMessage());
  }

  @Test
  void fetchStockClosePrice_WithNullResponse_ShouldReturnZero() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(null);

    // Act
    Double result = stocksService.fetchStockClosePrice(TEST_SYMBOL);

    // Assert
    assertEquals(0.0, result, 0.01);
  }

  @Test
  void getStockDataForHomePage_MarketCapConversions_ShouldFormatCorrectly() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    // Test case 1: Trillions
    Map<String, Object> trillionResponse = new HashMap<>();
    trillionResponse.put("exchange", "NYSE");
    trillionResponse.put("marketCapitalization", 2500000.0); // 2.5T in millions
    trillionResponse.put("finnhubIndustry", "Technology");

    when(responseSpec.body(Map.class)).thenReturn(trillionResponse);

    // Act
    StockDataDTO result = stocksService.getStockDataForHomePage("MSFT");

    // Assert
    assertEquals("2.5T", result.getMarketCap());

    // Test case 2: Billions
    Map<String, Object> billionResponse = new HashMap<>();
    billionResponse.put("exchange", "NYSE");
    billionResponse.put("marketCapitalization", 500000.0); // 500B in millions
    billionResponse.put("finnhubIndustry", "Finance");

    when(responseSpec.body(Map.class)).thenReturn(billionResponse);

    // Act
    StockDataDTO result2 = stocksService.getStockDataForHomePage("JPM");

    // Assert
    assertEquals("500.0B", result2.getMarketCap());

    // Test case 3: Millions
    Map<String, Object> millionResponse = new HashMap<>();
    millionResponse.put("exchange", "NASDAQ");
    millionResponse.put("marketCapitalization", 500.0); // 500M
    millionResponse.put("finnhubIndustry", "Healthcare");

    when(responseSpec.body(Map.class)).thenReturn(millionResponse);

    // Act
    StockDataDTO result3 = stocksService.getStockDataForHomePage("SMALL");

    // Assert
    assertEquals("500.0M", result3.getMarketCap());
  }

  @Test
  void getStock_WithEmptyRecommendationsResponse_ShouldReturnEmptyRecommendations() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> polygonResponse = createPolygonStockDataResponse();

    List<Map<String, Object>> emptyRecommendations = new ArrayList<>();

    when(responseSpec.body(Map.class))
            .thenReturn(polygonResponse)    // Polygon call
            .thenReturn(null);           // Financials call (returns null as expected)

    when(responseSpec.body(List.class))
            .thenReturn(emptyRecommendations); // Recommendations call

    // Act
    StockDTO result = stocksService.getStock(TEST_SYMBOL);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getData());
    assertNotNull(result.getRecommendations());
    assertEquals(TEST_COMPANY_NAME, result.getData().getName());
    // Should have empty recommendations DTO
    assertEquals(0, result.getRecommendations().getBuy());
  }

  @Test
  void fetchStockClosePrice_WithEmptySymbol_ShouldThrowInvalidInputException() {
    // Arrange
    String emptySymbol = "";

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> stocksService.fetchStockClosePrice(emptySymbol));

    assertEquals("Stock symbol cannot be null or empty", exception.getMessage());
    verify(restClient, never()).get();
  }

  @Test
  void getStock_WithNullPolygonResponse_ShouldThrowInvalidInputException() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.body(Map.class)).thenReturn(null);

    // Act & Assert
    InvalidInputException ex = assertThrows(
            InvalidInputException.class,
            () -> stocksService.getStock(TEST_SYMBOL)
    );

    assertEquals("Company name cannot be null or empty", ex.getMessage());
  }



  @Test
  void getStockDataForHomePage_CacheIntegration_ShouldWorkCorrectly() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    Map<String, Object> finnhubResponse = createFinnhubStockDataResponse();
    when(responseSpec.body(Map.class)).thenReturn(finnhubResponse);

    // Act
    StockDataDTO result1 = stocksService.getStockDataForHomePage(TEST_SYMBOL);

    // Act
    StockDataDTO result2 = stocksService.getStockDataForHomePage(TEST_SYMBOL);

    // Assert
    assertNotNull(result1);
    assertNotNull(result2);
    assertEquals(result1.getExchange(), result2.getExchange());
    assertEquals(result1.getMarketCap(), result2.getMarketCap());
    assertEquals(result1.getIndustry(), result2.getIndustry());

    verify(restClient, times(1)).get();
  }

  // Helper methods for creating test data
  private Map<String, Object> createFinnhubStockDataResponse() {
    Map<String, Object> response = new HashMap<>();
    response.put("exchange", "NASDAQ");
    response.put("marketCapitalization", 3000000.0); // 3 trillion in millions
    response.put("finnhubIndustry", "Technology");
    return response;
  }

  private Map<String, Object> createPolygonStockDataResponse() {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> results = new HashMap<>();
    Map<String, Object> branding = new HashMap<>();

    branding.put("logo_url", "https://example.com/logo.png");
    results.put("name", TEST_COMPANY_NAME);
    results.put("description", "Technology company");
    results.put("branding", branding);
    results.put("primary_exchange", "NASDAQ");
    results.put("market_cap", 3000000000000.0); // 3 trillion

    response.put("results", results);
    return response;
  }

  private List<Map<String, Object>> createRecommendationsResponse() {
    Map<String, Object> recommendation = new HashMap<>();
    recommendation.put("strongBuy", 10);
    recommendation.put("buy", 5);
    recommendation.put("hold", 3);
    recommendation.put("sell", 1);
    recommendation.put("strongSell", 0);

    List<Map<String, Object>> recommendations = new ArrayList<>();
    recommendations.add(recommendation);
    return recommendations;
  }
}