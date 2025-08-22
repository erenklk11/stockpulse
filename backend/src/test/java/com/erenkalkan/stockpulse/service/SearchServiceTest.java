package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.dto.SearchTickerResponseDTO;
import com.erenkalkan.stockpulse.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @Mock
  private RestClient restClient;

  @Mock
  private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private RestClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private RestClient.ResponseSpec responseSpec;

  @InjectMocks
  private SearchService searchService;

  private static final String TEST_URL = "https://finnhub.io/api/v1/";
  private static final String TEST_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    // Arrange - Set up configuration properties
    ReflectionTestUtils.setField(searchService, "url", TEST_URL);
    ReflectionTestUtils.setField(searchService, "key", TEST_KEY);
  }

  @Test
  void searchTicker_ValidInput_ReturnsSearchResults() {
    // Arrange
    String input = "AAPL";
    Map<String, Object> apiResponse = createMockApiResponse();

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(apiResponse);

    // Act
    List<SearchTickerResponseDTO> result = searchService.searchTicker(input);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("AAPL", result.get(0).getSymbol());
    assertEquals("Apple Inc", result.get(0).getName());
    assertEquals("MSFT", result.get(1).getSymbol());
    assertEquals("Microsoft Corporation", result.get(1).getName());

    verify(restClient).get();
    verify(requestHeadersUriSpec).uri(TEST_URL + "search?q=" + input + "&token=" + TEST_KEY);
  }

  @Test
  void searchTicker_ValidInputWithMoreThan5Results_ReturnsFirst5Results() {
    // Arrange
    String input = "tech";
    Map<String, Object> apiResponse = createMockApiResponseWithManyResults();

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(apiResponse);

    // Act
    List<SearchTickerResponseDTO> result = searchService.searchTicker(input);

    // Assert
    assertNotNull(result);
    assertEquals(5, result.size(), "Should limit results to 5 items");

    // Verify first and last items to ensure correct ordering
    assertEquals("AAPL", result.get(0).getSymbol());
    assertEquals("NFLX", result.get(4).getSymbol());
  }

  @Test
  void searchTicker_NullInput_ThrowsInvalidInputException() {
    // Arrange
    String input = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> searchService.searchTicker(input)
    );

    assertEquals("Input cannot be null or empty", exception.getMessage());
    verifyNoInteractions(restClient);
  }

  @Test
  void searchTicker_EmptyInput_ThrowsInvalidInputException() {
    // Arrange
    String input = "";

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> searchService.searchTicker(input)
    );

    assertEquals("Input cannot be null or empty", exception.getMessage());
    verifyNoInteractions(restClient);
  }

  @Test
  void searchTicker_WhitespaceInput_ThrowsInvalidInputException() {
    // Arrange
    String input = "   ";

    // Act & Assert
    InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> searchService.searchTicker(input)
    );

    assertEquals("Input cannot be null or empty", exception.getMessage());
    verifyNoInteractions(restClient);
  }

  @Test
  void searchTicker_ApiReturnsNullResponse_ReturnsEmptyList() {
    // Arrange
    String input = "INVALID";

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(null);

    // Act
    List<SearchTickerResponseDTO> result = searchService.searchTicker(input);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(restClient).get();
  }

  @Test
  void searchTicker_ApiResponseMissingResultKey_ReturnsEmptyList() {
    // Arrange
    String input = "TEST";
    Map<String, Object> apiResponse = new HashMap<>();
    apiResponse.put("error", "Some error message");

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(apiResponse);

    // Act
    List<SearchTickerResponseDTO> result = searchService.searchTicker(input);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void searchTicker_ApiResponseWithEmptyResult_ReturnsEmptyList() {
    // Arrange
    String input = "NONEXISTENT";
    Map<String, Object> apiResponse = new HashMap<>();
    apiResponse.put("result", List.of());

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(apiResponse);

    // Act
    List<SearchTickerResponseDTO> result = searchService.searchTicker(input);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void searchTicker_RestClientThrowsException_ThrowsRestClientException() {
    // Arrange
    String input = "AAPL";
    RuntimeException apiException = new RuntimeException("API connection failed");

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenThrow(apiException);

    // Act & Assert
    RestClientException exception = assertThrows(
            RestClientException.class,
            () -> searchService.searchTicker(input)
    );

    assertEquals("Failed to fetch search results from Alpha Vantage API", exception.getMessage());
    assertEquals(apiException, exception.getCause());
  }

  @Test
  void searchTicker_ValidInput_BuildsCorrectApiUrl() {
    // Arrange
    String input = "GOOGL";
    String expectedUrl = TEST_URL + "search?q=" + input + "&token=" + TEST_KEY;
    Map<String, Object> apiResponse = createMockApiResponse();

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(apiResponse);

    // Act
    searchService.searchTicker(input);

    // Assert
    verify(requestHeadersUriSpec).uri(expectedUrl);
  }

  @Test
  void searchTicker_ResultsWithNullValues_HandlesGracefully() {
    // Arrange
    String input = "TEST";
    Map<String, Object> apiResponse = createMockApiResponseWithNullValues();

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(Map.class)).thenReturn(apiResponse);

    // Act
    List<SearchTickerResponseDTO> result = searchService.searchTicker(input);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertNull(result.get(0).getSymbol());
    assertNull(result.get(0).getName());
  }

  // Helper methods to create mock API responses
  private Map<String, Object> createMockApiResponse() {
    Map<String, Object> response = new HashMap<>();

    Map<String, String> result1 = new HashMap<>();
    result1.put("symbol", "AAPL");
    result1.put("description", "Apple Inc");

    Map<String, String> result2 = new HashMap<>();
    result2.put("symbol", "MSFT");
    result2.put("description", "Microsoft Corporation");

    response.put("result", List.of(result1, result2));
    return response;
  }

  private Map<String, Object> createMockApiResponseWithManyResults() {
    Map<String, Object> response = new HashMap<>();

    // Create 7 results to test the limit of 5
    Map<String, String> result1 = Map.of("symbol", "AAPL", "description", "Apple Inc");
    Map<String, String> result2 = Map.of("symbol", "MSFT", "description", "Microsoft Corporation");
    Map<String, String> result3 = Map.of("symbol", "GOOGL", "description", "Alphabet Inc");
    Map<String, String> result4 = Map.of("symbol", "AMZN", "description", "Amazon.com Inc");
    Map<String, String> result5 = Map.of("symbol", "NFLX", "description", "Netflix Inc");
    Map<String, String> result6 = Map.of("symbol", "TSLA", "description", "Tesla Inc");
    Map<String, String> result7 = Map.of("symbol", "META", "description", "Meta Platforms Inc");

    response.put("result", List.of(result1, result2, result3, result4, result5, result6, result7));
    return response;
  }

  private Map<String, Object> createMockApiResponseWithNullValues() {
    Map<String, Object> response = new HashMap<>();

    Map<String, String> result1 = new HashMap<>();
    result1.put("symbol", null);
    result1.put("description", null);

    response.put("result", List.of(result1));
    return response;
  }
}